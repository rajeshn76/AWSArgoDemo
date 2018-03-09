package net.mls.pipeline.feature;

import net.mls.pipeline.common.util.S3Client;
import net.mls.pipeline.feature.fn.AggregateCSVStringsFn;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;


public class OutputDataModelTransform extends PTransform<PCollection<String>, PDone> {
    private String outputFile;
    private String bucket;

    public OutputDataModelTransform(String outputFile, String bucket) {
        this.outputFile = outputFile;
        this.bucket = bucket;
    }

    @Override
    public PDone expand(PCollection<String> csvStrings) {
        csvStrings
                .apply(ParDo.of(new StringToKVFn()))
                .apply(GroupByKey.create())
                .apply(ParDo.of(new AggregateCSVStringsFn()))
                .apply("WriteDataModel", ParDo.of(new S3UploadFn()));
        return PDone.in(csvStrings.getPipeline());
    }

    private class S3UploadFn extends DoFn<String, Void> {
        @DoFn.ProcessElement
        public void processElement(ProcessContext c) throws Exception {
            S3Client.uploadFromString(bucket, outputFile, c.element());
        }
    }

    private class StringToKVFn extends DoFn<String, KV<String,String>> {
        @DoFn.ProcessElement
        public void processElement(ProcessContext c) throws Exception {
            c.output(KV.of("GroupByKey", c.element()));
        }
    }
}