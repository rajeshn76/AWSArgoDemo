package net.mls.pipeline.learning;

import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.mls.pipeline.common.util.InputDataTransform;
import net.mls.pipeline.common.util.MLSPipelinesOptions;
import net.mls.pipeline.common.util.S3Client;
import net.mls.pipeline.learning.util.ZipFile;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class LearningPipeline {
    private static Config conf = ConfigFactory.load();

    public static void main(String[] args) {
        MLSPipelinesOptions options = PipelineOptionsFactory.fromArgs(args)
                .withValidation()
                .as(MLSPipelinesOptions.class);

        Pipeline p = Pipeline.create(options);

        String bucket = Optional.ofNullable(options.getBucket())
                                .orElseGet(() -> conf.getString("s3.bucket"));
        String inputFile = Optional.ofNullable(options.getInputFile())
                                    .orElseGet(() -> conf.getString("s3.inputFile"));
        String outputFile = Optional.ofNullable(options.getOutputFile())
                .orElseGet(() -> conf.getString("s3.outputFile"));

        p.apply(new InputDataTransform(inputFile, bucket))
                .apply(ParDo.of(new RowProcessFn()))
                .apply(GroupByKey.create())
                .apply(ParDo.of(new AggregateRowFn()))
                .apply(ParDo.of(new LogisticRegressionFn(outputFile, bucket)));

        //p.apply("LogisticRegression", new LogisticRegressionTx(options.getBucket(), options.getOutputFile(), options.getInputFile()));
        try {
            p.run().waitUntilFinish();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    static class AggregateRowFn extends DoFn<KV<String, Iterable<Row>>, List<Row>>{
        @ProcessElement
        public void processElement(ProcessContext c) throws Exception {
            Iterable<Row> it = c.element().getValue();
            c.output(Lists.newArrayList(it));
        }
    }

    static class RowProcessFn extends DoFn<String, KV<String, Row>>{
        @ProcessElement
        public void processElement(ProcessContext c) throws Exception {
            String[] line = c.element().split(",");
            Row r = RowFactory.create(line[0], Boolean.parseBoolean(line[1]), line[2], Double.parseDouble(line[3]));
            c.output(KV.of("Test", r));
        }
    }

    static class LogisticRegressionFn extends DoFn<List<Row>, Void>{

        private String output;
        private String bucket;
        LogisticRegressionFn(String output, String bucket) {
           this.output = output;
           this.bucket = bucket;
        }
        @ProcessElement
        public void processElement(ProcessContext c) throws Exception {
            //review,afterRelease,version,label
            StructType schema = new StructType(new StructField[] {
                    new StructField("review", DataTypes.StringType, false, Metadata.empty()),
                    new StructField("afterRelease", DataTypes.BooleanType, false, Metadata.empty()),
                    new StructField("version", DataTypes.StringType, false, Metadata.empty()),
                    new StructField("label", DataTypes.DoubleType, false, Metadata.empty())
            });
//
//            SparkConf sparkConf = new SparkConf().setAppName("logisticRegression")
//                    .setMaster("local")
//                    .set("spark.testing.memory", "471859200");
//            JavaSparkContext jsContext = new JavaSparkContext(sparkConf);
//            SQLContext spark = new SQLContext(jsContext);

            SQLContext spark = SparkSession.builder()
                    .appName("logisticRegression")
                    .master("local")
                    .config("spark.testing.memory", "471859200")
                    .getOrCreate().sqlContext();

            Dataset<Row> data = spark.createDataFrame(c.element(), schema);

            Tokenizer tokenizer = new Tokenizer().setInputCol("review").setOutputCol("words");
            HashingTF hashingTF = new HashingTF().setInputCol("words").setOutputCol("rawFeatures")
                                        .setNumFeatures(1000);
            IDF idf = new IDF().setInputCol("rawFeatures").setOutputCol("features").setMinDocFreq(10);
            LogisticRegression lr = new LogisticRegression()
                                        .setMaxIter(10)
                                        .setRegParam(0.01);

            org.apache.spark.ml.Pipeline pipeline = new org.apache.spark.ml.Pipeline()
                    .setStages(new PipelineStage[]{tokenizer, hashingTF, idf, lr});

            PipelineModel toSave = pipeline.fit(data);

            try {
                String tmp = System.getProperty("java.io.tmpdir") + output.substring(output.lastIndexOf('/'));
                toSave.write().overwrite().save(tmp);
                File zipFile = ZipFile.pack(tmp);


                S3Client.upload(bucket, output, zipFile);
                zipFile.delete();

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }

        }
    }

//    static class LogisticRegressionTx extends PTransform<PBegin, PDone> {
//        private String bucket;
//        private String out;
//        private String in;
//        LogisticRegressionTx(String bucket, String out, String in) {
//            this.bucket = bucket;
//            this.out = out;
//            this.in = in;
//        }
//        SparkSession spark = SparkSession.builder().appName("logisticRegression").master("local").config("spark.testing.memory", "471859200").getOrCreate();
//
//        @Override
//        public PDone expand(PBegin input) {
//
//            spark.sparkContext().hadoopConfiguration().set("fs.s3n.awsAccessKeyId", conf.getString("s3Config.accessKey"));
//            spark.sparkContext().hadoopConfiguration().set("fs.s3n.awsSecretAccessKey",  conf.getString("s3Config.secretKey"));
//
//            String buck = Optional.ofNullable(bucket).orElseGet(() -> conf.getString("output.s3.path"));
//
//            String inFile = String.format("s3n://%s/%s", buck,
//                    Optional.ofNullable(in).orElseGet(() -> conf.getString("inputFile")));
//            Dataset<Row> data = spark.read().option("header", true).option("inferSchema", true).option("encoding", "UTF-8")
//                    .csv(inFile); // "s3n://argo-repo-npq123/TestDataModel.csv"
//
//            Tokenizer tokenizer = new Tokenizer().setInputCol("review").setOutputCol("words");
//            HashingTF hashingTF = new HashingTF().setInputCol("words").setOutputCol("rawFeatures");
//            IDF idf = new IDF().setInputCol("rawFeatures").setOutputCol("features").setMinDocFreq(10);
//            LogisticRegression lr = new LogisticRegression();
//
//            org.apache.spark.ml.Pipeline pipeline = new org.apache.spark.ml.Pipeline()
//                    .setStages(new PipelineStage[]{tokenizer, hashingTF, idf, lr});
//
//            PipelineModel toSave = pipeline.fit(data);
//            try {
//                final String outputPath = conf.getString("outputFile");
//                String tmp = System.getProperty("java.io.tmpdir") + outputPath.substring(outputPath.lastIndexOf('/'));
//                toSave.write().overwrite().save(tmp);
//                File zipFile = ZipFile.pack(tmp);
//                String output = Optional.ofNullable(out).orElseGet(() -> conf.getString("outputFile"));
//
//                S3Client.upload(buck, output, zipFile);
//                zipFile.delete();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                throw new RuntimeException();
//            }
//
//            return PDone.in(input.getPipeline());
//        }
//    }


}
