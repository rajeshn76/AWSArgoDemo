package net.mls.pipeline.common.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ConfigUtil {
    public static final String HDFS_CONFIGURATION = "hdfsConfiguration";
    public static final String RUNNER = "runner";
    public static final String DEFAULT_FS = "fs.defaultFS";
//    public static String[] pipelineOptions;
//    static {
//        Config conf = ConfigFactory.load("test");
//
////        String hdfsConfig = String.format("--%s=[{\"%s\":\"%s\"}]", HDFS_CONFIGURATION, DEFAULT_FS, conf.getString(HDFS_CONFIGURATION+"."+DEFAULT_FS));
//        String runnerConfig = String.format("--%s=%s", RUNNER, conf.getString(RUNNER));
//        pipelineOptions = new String[]{runnerConfig};
//    }

    public static String[] getPipelineOptions(String configFile) {
        Config conf = ConfigFactory.load(configFile);
        String runnerConfig = String.format("--%s=%s", RUNNER, conf.getString(RUNNER));
        return new String[]{runnerConfig};
    }
}
