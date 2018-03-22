package net.mls.pipeline.learning;

import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.mls.pipeline.common.util.MLSPipelinesOptions;
import net.mls.pipeline.common.util.S3Client;
import net.mls.pipeline.learning.util.ZipFile;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
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


    static String bucket;

    public static void main(String[] args) {
        MLSPipelinesOptions options = PipelineOptionsFactory.fromArgs(args)
                .withValidation()
                .as(MLSPipelinesOptions.class);

        Pipeline p = Pipeline.create(options);

        bucket = Optional.ofNullable(options.getBucket())
                                .orElseGet(() -> conf.getString("s3.bucket"));
        String inputFile = Optional.ofNullable(options.getInputFile())
                                    .orElseGet(() -> conf.getString("s3.inputFile"));
        String output = Optional.ofNullable(options.getOutputFile())
                .orElseGet(() -> conf.getString("s3.outputFile"));

        String featureColumns = Optional.ofNullable(options.getFeatureColumns())
                .orElseGet(() -> conf.getString("columns.input"));

        p.apply(TextIO.read().from("s3://"+bucket+"/"+inputFile))
                .apply(Filter.by(x -> !x.equals(conf.getString("columns.input"))))
                .apply(ParDo.of(new RowProcessFn()))
                .apply(GroupByKey.create())
                .apply(ParDo.of(new AggregateRowFn()))
                .apply(ParDo.of(new LogisticRegressionFn(featureColumns, output, bucket)));

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
        String featureColumns;
        String output;
        String bucket;
        LogisticRegressionFn(String featureColumns, String output, String bucket) {
            this.featureColumns=featureColumns;
            this.output=output;
            this.bucket=bucket;
        }
        @ProcessElement
        public void processElement(ProcessContext c) throws Exception {
            //review,afterRelease,version/hour,label

            System.out.println("Feature columns"+featureColumns);
            String[] header = featureColumns.split(",");
            StructType schema = new StructType(new StructField[] {
                    new StructField(header[0], DataTypes.StringType, false, Metadata.empty()),
                    new StructField(header[1], DataTypes.BooleanType, false, Metadata.empty()),
                    new StructField(header[2], DataTypes.StringType, false, Metadata.empty()),
                    new StructField(header[3], DataTypes.DoubleType, false, Metadata.empty())
            });

            SQLContext spark = SparkSession.builder()
                    .appName("logisticRegression")
                    .master("local")
                    .config("spark.testing.memory", "471859200")
                    .getOrCreate().sqlContext();

            Dataset<Row> data = spark.createDataFrame(c.element(), schema);

            Tokenizer tokenizer = new Tokenizer().setInputCol(header[0]).setOutputCol("words");
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


}
