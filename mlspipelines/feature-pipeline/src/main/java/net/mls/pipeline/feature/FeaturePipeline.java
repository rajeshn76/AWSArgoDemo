package net.mls.pipeline.feature;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.mls.pipeline.common.avro.BasicData;
import net.mls.pipeline.common.util.MLSPipelinesOptions;
import net.mls.pipeline.feature.avro.DataModel;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TupleTagList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;

public class FeaturePipeline {

    private static final Logger LOG = LoggerFactory.getLogger(FeaturePipeline.class);

    private static final TupleTag<String> successTag = new TupleTag<String>() {};
    private static final TupleTag<String> failedTag = new TupleTag<String>() {};

    private static final String S3_PATH_FORMAT = "s3://%s/%s";
    private static Config conf = ConfigFactory.load();

    public static void main(String[] args) throws Exception {


        MLSPipelinesOptions options = PipelineOptionsFactory.fromArgs(args)
                .withValidation()
                .as(MLSPipelinesOptions.class);

        Pipeline p = Pipeline.create(options);

        CoderRegistry cr = p.getCoderRegistry();
        cr.registerCoderForClass(BasicData.class, AvroCoder.of(BasicData.class));
        cr.registerCoderForClass(DataModel.class, AvroCoder.of(DataModel.class));


        String featureColumns = Optional.ofNullable(options.getFeatureColumns())
                .orElseGet(() -> conf.getString("featureEngineering.outputColumns"));

        String fnClass = Optional.ofNullable(options.getFuncName())
                .orElseGet(() -> conf.getString("featureEngineering.fnClass"));

        String bucket = Optional.ofNullable(options.getBucket())
                .orElseGet(() -> conf.getString("s3.bucket"));
        String inputFile = Optional.ofNullable(options.getInputFile())
                .orElseGet(() -> conf.getString("s3.inputFile"));

        String s3InPath = String.format(S3_PATH_FORMAT, bucket, inputFile);

        String outputFile = Optional.ofNullable(options.getOutputFile())
                .orElseGet(() -> conf.getString("s3.outputFile"));

        String s3OutPath =  String.format(S3_PATH_FORMAT, bucket, outputFile);
        String errorPath = String.format(S3_PATH_FORMAT, bucket, conf.getString("s3.errorFile"));
        Function<String, String> fn = (Function<String,String>) Class.forName(fnClass).newInstance();

        PCollectionTuple results =
                p.apply(TextIO.read().from(s3InPath))
                .apply(ParDo.of(new FeatureEngineeringFn(fn))
                            .withOutputTags(successTag, TupleTagList.of(failedTag)));

// TODO fix; The XML you provided was not well-formed or did not validate against our published schema
//        results.get(failedTag)
//                .apply("Failed", TextIO.write().to(errorPath).withoutSharding());

        results.get(successTag)
                .apply("successes", TextIO.write().to(s3OutPath).withoutSharding()
                        .withHeader(featureColumns));

        try {
            p.run().waitUntilFinish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class FeatureEngineeringFn extends DoFn<String, String> {

        private Function<String, String> fn;

        FeatureEngineeringFn(Function<String, String> fn) {
            this.fn = fn;
        }

        @DoFn.ProcessElement
        public void processElement(ProcessContext c) {
            try {
                c.output(fn.apply(c.element()));
            } catch (Exception e) {
                LOG.error("Failed to process {} --- adding to dead letter file. Error {}", c.element(), e.toString());
                c.output(failedTag, c.element());
            }
        }
    }

}
