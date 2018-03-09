package net.mls.pipeline.data;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.mls.pipeline.common.util.ConfigUtil;
import net.mls.pipeline.common.avro.BasicData;
import net.mls.pipeline.data.fn.BasicDataProcessFn;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.io.AvroIO;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;

public class DataIngestionPipeline {

    public static void main(String[] args) {
        String confFile = "application";//args[0];
        Config conf = ConfigFactory.load(confFile);

        String[] config = ConfigUtil.getPipelineOptions(confFile);

        PipelineOptions options = PipelineOptionsFactory.fromArgs(config)
                .withValidation()
                .as(PipelineOptions.class);

        Pipeline p = Pipeline.create(options);


        CoderRegistry cr = p.getCoderRegistry();
        cr.registerCoderForClass(BasicData.class, AvroCoder.of(BasicData.class));
        p.apply(TextIO.read().from(conf.getString("input")))
                .apply(ParDo.of(new RawDataProcessFn()))
                .apply(TextIO.write().to(conf.getString("output")).withoutSharding());
//                .apply(AvroIO.write(BasicData.class).to(conf.getString("output")).withoutSharding());

        p.run();
    }

    static class RawDataProcessFn extends DoFn<String, String> {
        @DoFn.ProcessElement
        public void processElement(ProcessContext c) {
            String[] line = c.element().split("\",\"");
            BasicDataProcessFn fn = new BasicDataProcessFn();
            BasicData data = fn.apply(line);

            c.output(data.getText().toString()+","+data.getDate().toString()+","+data.getSentiment().toString());
        }
    }
}
