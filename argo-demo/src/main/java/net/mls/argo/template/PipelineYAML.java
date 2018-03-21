package net.mls.argo.template;

import net.mls.argo.template.structure.*;
import net.mls.argo.util.WorkflowConfig;
import org.apache.commons.lang.RandomStringUtils;

import java.util.*;

import static net.mls.argo.template.ParamConstants.*;

public final class PipelineYAML {

    public Pipeline createPipeline(WorkflowConfig conf) {
        Pipeline p = new Pipeline();

        Template ft = new Template("feature-training");

        Step featureEngineering = new Step("feature-engineering", "beam-template");
        Arguments feArgs = new Arguments();
        feArgs.addParameter(new Parameter(JAR_PARAM, conf.getFeatureJar()));
        feArgs.addParameter(new Parameter(INPUT_PARAM, conf.getDataPath()));
        feArgs.addParameter(new Parameter(OUTPUT_PARAM, conf.getFeaturesPath()));
        featureEngineering.setArguments(feArgs);
        ft.addStep(featureEngineering);

        Step modelTraining = new Step("model-training", "beam-template");
        Arguments mtArgs = new Arguments();
        mtArgs.addParameter(new Parameter(JAR_PARAM, conf.getLearningJar()));
        mtArgs.addParameter(new Parameter(INPUT_PARAM, conf.getFeaturesPath()));
        mtArgs.addParameter(new Parameter(OUTPUT_PARAM, conf.getModelPath()));
        modelTraining.setArguments(mtArgs);
        ft.addStep(modelTraining);

        Step buildAndPush = new Step("build-and-push", "build-push");
        Arguments bapArgs = new Arguments();
        bapArgs.addParameter(new Parameter(JAR_PARAM, conf.getModelJar()));
        bapArgs.addParameter(new Parameter(MODEL_PARAM, conf.getModelPath()));
        bapArgs.addParameter(new Parameter(DOCKER_REPO_PARAM, conf.getDockerRepo()));
        bapArgs.addParameter(new Parameter(DOCKER_IMAGE_PARAM, conf.getDockerImage()));
        bapArgs.addParameter(new Parameter(DOCKER_VERS_PARAM, conf.getDockerVersion()));
        buildAndPush.setArguments(bapArgs);
        ft.addStep(buildAndPush);

        Step modelServing = new Step("model-serving", "serving-template");
        Arguments msArgs = new Arguments();
        msArgs.addParameter(new Parameter(KUBE_PARAM, conf.getKubeWfName()
                + RandomStringUtils.randomAlphanumeric(5).toLowerCase()));
        msArgs.addParameter(new Parameter(DOCKER_REPO_PARAM, conf.getDockerRepo()));
        msArgs.addParameter(new Parameter(DOCKER_IMAGE_PARAM, conf.getDockerImage()));
        msArgs.addParameter(new Parameter(DOCKER_VERS_PARAM, conf.getDockerVersion()));
        modelServing.setArguments(msArgs);
        ft.addStep(modelServing);


        Template bt = new Template("beam-template");
        Inputs btInputs = new Inputs();
        btInputs.addParameter(new Parameter(JAR_PARAM));
        btInputs.addParameter(new Parameter(INPUT_PARAM));
        btInputs.addParameter(new Parameter(OUTPUT_PARAM));

        S3 s3 = new S3(conf.getS3Endpoint(), conf.getS3Bucket(), "{{inputs.parameters.jar}}", new Secret("s3-credentials", "accessKey"), new Secret("s3-credentials", "secretKey"));
        Artifact btArtifact = new Artifact(JAR_ART, "/pipeline.jar", s3);
        btInputs.addArtifact(btArtifact);
        bt.setInputs(btInputs);

//        For DirectRunner
        Container btContainerDirect = new Container(IMAGE_JAVA, Arrays.asList("bash", "-c"), Collections.singletonList(BEAM_DIRECT_CMD));
        bt.setContainer(btContainerDirect);

//        For SparkRunner
//        Container btContainerSpark = new Container(IMAGE_JAVA, Arrays.asList("bash", "-c"), Collections.singletonList(BEAM_SPARK_CMD));
//        bt.setContainer(btContainerSpark);

//        For FlinkRunner
//        Container btContainerFlink = new Container(IMAGE_FLINK, Arrays.asList("bash", "-c"), Collections.singletonList(BEAM_FLINK_CMD));
//        bt.setContainer(btContainerFlink);

        Resources resources = new Resources(4096L, 0.3f);
        bt.setResources(resources);


        Template bp = new Template("build-push");
        Inputs bpInputs = new Inputs();
        bpInputs.addParameter(new Parameter(JAR_PARAM));
        bpInputs.addParameter(new Parameter(MODEL_PARAM));
        bpInputs.addParameter(new Parameter(DOCKER_REPO_PARAM));
        bpInputs.addParameter(new Parameter(DOCKER_IMAGE_PARAM));
        bpInputs.addParameter(new Parameter(DOCKER_VERS_PARAM));

        Artifact bpJarArtifact = new Artifact(JAR_ART, "/model-serving.jar", s3);
        Git git = new Git("https://github.com/venci6/demos.git", "master");
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
        p.spec.addTemplate(bt);
        p.spec.addTemplate(bp);
        p.spec.addTemplate(st);

        return p;
    }
}
