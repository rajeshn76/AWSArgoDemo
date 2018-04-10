package net.mls.pipeline.common.util;

import org.apache.beam.sdk.io.aws.options.S3Options;

public interface MLSPipelinesOptions extends S3Options {

    String getInputFile();
    void setInputFile(String value);

    String getOutputFile();
    void setOutputFile(String value);

    String getBucket();
    void setBucket(String value);

    String getFeatureColumns();
    void setFeatureColumns(String value);

    String getFuncName();
    void setFuncName(String value);

    String getModelType();
    void setModelType(String value);



}