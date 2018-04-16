package net.mls.argo.template;

import net.mls.argo.WorkflowConfig;
import net.mls.argo.template.structure.*;
import org.apache.commons.lang.RandomStringUtils;

import java.util.*;

import static net.mls.argo.template.TemplateConstants.*;

public final class MLWorkflow implements WorkflowFactory {

    public WorkflowSpec create(WorkflowConfig conf) {
        WorkflowSpec p = new WorkflowSpec(conf.getModelType());

        Template ft = new Template("feature-training");

        Step featureEngineering = new Step("feature-engineering", "fe-template");
        Arguments feArgs = new Arguments();
        feArgs.addParameter(new Parameter(JAR_PARAM, conf.getFeatureJar()));
        feArgs.addParameter(new Parameter(INPUT_PARAM, conf.getDataPath()));
        feArgs.addParameter(new Parameter(OUTPUT_PARAM, conf.getFeaturesPath()));
        feArgs.addParameter(new Parameter(COLUMNS_PARAM, conf.getColumns()));
        feArgs.addParameter(new Parameter(FE_JAR_PARAM, conf.getFuncJar()));
        feArgs.addParameter(new Parameter(FUNC_PARAM, conf.getFuncName()));
        featureEngineering.setArguments(feArgs);
        ft.addStep(featureEngineering);

        Step modelTraining = new Step("model-training", "mt-template");
        Arguments mtArgs = new Arguments();
        mtArgs.addParameter(new Parameter(JAR_PARAM, conf.getLearningJar()));
        mtArgs.addParameter(new Parameter(INPUT_PARAM, conf.getFeaturesPath()));
        mtArgs.addParameter(new Parameter(OUTPUT_PARAM, conf.getModelPath()));
        mtArgs.addParameter(new Parameter(COLUMNS_PARAM, conf.getColumns()));
        mtArgs.addParameter(new Parameter(MODEL_TYPE_PARAM, conf.getModelType()));
        modelTraining.setArguments(mtArgs);
        ft.addStep(modelTraining);

        Step buildAndPush = new Step("build-and-push", "build-push");
        Arguments bapArgs = new Arguments();
        String rand = RandomStringUtils.randomAlphanumeric(5).toLowerCase();
        String dockerVersion = conf.getDockerVersion() + rand;
        bapArgs.addParameter(new Parameter(JAR_PARAM, conf.getModelJar()));
        bapArgs.addParameter(new Parameter(MODEL_PARAM, conf.getModelPath()));
        bapArgs.addParameter(new Parameter(COLUMNS_PARAM, conf.getColumns()));
        bapArgs.addParameter(new Parameter(DOCKER_REPO_PARAM, conf.getDockerRepo()));
        bapArgs.addParameter(new Parameter(DOCKER_IMAGE_PARAM, conf.getDockerImage()));
        bapArgs.addParameter(new Parameter(DOCKER_VERS_PARAM, dockerVersion));
        buildAndPush.setArguments(bapArgs);
        ft.addStep(buildAndPush);

        Step modelServing = new Step("model-serving", "serving-template");
        Arguments msArgs = new Arguments();
        msArgs.addParameter(new Parameter(KUBE_PARAM, conf.getKubeWfName()
                + rand));
        msArgs.addParameter(new Parameter(DOCKER_REPO_PARAM, conf.getDockerRepo()));
        msArgs.addParameter(new Parameter(DOCKER_IMAGE_PARAM, conf.getDockerImage()));
        msArgs.addParameter(new Parameter(DOCKER_VERS_PARAM, dockerVersion));
        modelServing.setArguments(msArgs);
        ft.addStep(modelServing);

        // Common between FE & MT
        Secret s3AccessSecret = new Secret("s3-credentials", "accessKey");
        Secret s3SecSecret = new Secret("s3-credentials", "secretKey");
        S3 s3 = new S3(conf.getS3Endpoint(), conf.getS3Bucket(), "{{inputs.parameters.jar}}", s3AccessSecret, s3SecSecret);
        Artifact pipelineArtifact = new Artifact(JAR_ART, "/pipeline.jar", s3);

        Map<String, Secret> s3Access = new HashMap<>();
        s3Access.put("secretKeyRef", new Secret("s3-credentials", "accessKey"));
        Map<String, Secret> s3Secret = new HashMap<>();
        s3Secret.put("secretKeyRef", new Secret("s3-credentials", "secretKey"));
        List<Env> s3EnvList = Arrays.asList(new Env(S3_ACCESS, s3Access), new Env(S3_SECRET, s3Secret));

        Resources btResources = new Resources(4096L, 0.3f);


        Template feTemplate = new Template("fe-template");
        Inputs feTemplateInputs = new Inputs();
        feTemplateInputs.addParameter(new Parameter(JAR_PARAM));
        feTemplateInputs.addParameter(new Parameter(INPUT_PARAM));
        feTemplateInputs.addParameter(new Parameter(OUTPUT_PARAM));
        feTemplateInputs.addParameter(new Parameter(COLUMNS_PARAM));
        feTemplateInputs.addParameter(new Parameter(FE_JAR_PARAM));
        feTemplateInputs.addParameter(new Parameter(FUNC_PARAM));

        feTemplateInputs.addArtifact(pipelineArtifact);
        S3 s3Func = new S3(conf.getS3Endpoint(), conf.getS3Bucket(), "{{inputs.parameters.feature-engineering-jar}}", s3AccessSecret, s3SecSecret);
        Artifact funcArtifact = new Artifact(FUNC_ART, "/feature-engineering.jar", s3Func);
        feTemplateInputs.addArtifact(funcArtifact);
        feTemplate.setInputs(feTemplateInputs);

        Container feContainer = createFEContainer(conf.getFeatureRunner());
        feTemplate.setContainer(feContainer);
        feContainer.setEnv(s3EnvList);
        feTemplate.setResources(btResources);


        Template mtTemplate = new Template("mt-template");
        Inputs mtTemplateInputs = new Inputs();
        mtTemplateInputs.addParameter(new Parameter(JAR_PARAM));
        mtTemplateInputs.addParameter(new Parameter(INPUT_PARAM));
        mtTemplateInputs.addParameter(new Parameter(OUTPUT_PARAM));
        mtTemplateInputs.addParameter(new Parameter(COLUMNS_PARAM));
        mtTemplateInputs.addParameter(new Parameter(MODEL_TYPE_PARAM));

        mtTemplateInputs.addArtifact(pipelineArtifact);
        mtTemplate.setInputs(mtTemplateInputs);

        Container mtContainer = createMTContainer(conf.getTrainingRunner());
        mtContainer.setEnv(s3EnvList);

        mtTemplate.setContainer(mtContainer);
        mtTemplate.setResources(btResources);


        Template bp = new Template("build-push");
        Inputs bpInputs = new Inputs();
        bpInputs.addParameter(new Parameter(JAR_PARAM));
        bpInputs.addParameter(new Parameter(MODEL_PARAM));
        bpInputs.addParameter(new Parameter(COLUMNS_PARAM));
        bpInputs.addParameter(new Parameter(DOCKER_REPO_PARAM));
        bpInputs.addParameter(new Parameter(DOCKER_IMAGE_PARAM));
        bpInputs.addParameter(new Parameter(DOCKER_VERS_PARAM));

        String modelType = conf.getModelType();
        String buildCmd = String.format("%s %s", BUILD_PUSH_CMD, modelType);
        String branch;

        if(modelType.equalsIgnoreCase("sentiment")) {
            buildCmd += BP_SA_PARAMS;
            branch = "sentiment-analysis";
        } else {
            branch = "recommender-engine";
        }

        Artifact bpJarArtifact = new Artifact(JAR_ART, "/model-serving.jar", s3);
        Git git = new Git("https://github.com/venci6/demos.git", "model/"+branch);
        Artifact bpGitArtifact = new Artifact("docker-files", "/docker-files", git);
        bpInputs.addArtifact(bpGitArtifact);
        bpInputs.addArtifact(bpJarArtifact);
        bp.setInputs(bpInputs);


        Container bpContainer = new Container(IMAGE_DOCKER, Arrays.asList("sh", "-c"), Collections.singletonList(buildCmd));
        Map<String, Secret> userMap = new HashMap<>();
        userMap.put("secretKeyRef", new Secret("docker-credentials", "username"));
        Map<String, Secret> pwMap = new HashMap<>();
        pwMap.put("secretKeyRef", new Secret("docker-credentials", "password"));
        bpContainer.setEnv(Arrays.asList(new Env(DOCKER_HOST, "127.0.0.1"), new Env(DOCKER_USERNAME, userMap), new Env(DOCKER_PASSWORD, pwMap)));

        bp.setContainer(bpContainer);
        Map<String, Boolean> securityContext = new HashMap<>();
        securityContext.put("privileged", true);
        Sidecar sc = new Sidecar("dind", IMAGE_DIND, securityContext, true);
        List<Sidecar> sidecars = Collections.singletonList(sc);
        bp.setSidecars(sidecars);


        Template st = new Template("serving-template");
        Inputs stInputs = new Inputs();
        stInputs.addParameter(new Parameter(KUBE_PARAM));
        stInputs.addParameter(new Parameter(DOCKER_REPO_PARAM));
        stInputs.addParameter(new Parameter(DOCKER_IMAGE_PARAM));
        stInputs.addParameter(new Parameter(DOCKER_VERS_PARAM));
        st.setInputs(stInputs);

        Resource r = new Resource("create", KUBE_MANIFEST);
        st.setResource(r);

        p.spec.addTemplate(ft);
        p.spec.addTemplate(feTemplate);
        p.spec.addTemplate(mtTemplate);
        p.spec.addTemplate(bp);
        p.spec.addTemplate(st);

        return p;
    }

    private Container createFEContainer(String runner) {
        Container c;
        List<String> bash = Arrays.asList("bash", "-c");

        if(runner.equalsIgnoreCase("FlinkRunner")) {
            c = new Container(IMAGE_FLINK, bash, Collections.singletonList(FE_FLINK_CMD));
            // TODO spark runner for FE
//        } else if(runner.equalsIgnoreCase("SparkRunner")) {
//            c = new Container(IMAGE_JAVA, bash, Collections.singletonList(MT_SPARK_CMD));
        } else {  // DirectRunner
            c = new Container(IMAGE_JAVA, bash, Collections.singletonList(FE_DIRECT_CMD));
        }
        return c;
    }

    private Container createMTContainer(String runner) {
        Container c;
        List<String> bash = Arrays.asList("bash", "-c");

        if(runner.equalsIgnoreCase("FlinkRunner")) {
            c = new Container(IMAGE_FLINK, bash, Collections.singletonList(MT_FLINK_CMD));
        } else if(runner.equalsIgnoreCase("SparkRunner")) {
            c = new Container(IMAGE_JAVA, bash, Collections.singletonList(MT_SPARK_CMD));
        } else {  // DirectRunner
            c = new Container(IMAGE_JAVA, bash, Collections.singletonList(MT_DIRECT_CMD));
        }
        return c;
    }
}
