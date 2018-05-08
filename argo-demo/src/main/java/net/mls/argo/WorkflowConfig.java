package net.mls.argo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkflowConfig {

   @Value("${featureEngineering.featureJar}")
    private String featureJar;

    @Value("${featureEngineering.dataPath}")
    private String dataPath;

    @Value("${featureEngineering.featuresPath}")
    private String featuresPath;

    @Value("${featureEngineering.columns}")
    private String columns;

    @Value("${featureEngineering.funcJar}")
    private String funcJar;

    @Value("${featureEngineering.funcName}")
    private String funcName;

    @Value("${featureEngineering.runner}")
    private String featureRunner;

    @Value("${modelTraining.learningJar}")
    private String learningJar;
    @Value("${modelTraining.modelPath}")
    private String modelPath;
    @Value("${modelTraining.runner}")
    private String trainingRunner;

    @Value("${modelServing.modelJar}")
    private String modelJar;
    @Value("${modelServing.kubeWfName}")
    private String kubeWfName;

    @Value("${modelServing.enableStats}")
    private Boolean enableStats;
    @Value("${modelServing.statsJar}")
    private String statsJar;
    @Value("${modelServing.enableProcessor}")
    private Boolean enableProcessor;
    @Value("${modelServing.processorJar}")
    private String processorJar;

    @Value("${buildPush.dockerRepo}")
    private String dockerRepo;
    @Value("${buildPush.dockerImage}")
    private String dockerImage;
    @Value("${buildPush.dockerVersion}")
    private String dockerVersion;


    @Value("${s3Endpoint}")
    private String s3Endpoint;
    @Value("${s3Bucket}")
    private String s3Bucket;

    @Value("${modelType}")
    private String modelType;

    @Value("${pipelineType}")
    private String pipelineType;

    public String getFeatureJar() {
        return featureJar;
    }

    public String getDataPath() {
        return dataPath;
    }

    public String getFeaturesPath() {
        return featuresPath;
    }

    public String getColumns() {
        return columns;
    }

    public String getFuncJar() {
        return funcJar;
    }

    public String getFuncName() {
        return funcName;
    }

    public String getLearningJar() {
        return learningJar;
    }

    public String getModelPath() {
        return modelPath;
    }

    public String getModelJar() {
        return modelJar;
    }

    public String getDockerRepo() {
        return dockerRepo;
    }

    public String getDockerImage() {
        return dockerImage;
    }

    public String getDockerVersion() {
        return dockerVersion;
    }

    public String getKubeWfName() {
        return kubeWfName;
    }

    public String getS3Endpoint() {
        return s3Endpoint;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public String getFeatureRunner() {
        return featureRunner;
    }

    public String getTrainingRunner() {
        return trainingRunner;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public Boolean getEnableStats() {
        return enableStats;
    }

    public void setEnableStats(Boolean enableStats) {
        this.enableStats = enableStats;
    }

    public String getStatsJar() {
        return statsJar;
    }

    public void setStatsJar(String statsJar) {
        this.statsJar = statsJar;
    }

    public Boolean getEnableProcessor() {
        return enableProcessor;
    }

    public void setEnableProcessor(Boolean enableProcessor) {
        this.enableProcessor = enableProcessor;
    }

    public String getProcessorJar() {
        return processorJar;
    }

    public void setProcessorJar(String processorJar) {
        this.processorJar = processorJar;
    }

    public String getPipelineType() {
        return pipelineType;
    }

    public void setPipelineType(String pipelineType) {
        this.pipelineType = pipelineType;
    }
    public WorkflowConfig() {

    }
    public WorkflowConfig(String dataPath, String featuresPath, String columns, String funcName,
                          String modelPath, String kubeWfName, String dockerVersion, String modelType) {
        this.dataPath = dataPath;
        this.featuresPath = featuresPath;
        this.columns = columns;
        this.funcName = funcName;
        this.modelPath = modelPath;
        this.kubeWfName = kubeWfName;
        this.dockerVersion = dockerVersion;
        this.modelType = modelType;
    }

    public WorkflowConfig mergeWith(WorkflowConfig wc) {
        if (wc != null) {
            wc.featureJar = wc.featureJar != null ? wc.featureJar : this.featureJar;
            wc.dataPath = wc.dataPath != null ? wc.dataPath : this.dataPath;
            wc.featuresPath = wc.featuresPath != null ? wc.featuresPath : this.featuresPath;
            wc.columns = wc.columns != null ? wc.columns : this.columns;
            wc.funcJar = wc.funcJar != null ? wc.funcJar : this.funcJar;
            wc.funcName = wc.funcName != null ? wc.funcName : this.funcName;
            wc.learningJar = wc.learningJar != null ? wc.learningJar : this.learningJar;
            wc.modelPath = wc.modelPath != null ? wc.modelPath : this.modelPath;
            wc.modelJar = wc.modelJar != null ? wc.modelJar : this.modelJar;
            wc.dockerRepo = wc.dockerRepo != null ? wc.dockerRepo : this.dockerRepo;
            wc.dockerImage = wc.dockerImage != null ? wc.dockerImage : this.dockerImage;
            wc.dockerVersion = wc.dockerVersion != null ? wc.dockerVersion : this.dockerVersion;
            wc.kubeWfName = wc.kubeWfName != null ? wc.kubeWfName : this.kubeWfName;
            wc.s3Endpoint = wc.s3Endpoint != null ? wc.s3Endpoint : this.s3Endpoint;
            wc.s3Bucket = wc.s3Bucket != null ? wc.s3Bucket : this.s3Bucket;
            wc.modelType = wc.modelType != null ? wc.modelType : this.modelType;
            wc.featureRunner = wc.featureRunner != null ? wc.featureRunner : this.featureRunner;
            wc.trainingRunner = wc.trainingRunner != null ? wc.trainingRunner : this.trainingRunner;

            wc.statsJar = wc.statsJar != null ? wc.statsJar : this.statsJar;
            wc.enableStats= wc.enableStats != null ? wc.enableStats : this.enableStats;
            wc.processorJar = wc.processorJar != null ? wc.processorJar : this.processorJar;
            wc.enableProcessor= wc.enableProcessor != null ? wc.enableProcessor : this.enableProcessor;

            wc.pipelineType= wc.pipelineType != null ? wc.pipelineType : this.pipelineType;
        }
        return wc;
    }
}
