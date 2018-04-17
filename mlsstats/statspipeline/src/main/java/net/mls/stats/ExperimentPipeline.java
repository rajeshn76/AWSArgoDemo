package net.mls.stats;


import com.google.common.collect.ImmutableMap;
import net.mls.event.ExperimentEvent;
import net.mls.event.conf.ExperimentEventParserStrategy;
import net.mls.store.EventCount;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.cassandra.CassandraIO;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;

public class ExperimentPipeline {
    private static final Logger LOG = LoggerFactory.getLogger(ExperimentPipeline.class);
    public static StatsPipelineSettings settings = new StatsPipelineSettings(null);
    public static void main(String[] args) {
        String runnerConfig = String.format("--%s=%s", "runner", settings.rootConf().getString("runner"));
        PipelineOptions options = PipelineOptionsFactory.fromArgs(new String[] {runnerConfig}).withValidation().create();
        Pipeline pipeline = Pipeline.create(options);

        KafkaIO.Read<String, String> kafkaIOReader = KafkaIO.<String, String>read()
                .withBootstrapServers(settings.kafkaHosts())
                .withTopic(settings.kafkaTopicRaw())
                .withKeyDeserializer(StringDeserializer.class)
                .withValueDeserializer(StringDeserializer.class)
                .updateConsumerProperties(ImmutableMap.of(
                        "enable.auto.commit", "true",
                        "group.id", settings.kafkaGroupId()));

        PCollection<ExperimentEvent> experimentCollection = pipeline.apply(kafkaIOReader.withoutMetadata())
                .apply(Values.create())
                .apply(MapElements.via(new ParseIntoExperimentEventFn()));

        PCollection<EventCount> eventCollection = experimentCollection
                .apply(experimentEventWindowing())
                .apply(new CountEvents())
                .apply(MapElements.via(new FormatAsEventCountFn()));

        eventCollection.apply(CassandraIO.<EventCount>write()
                .withHosts(Collections.singletonList(settings.cassandraHosts()))
                .withPort(settings.cassandraNativePort())
                .withKeyspace(settings.cassandraKeyspace())
                .withEntity(EventCount.class));

        pipeline.run().waitUntilFinish();
    }

    private static Window<ExperimentEvent> experimentEventWindowing() {
        final AfterProcessingTime earlyFirings = AfterProcessingTime.pastFirstElementInPane()
                .plusDelayOf(Duration.standardSeconds(10));

        return Window.<ExperimentEvent>into(FixedWindows.of(Duration.standardSeconds(2)))
                .triggering(AfterWatermark.pastEndOfWindow().withEarlyFirings(earlyFirings))
                .withAllowedLateness(Duration.standardDays(1)).discardingFiredPanes();
    }

    public static class ParseIntoExperimentEventFn extends SimpleFunction<String, ExperimentEvent> {
        @Override
        public ExperimentEvent apply(String input) {
            LOG.debug("Received an analytic event: {}", input);
            return ExperimentEventParserStrategy.valueOf(settings.experimentEventParser()).parse(input);
        }
    }

    public static class CountEvents extends PTransform<PCollection<ExperimentEvent>, PCollection<KV<ExperimentEvent, Long>>> {
        @Override
        public PCollection<KV<ExperimentEvent, Long>> expand(PCollection<ExperimentEvent> events) {
            return events.apply(Count.perElement());
        }
    }

    public static class FormatAsEventCountFn extends SimpleFunction<KV<ExperimentEvent, Long>, EventCount> {
        @Override
        public EventCount apply(KV<ExperimentEvent, Long> input) {
            final ExperimentEvent event = input.getKey();
            LOG.debug(event + " :: count = " + input.getValue());
            return new EventCount(
                    new Date(System.currentTimeMillis()),
                    event.getExperiment(),
                    event.getVariant(),
                    input.getValue().floatValue(),
                    !event.isView());
        }
    }
}
