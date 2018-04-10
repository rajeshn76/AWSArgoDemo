package net.mls.pipeline.learning.sentimentanalysis;

import net.mls.pipeline.common.util.S3Client;
import net.mls.pipeline.learning.LearningPipeline;
import net.mls.pipeline.learning.util.ZipFile;
import org.apache.beam.sdk.transforms.DoFn;

import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.apache.spark.sql.*;

import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class SentimentAnalysisTraining  implements Serializable {
    String featureColumns;
    String output;
    String bucket;

    public SentimentAnalysisTraining(String featureColumns, String output, String bucket) {
        this.featureColumns = featureColumns;
        this.output = output;
        this.bucket = bucket;
    }

    public LogisticRegressionTransform transform() {
        return new LogisticRegressionTransform();
    }
    public class LogisticRegressionTransform extends PTransform<PCollection<String>, PDone> {
        String featureColumns;
        String output;
        String bucket;


        @Override
        public PDone expand(PCollection<String> input) {

            input
                    .apply(ParDo.of(new RowProcessFn()))
                    .apply(GroupByKey.create())
                    .apply(ParDo.of(new LearningPipeline.AggregateRowFn<>()))
                    .apply(ParDo.of(new LogisticRegressionFn()));

            return PDone.in(input.getPipeline());
        }
    }

    class RowProcessFn extends DoFn<String, KV<String, Row>> {
        @ProcessElement
        public void processElement(ProcessContext c) throws Exception {
            String[] line = c.element().split(",");
            Row r = RowFactory.create(line[0], Boolean.parseBoolean(line[1]), line[2], Double.parseDouble(line[3]));
            c.output(KV.of("Test", r));

        }
    }

    class LogisticRegressionFn extends DoFn<List<Row>, Void> {
        @ProcessElement
        public void processElement(ProcessContext c) throws Exception {
            //review,afterRelease,version/hour,label

            System.out.println("Feature columns" + featureColumns);
            String[] header = featureColumns.split(",");
            StructType schema = new StructType(new StructField[]{
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