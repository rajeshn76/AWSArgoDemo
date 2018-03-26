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
            wc.featureRunner = wc.featureRunner != null ? wc.featureRunner : this.featureRunner;;
            wc.trainingRunner = wc.trainingRunner != null ? wc.trainingRunner : this.trainingRunner;;
        }
        return wc;
    }
}