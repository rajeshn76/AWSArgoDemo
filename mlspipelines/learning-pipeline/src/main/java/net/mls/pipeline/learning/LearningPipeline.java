package net.mls.pipeline.learning;

import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.mls.pipeline.common.util.MLSPipelinesOptions;
import net.mls.pipeline.learning.recommender.RecommenderEngine;
import net.mls.pipeline.learning.sentimentanalysis.SentimentAnalysisTraining;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.KV;

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
        String modelType = Optional.ofNullable(options.getModelType())
                .orElseGet(() -> conf.getString("modelType"));

        String  inputPath = "s3://"+bucket+"/"+inputFile;
        System.out.println("Reading from s3: " +inputPath);
        p.apply(TextIO.read().from(inputPath))
                .apply(Filter.by(x -> !x.equals(featureColumns)))
                .apply(getModelTrainingClass(modelType, featureColumns, output));

        try {
            p.run().waitUntilFinish();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static PTransform getModelTrainingClass(String str, String cols, String output) {
        if (str.equalsIgnoreCase("sentiment")) {
            SentimentAnalysisTraining sa = new SentimentAnalysisTraining(cols, output, bucket);
            return sa.transform();
        } else if(str.equalsIgnoreCase("recommender")) {
            String  outputPath = "s3a://"+bucket+"/"+output;
            RecommenderEngine re = new RecommenderEngine(outputPath, conf.getString("s3Config.accessKey"), conf.getString("s3Config.secretKey"));
            return re.transform();
        } else {
            throw new UnsupportedOperationException("No model for " + str);
        }
    }

    public static class AggregateRowFn<T> extends DoFn<KV<String, Iterable<T>>, List<T>>{
        @ProcessElement
        public void processElement(ProcessContext c) throws Exception {
            Iterable<T> it = c.element().getValue();
            c.output(Lists.newArrayList(it));
        }
    }
}
