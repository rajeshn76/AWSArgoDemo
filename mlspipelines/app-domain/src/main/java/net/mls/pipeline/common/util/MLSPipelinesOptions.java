package net.mls.pipeline.common.util;

import org.apache.beam.sdk.options.PipelineOptions;

public interface MLSPipelinesOptions extends PipelineOptions {

    String getInputFile();
    void setInputFile(String value);

    String getOutputFile();
    void setOutputFile(String value);

    String getBucket();
    void setBucket(String value);

    String getAccessKey();
    void setAccessKey(String value);

    String getSecretAccessKey();
    void setSecretAccessKey(String value);

}