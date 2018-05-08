package net.mls.argo.template;

import com.google.common.collect.ImmutableMap;
import net.mls.argo.WorkflowConfig;
import net.mls.argo.template.structure.*;
import net.mls.argo.util.PipelineType;
import org.apache.commons.lang.RandomStringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static net.mls.argo.template.TemplateConstants.*;

public final class MLWorkflow implements WorkflowFactory {

    private WorkflowConfig conf;
    private String rand;
    private String dockerVersion;

    private final Secret s3AccessSecret = new Secret("s3-credentials", "accessKey");
    private final Secret s3SecSecret = new Secret("s3-credentials", "secretKey");
    private Artifact pipelineArtifact;

    public WorkflowSpec create(PipelineType pipelineType, WorkflowConfig conf) {
        this.conf = conf;
        this.rand = RandomStringUtils.randomAlphanumeric(5).toLowerCase();
        this.dockerVersion = conf.getDockerVersion() + rand;
        this.pipelineArtifact = getJarArtifact(s3AccessSecret, s3SecSecret, "/pipeline.jar");

        WorkflowSpec wf;

        switch(pipelineType) {
            case BUILDING:
                wf = createBuildingPipeline();
                break;
            case SERVING:
                wf = createServingPipeline();
                break;
            case BUILDING_SERVING:
                wf = createBuildingAndServing();
                break;
            default:
                wf = createBuildingAndServing();
                break;
        }
        return wf;
    }

    private WorkflowSpec createBuildingAndServing() {
        WorkflowSpec p = new WorkflowSpec(conf.getModelType());
        Spec spec = getGlobalSpecParams(PipelineType.BUILDING_SERVING);
        p.setSpec(spec);

        Template ft = new Template(PipelineType.BUILDING_SERVING.toString());

        // Steps
        Step featureEngineering = getFeatureEngineeringStep();
        ft.addStep(featureEngineering);

        Step modelTraining = getModelTrainingStep();
        ft.addStep(modelTraining);

        Step buildAndPush = getBuildPushStep();
        ft.addStep(buildAndPush);
        List<Map<String,String>> bpItems = createBPItems();
        buildAndPush.setItems(bpItems);

        Step modelServing = getModelServingStep();
        ft.addStep(modelServing);
        List<Map<String, String>> servingItems = createServingItems();
        modelServing.setItems(servingItems);


        List<Env> s3EnvList = getS3EnvList();
        Resources btResources = new Resources(4096L, 0.3f);


        // Templates
        Template feTemplate = getFeatureEngineeringTemplate(pipelineArtifact, s3AccessSecret, s3SecSecret, s3EnvList, btResources);
        Template mtTemplate = getModelTrainingTemplate(pipelineArtifact,s3EnvList,btResources);
        Template bpTemplate = getBuildPushTemplate(s3AccessSecret, s3SecSecret);
        Template stTemplate = getServingTemplate();

        p.spec.addTemplate(ft);
        p.spec.addTemplate(feTemplate);
        p.spec.addTemplate(mtTemplate);
        p.spec.addTemplate(bpTemplate);
        p.spec.addTemplate(stTemplate);

        return p;
    }

    private WorkflowSpec createBuildingPipeline() {
        WorkflowSpec p = new WorkflowSpec(conf.getModelType());
        Spec spec = getGlobalSpecParams(PipelineType.BUILDING);
        p.setSpec(spec);

        Template ft = new Template(PipelineType.BUILDING.toString());

        Step featureEngineering = getFeatureEngineeringStep();
        ft.addStep(featureEngineering);
        Step modelTraining = getModelTrainingStep();
        ft.addStep(modelTraining);

        List<Env> s3EnvList = getS3EnvList();
        Resources btResources = new Resources(4096L, 0.3f);

        Template feTemplate = getFeatureEngineeringTemplate(pipelineArtifact, s3AccessSecret, s3SecSecret, s3EnvList, btResources);
        Template mtTemplate = getModelTrainingTemplate(pipelineArtifact,s3EnvList,btResources);

        p.spec.addTemplate(ft);
        p.spec.addTemplate(feTemplate);
        p.spec.addTemplate(mtTemplate);

        return p;
    }

    private WorkflowSpec createServingPipeline() {
        WorkflowSpec p = new WorkflowSpec(conf.getModelType());
        Spec spec = getGlobalSpecParams(PipelineType.SERVING);
        p.setSpec(spec);

        Template ft = new Template(PipelineType.SERVING.toString());

        Step buildAndPush = getBuildPushStep();
        ft.addStep(buildAndPush);
        List<Map<String,String>> bpItems = createBPItems();
        buildAndPush.setItems(bpItems);


        Step modelServing = getModelServingStep();
        ft.addStep(modelServing);
        List<Map<String, String>> servingItems = createServingItems();
        modelServing.setItems(servingItems);


        Template bp = getBuildPushTemplate(s3AccessSecret, s3SecSecret);
        Template st = getServingTemplate();

        p.spec.addTemplate(ft);

        p.spec.addTemplate(bp);
        p.spec.addTemplate(st);

        return p;
    }

    // Spec Level Params
    private Spec getGlobalSpecParams(PipelineType pipelineType) {
        Spec spec = new Spec(pipelineType.toString());
        Arguments globalArgs = new Arguments();
        globalArgs.addParameter(new Parameter(COLUMNS_PARAM, conf.getColumns()));
        globalArgs.addParameter(new Parameter(MODEL_TYPE_PARAM, conf.getModelType()));
        switch(pipelineType) {
            case BUILDING:
                setGlobalBuildingParams(globalArgs);
                break;
            case SERVING:
                setGlobalServingParams(globalArgs);
                break;
            case BUILDING_SERVING:
                setGlobalBuildingParams(globalArgs);
                setGlobalServingParams(globalArgs);
                break;
        }
        spec.setArguments(globalArgs);
        return spec;
    }

    private void setGlobalBuildingParams(Arguments global) {
        global.addParameter(new Parameter(FEATURES_PARAM, conf.getFeaturesPath()));
        global.addParameter(new Parameter(MODEL_PATH, conf.getModelPath()));
    }

    private void setGlobalServingParams(Arguments global) {
        global.addParameter(new Parameter(DOCKER_REPO_PARAM, conf.getDockerRepo()));
        global.addParameter(new Parameter(DOCKER_VERS_PARAM, dockerVersion));
    }

    // Steps
    private Step getFeatureEngineeringStep() {
        Step step = new Step("feature-engineering", "fe-template");
        Arguments feArgs = new Arguments();
        feArgs.addParameter(new Parameter(JAR_PARAM, conf.getFeatureJar()));
        feArgs.addParameter(new Parameter(INPUT_PARAM, conf.getDataPath()));
        feArgs.addParameter(new Parameter(FE_JAR_PARAM, conf.getFuncJar()));
        feArgs.addParameter(new Parameter(FUNC_PARAM, conf.getFuncName()));
        step.setArguments(feArgs);
        return step;
    }

    private Step getModelTrainingStep() {
        Step step = new Step("model-training", "mt-template");
        Arguments mtArgs = new Arguments();
        mtArgs.addParameter(new Parameter(JAR_PARAM, conf.getLearningJar()));
        step.setArguments(mtArgs);
        return step;
    }

    private Step getBuildPushStep() {
        Step buildAndPush = new Step("build-and-push", "build-push-template");
        Arguments bapArgs = new Arguments();
        bapArgs.addParameter(new Parameter(JAR_PARAM, "{{item.jar}}"));
        bapArgs.addParameter(new Parameter(BRANCH_PARAM, "{{item.git-branch}}"));
        bapArgs.addParameter(new Parameter(CCMD_PARAM, "{{item.cmd}}"));
        buildAndPush.setArguments(bapArgs);
        return buildAndPush;
    }

    private Step getModelServingStep() {
        Step modelServing= new Step("model-serving", "serving-template");
        Arguments msArgs = new Arguments();
        msArgs.addParameter(new Parameter(KUBE_PARAM, conf.getKubeWfName()+ "-{{item.image}}-{{item.version}}"));
        msArgs.addParameter(new Parameter(DOCKER_IMAGE_PARAM, "{{item.image}}"));
        msArgs.addParameter(new Parameter(DOCKER_VERS_PARAM, "{{item.vers}}"));
        modelServing.setArguments(msArgs);
        return modelServing;
    }

    // Templates
    private Template getFeatureEngineeringTemplate(Artifact pipelineArtifact, Secret s3AccessSecret, Secret s3SecSecret, List<Env> s3EnvList, Resources btResources) {
        Template feTemplate = new Template("fe-template");
        Inputs feTemplateInputs = new Inputs();
        feTemplateInputs.addParameter(new Parameter(JAR_PARAM));
        feTemplateInputs.addParameter(new Parameter(INPUT_PARAM));
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
        return feTemplate;
    }

    private Template getModelTrainingTemplate(Artifact pipelineArtifact, List<Env> s3EnvList, Resources btResources) {
        Template mtTemplate = new Template("mt-template");
        Inputs mtTemplateInputs = new Inputs();
        mtTemplateInputs.addParameter(new Parameter(JAR_PARAM));
        mtTemplateInputs.addArtifact(pipelineArtifact);
        mtTemplate.setInputs(mtTemplateInputs);

        Container mtContainer = createMTContainer(conf.getTrainingRunner());
        mtContainer.setEnv(s3EnvList);

        mtTemplate.setContainer(mtContainer);
        mtTemplate.setResources(btResources);
        return mtTemplate;
    }

    private Template getBuildPushTemplate(Secret s3AccessSecret, Secret s3SecSecret ) {
        Template bp = new Template("build-push-template");
        Inputs bpInputs = new Inputs();
        bpInputs.addParameter(new Parameter(JAR_PARAM));
        bpInputs.addParameter(new Parameter(BRANCH_PARAM));
        bpInputs.addParameter(new Parameter(CCMD_PARAM));

        Artifact bpJarArtifact = getJarArtifact(s3AccessSecret, s3SecSecret, "/app.jar");
        Git git = new Git("https://github.com/venci6/demos.git", "{{inputs.parameters.branch}}");
        Artifact bpGitArtifact = new Artifact("docker-files", "/docker-files", git);
        bpInputs.addArtifact(bpGitArtifact);
        bpInputs.addArtifact(bpJarArtifact);
        bp.setInputs(bpInputs);


        Container bpContainer = new Container(IMAGE_DOCKER, Arrays.asList("sh", "-c"), Collections.singletonList(BUILD_PUSH_CMD));
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

        return bp;
    }

    private Template getServingTemplate() {
        Template st = new Template("serving-template");
        Inputs stInputs = new Inputs();
        stInputs.addParameter(new Parameter(KUBE_PARAM));
        stInputs.addParameter(new Parameter(DOCKER_IMAGE_PARAM));
        stInputs.addParameter(new Parameter(DOCKER_VERS_PARAM));
        st.setInputs(stInputs);

        Resource r = new Resource("create", KUBE_MANIFEST);
        st.setResource(r);
        return st;
    }

    // Util
    private Artifact getJarArtifact(Secret s3AccessSecret, Secret s3SecSecret, String jarName) {
        S3 s3 = new S3(conf.getS3Endpoint(), conf.getS3Bucket(), "{{inputs.parameters.jar}}", s3AccessSecret, s3SecSecret);

        return new Artifact(JAR_ART, jarName, s3);
    }

    private List<Env> getS3EnvList() {
        Map<String, Secret> s3Access = new HashMap<>();
        s3Access.put("secretKeyRef", new Secret("s3-credentials", "accessKey"));
        Map<String, Secret> s3Secret = new HashMap<>();
        s3Secret.put("secretKeyRef", new Secret("s3-credentials", "secretKey"));
        return Arrays.asList(new Env(S3_ACCESS, s3Access), new Env(S3_SECRET, s3Secret));
    }

    private List<Map<String, String>> createBPItems () {
        List<Map<String,String>> bpItems = new ArrayList<>();

        String modelType = conf.getModelType();

        String columns = modelType.equalsIgnoreCase("sentiment") ? BP_MODEL_SA_PARAMS : "";
        final String branch = "model/" + modelType;

        // Deploy multiple models
        String[] models = conf.getModelPath().split(",");
        int version = 97;

        for(String modelPath : models) {
            char letter = (char) version;
            String cmd = String.format("%s {{workflow.parameters.docker-repo}} model {{workflow.parameters.docker-version}}%c {{workflow.parameters.model-type}} %s", modelPath, letter, columns);

            Map<String, String> modelBuild = ImmutableMap.of("git-branch", branch,
                    JAR_PARAM, conf.getModelJar(),
                    "cmd", cmd);
            bpItems.add(modelBuild);
            version++;
        }


        // Extra services that can be deployed
        if (conf.getEnableStats()) {
            Map<String, String> statsMap = ImmutableMap.of("git-branch", "basic",
                    JAR_PARAM, conf.getStatsJar(),
                    "cmd", BP_STATS_PARAMS);
            bpItems.add(statsMap);
        }

        if (conf.getEnableProcessor()) {
            Map<String, String> processorMap = ImmutableMap.of("git-branch", "basic",
                    JAR_PARAM, conf.getProcessorJar(),
                    "cmd", BP_PROCESS_PARAMS);
            bpItems.add(processorMap);
        }

        return bpItems;
    }

    private List<Map<String, String>> createServingItems() {
        List<Map<String,String>> servingItems = new ArrayList<>();

        String[] models = conf.getModelPath().split(",");
        int version = 97;

        for(String model : models) {
            Map<String, String> modelServeMap = ImmutableMap.of("image", "model",
                    "version", WF_DOCKER_VERS + (char)version);
            servingItems.add(modelServeMap);
            version++;
        }


        if (conf.getEnableStats()) {
            Map<String, String> pcServeMap = ImmutableMap.of("image", "stats",
                    "version", WF_DOCKER_VERS);
            servingItems.add(pcServeMap);
        }

        if (conf.getEnableProcessor()) {
            Map<String, String> processorServeMap = ImmutableMap.of("image", "processor",
                    "version", WF_DOCKER_VERS);
            servingItems.add(processorServeMap);
        }
        return servingItems;
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
