package net.mls.pipeline.learning.recommender;

import net.mls.pipeline.learning.LearningPipeline;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.recommendation.ALS;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.apache.spark.mllib.recommendation.Rating;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;


public class RecommenderEngine implements Serializable {
    private String outputPath;
    private String accessKey;
    private String secretKey;
    private Integer numIterations;

    public RecommenderEngine(String outputPath, String accessKey, String secretKey, Integer numIterations) {
        this.outputPath = outputPath;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.numIterations = numIterations;
    }
    private static int numPartitions = 20;

    private static SQLContext spark = SparkSession.builder()
            .appName("recommenderEngine")
            .master("local")
            .config("spark.testing.memory", "471859200")
            .getOrCreate().sqlContext();

    public RecommenderEngineTransform transform() {
        return new RecommenderEngineTransform();
    }

    private class RecommenderEngineTransform extends PTransform<PCollection<String>, PDone> {
        @Override
        public PDone expand(PCollection<String> ratings) {
            ratings
                    .apply(ParDo.of(new RatingsFn()))
                    .apply(GroupByKey.create())
                    .apply(ParDo.of(new LearningPipeline.AggregateRowFn<>()))
                    .apply(ParDo.of(new TrainFn()));

            return PDone.in(ratings.getPipeline());

        }
    }

    static class RatingsFn extends DoFn<String, KV<String,Rating>> {
        @ProcessElement
        public void processElement(ProcessContext c) throws Exception {
            String[] line = c.element().split(",");
            c.output(KV.of("Ratings", new Rating(Integer.parseInt(line[0]), Integer.parseInt(line[1]), Double.parseDouble(line[2]))));
        }
    }


    private static Double computeRmse(MatrixFactorizationModel model, RDD<Rating> data) {
        JavaRDD<Rating> rdd = data.toJavaRDD();
        JavaPairRDD<Integer,Integer> pair = rdd.mapToPair(x -> new Tuple2(x.user(), x.product()));

        JavaPairRDD<JavaPairRDD<Integer,Integer>, Double> pairWithPrediction = rdd.mapToPair(x -> new Tuple2(new Tuple2(x.user(), x.product()), x.rating()) );

        long n = data.count();

        JavaRDD<Rating> predictions = model.predict(pair);
        JavaPairRDD<JavaPairRDD<Integer,Integer>, Double> predictionsAndRatings = predictions.mapToPair(x -> new Tuple2(new Tuple2(x.user(), x.product()), x.rating()));

        JavaRDD<Tuple2<Double,Double>> vals = predictionsAndRatings.join(pairWithPrediction)
                .values();

        Double mapped = vals.map(x -> (x._1() - x._2()) * (x._1() - x._2())).reduce( (y,z) -> (y + z)) / n;
        /// n);
        return Math.sqrt(mapped);
    }
    class TrainFn extends DoFn<List<Rating>, Void> {
        @ProcessElement
        public void processElement(ProcessContext c) throws Exception {
            JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());


//                Dataset<Rating> ratingSet = spark.createDataset(list, Encoders.javaSerialization(Rating.class));
//                long count = ratingSet.count();
//                JavaRDD<Rating> rating1 = ratingSet.toJavaRDD();
//                long count1 = rating1.count();

            JavaRDD<Rating> ratings = jsc.parallelize(c.element());
            JavaPairRDD<Long, Rating> dataset = new AddRandomLongColumnFn().apply(ratings);


            RDD<Rating> training = JavaRDD.toRDD(new TrainingFilterFn().apply(dataset));
            System.out.println("Training count "+ training.count());
            RDD<Rating> validation = JavaRDD.toRDD(new ValidationFilterFn().apply(dataset));
            System.out.println("validation count "+ validation.count());
            RDD<Rating> testing = JavaRDD.toRDD(new TestingFilterFn().apply(dataset));


            Integer rank = 8;
            List<Double> lambdas = Arrays.asList(0.1, 10.0);
//            Integer numIter = 10;


            Optional<MatrixFactorizationModel> bestModel = Optional.empty();
            Double bestValidationRmse = Double.MAX_VALUE;
            Integer bestRank = 0;
            Double bestLambda = -1.0;
            Integer bestNumIter = -1;

            long numValidation = validation.count();

            for(Double lambda: lambdas) {

                MatrixFactorizationModel model = ALS.train(training, rank, numIterations, lambda);
                Double validationRmse = computeRmse(model, validation);

                if(validationRmse < bestValidationRmse) {
                    bestModel = Optional.of(model);
                    bestValidationRmse = validationRmse;
                    bestRank = rank;
                    bestLambda = lambda;
                    bestNumIter = numIterations;
                }
            }

            System.out.println("rmse "+bestValidationRmse);
            MatrixFactorizationModel m = bestModel.get();

            spark.sparkContext().hadoopConfiguration().set("fs.s3a.impl", "org.apache.hadoop.fs.s3native.NativeS3FileSystem");
            spark.sparkContext().hadoopConfiguration().set("fs.s3a.awsAccessKeyId",accessKey);
            spark.sparkContext().hadoopConfiguration().set("fs.s3a.awsSecretAccessKey",secretKey);


            m.save(spark.sparkContext(), outputPath);


        }


        private class AddRandomLongColumnFn implements Function<JavaRDD<Rating>, JavaPairRDD<Long, Rating>> {
            @Override
            public JavaPairRDD<Long, Rating> apply(JavaRDD<Rating> rs) {
                Random rand = new Random();

                return rs.mapToPair(r -> new Tuple2((long) rand.nextInt(10), r));
            }
        }

        private class TrainingFilterFn implements Function<JavaPairRDD<Long, Rating>, JavaRDD<Rating>> {
            @Override
            public JavaRDD<Rating> apply(JavaPairRDD<Long, Rating> rs) {
                long c = rs.count();
                return rs.filter(x -> x._1() <=3)
                        .values()
                        .repartition(numPartitions)
                        .cache();
            }

        }

        private class ValidationFilterFn implements Function<JavaPairRDD<Long, Rating>, JavaRDD<Rating>> {
            @Override
            public JavaRDD<Rating> apply(JavaPairRDD<Long, Rating> rs) {
                return rs.filter(x -> x._1() == 4)
                        .values()
                        .repartition(numPartitions)
                        .cache();
            }
        }

        private class TestingFilterFn implements Function<JavaPairRDD<Long, Rating>, JavaRDD<Rating>> {
            @Override
            public JavaRDD<Rating> apply(JavaPairRDD<Long, Rating> rs) {
                return rs.filter(x -> x._1() == 5)
                        .values()
                        .cache();
            }
        }
    }

}
