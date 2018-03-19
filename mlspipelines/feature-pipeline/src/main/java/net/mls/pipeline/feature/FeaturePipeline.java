package net.mls.pipeline.feature;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.mls.pipeline.common.avro.BasicData;
import net.mls.pipeline.common.util.MLSPipelinesOptions;
import net.mls.pipeline.feature.avro.DataModel;
import net.mls.pipeline.feature.avro.IOSReview;
import net.mls.pipeline.feature.fn.DataModelProcessFn;
import net.mls.pipeline.feature.fn.DataModelStringifyFn;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.repackaged.org.apache.commons.lang3.StringUtils;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;

import java.util.Optional;

public class FeaturePipeline {

    static final String S3_PATH_FORMAT = "s3://%s/%s";
    static Config conf = ConfigFactory.load();

    public static void main(String[] args) {


        MLSPipelinesOptions options = PipelineOptionsFactory.fromArgs(args)
                .withValidation()
                .as(MLSPipelinesOptions.class);

        Pipeline p = Pipeline.create(options);

        CoderRegistry cr = p.getCoderRegistry();
        cr.registerCoderForClass(BasicData.class, AvroCoder.of(BasicData.class));
        cr.registerCoderForClass(DataModel.class, AvroCoder.of(DataModel.class));


        String featureColumns = Optional.ofNullable(options.getFeatureColumns())
                .orElseGet(() -> conf.getString("columns.output"));


        String bucket = Optional.ofNullable(options.getBucket())
                .orElseGet(() -> conf.getString("s3.bucket"));
        String inputFile = Optional.ofNullable(options.getInputFile())
                .orElseGet(() -> conf.getString("s3.inputFile"));

        String s3InPath = String.format(S3_PATH_FORMAT, bucket, inputFile);

        String outputFile = Optional.ofNullable(options.getOutputFile())
                .orElseGet(() -> conf.getString("s3.outputFile"));

        String s3OutPath =  String.format(S3_PATH_FORMAT, bucket, outputFile);

        p.apply(TextIO.read().from(s3InPath))
                .apply(ParDo.of(new BasicDataProcessFn()))
                .apply(ParDo.of(new CSVStringifyFn()))
                .apply(TextIO.write().to(s3OutPath).withoutSharding()
                        .withHeader(featureColumns));

        try {
            p.run().waitUntilFinish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class BasicDataProcessFn extends DoFn<String, DataModel> {
        @DoFn.ProcessElement
        public void processElement(ProcessContext c) {
            String[] line = c.element().split(",");

            IOSReview review;
            if(StringUtils.isNumeric( line[2])) { // date,version,rating,review
                review  = new IOSReview(line[0].trim(), line[3].trim(), line[2].trim(), line[1].trim());
            } else { // id,date,tweet,sentiment
                review = new IOSReview(line[1].trim(), line[2].trim(), line[3].trim(), null);
            }

            if(!review.getBody().toString().isEmpty()) {
                DataModelProcessFn fn = new DataModelProcessFn();
                c.output(fn.apply(review));
            }
        }
    }

    static class CSVStringifyFn extends DoFn<DataModel, String> {
        @DoFn.ProcessElement
        public void processElement(ProcessContext c) {
            DataModel dataModel = c.element();
            DataModelStringifyFn fn = new DataModelStringifyFn();
            c.output(fn.apply(dataModel));
        }
    }
}
