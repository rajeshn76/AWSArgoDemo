package net.mls.pipeline.feature.fn;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by char on 2/2/18.
 */
public class AggregateCSVStringsFn extends DoFn<KV<String, Iterable<String>>, String> {
    private final String HEADER = "review,afterRelease,version,label";

    @DoFn.ProcessElement
    public void processElement(ProcessContext c) throws Exception {
        String result= StreamSupport.stream(c.element().getValue().spliterator(), true)
                .collect(Collectors.joining("\n"));
        c.output(result);
    }
}
