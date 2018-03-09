package net.mls.pipeline.common.util;

import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class InputDataTransform extends PTransform<PBegin, PCollection<String>> {
    private String inputFile;
    private String bucket;

    public InputDataTransform(String inputFile, String bucket) {
        this.inputFile = inputFile;
        this.bucket = bucket;
    }

    @Override
    public PCollection<String> expand(PBegin input) {
        PCollection<String> lines = input.getPipeline().apply(Create.of("s3"))
                .apply(ParDo.of(new S3ReadFn("\n")));
        return lines;
    }

     class S3ReadFn extends DoFn<String, String> {
        private String delimiter;
        S3ReadFn(String delimiter) {
            this.delimiter = delimiter;
        }
        @ProcessElement
        public void processElement(ProcessContext c) throws Exception {
            InputStream is = S3Client.readFromString(bucket, inputFile);

            String result = IOUtils.toString(is, StandardCharsets.UTF_8);
            String[] text = result.split(delimiter);

            Arrays.stream(text).forEach(line -> c.output(line));
        }
    }
}
