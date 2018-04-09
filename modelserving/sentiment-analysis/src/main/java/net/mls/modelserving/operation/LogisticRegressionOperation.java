package net.mls.modelserving.operation;


import net.mls.modelserving.util.S3Client;
import net.mls.modelserving.util.ZipFile;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Service("lreg")
public class LogisticRegressionOperation implements Function<String, String> {

    Logger LOG = LoggerFactory.getLogger(LogisticRegressionOperation.class);

    private SparkSession spark = SparkSession.builder().appName("LogisticRegressionOperation").master("local").getOrCreate();
    private PipelineModel model = null;

    @Value("${s3.accessKey}")
    private String accessKey;
    @Value("${s3.secretKey}")
    private String secretKey;
    @Value("${s3.endpoint}")
    private String endpoint;
    @Value("${s3.bucketName}")
    private String bucketName;
    @Value("${sentimentAnalysis.model}")
    private String modelPath;
    @Value("${sentimentAnalysis.columns}")
    private String columns;

    @PostConstruct
    private void init() throws IOException {
        LOG.info("Reading model from s3 {}, {}, {}", endpoint, bucketName, modelPath);
        S3Client client = new S3Client(accessKey, secretKey, endpoint);
        File tmpZipFile = client.download(bucketName, modelPath);
        String tmpUnzipPath = ZipFile.unpack(tmpZipFile);
        model = PipelineModel.load(tmpUnzipPath);
        tmpZipFile.delete();
    }
    public String apply(String review) {
        List<Row> data = Collections.singletonList(RowFactory.create(review, false, "0", 0.0));

        String[] header = columns.split(",");

        StructType schema = new StructType(new StructField[] {
                new StructField(header[0], DataTypes.StringType, false, Metadata.empty()),
                new StructField(header[1], DataTypes.BooleanType, false, Metadata.empty()),
                new StructField(header[2], DataTypes.StringType, false, Metadata.empty()),
                new StructField(header[3], DataTypes.DoubleType, false, Metadata.empty())
        });

        Dataset<Row> testData = spark.createDataFrame(data, schema);
        Dataset<Row> prediction = model.transform(testData);

        Row testDataVec = ((Row[]) prediction.collect())[0];
        double predictionResult = (double) testDataVec.get(testDataVec.size()-1);
        return "Sentiment is " + (predictionResult == 1.0 ? "positive" : "negative");


    }
}
