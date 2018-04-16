package net.mls.modelserving.operation;

import net.mls.modelserving.MovieView;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.apache.spark.mllib.recommendation.Rating;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import scala.Tuple2;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service("rec")
public class MatrixFactorizationOperation implements Function<String,  List<MovieView>> {

    Logger LOG = LoggerFactory.getLogger(MatrixFactorizationOperation.class);

    @Value("${s3.accessKey}")
    private String accessKey;
    @Value("${s3.secretKey}")
    private String secretKey;
    @Value("${s3.bucketName}")
    private String bucket;
    @Value("${recommenderEngine.model}")
    private String modelPath;
    @Value("${recommenderEngine.moviesPath}")
    private String movieNamesPath;
    @Value("${recommenderEngine.count}")
    private int count;
    @Value("${modelType}")
    private String modelType;

    private MatrixFactorizationModel model;
    private Map<Integer, String> productNames;
    private SparkContext sc;
    private JavaPairRDD<Integer, String> pairRDD;

    @PostConstruct
    private void init() throws IOException {
        if(modelType.equalsIgnoreCase("recommender")) {
            LOG.info("Initializing MatrixFactorization Model");

            sc = SparkSession.builder()
                    .appName("matrixFactorization")
                    .master("local")
                    .config("spark.testing.memory", "471859200")
                    .getOrCreate().sparkContext();

            sc.hadoopConfiguration().set("fs.s3a.impl", "org.apache.hadoop.fs.s3native.NativeS3FileSystem");
            sc.hadoopConfiguration().set("fs.s3a.awsAccessKeyId", accessKey);
            sc.hadoopConfiguration().set("fs.s3a.awsSecretAccessKey", secretKey);

            String path = String.format("s3a://%s/%s", bucket, modelPath);
            LOG.info("Reading matrix factorization model from s3 {}", path);

            model = MatrixFactorizationModel.load(sc, path);

            String moviesPath = String.format("s3a://%s/%s", bucket, movieNamesPath);
            LOG.info("Reading movies from s3 {}", moviesPath);

            pairRDD = sc.textFile(moviesPath, 2).toJavaRDD()
                    .map(line -> line.split("\\|"))
                    .mapToPair(arr -> new Tuple2(Integer.parseInt(arr[0]), arr[1]));

            productNames = pairRDD.collectAsMap();
        }
    }
    @Override
    public List<MovieView> apply(String userId) {
        JavaSparkContext jsc = new JavaSparkContext(sc);

        Integer user = Integer.parseInt(userId);

        JavaRDD<Integer> candidates = jsc.parallelize(pairRDD.keys().collect());
        JavaPairRDD<Integer, Integer> mapped = candidates.mapToPair(c -> new Tuple2(user, c));


        List<Rating> modifiable = new ArrayList<>(model.predict(mapped)
                .collect());

        modifiable.sort(Comparator.comparing(Rating::rating).reversed());

        List<Rating> recommendations = modifiable.subList(0, count);

        return  recommendations.stream().map(r -> new MovieView(productNames.get(r.product()), r.rating())).collect(Collectors.toList());
    }

}
