package net.mls.argo.template;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.mls.argo.template.structure.*;
import org.apache.commons.lang.RandomStringUtils;

import java.util.*;

import static net.mls.argo.template.ParamConstants.*;

public final class PipelineYAML {

    private static Config conf = ConfigFactory.load();

    public Pipeline createPipeline() throws Exception {
        Pipeline p = new Pipeline();

        Template ft = new Template("feature-training");

        Step featureEngineering = new Step("feature-engineering", "beam-template");
        Arguments feArgs = new Arguments();
        feArgs.addParameter(new Parameter(JAR_PARAM, conf.getString("featureJar")));
        feArgs.addParameter(new Parameter(INPUT_PARAM, conf.getString("dataPath")));
        feArgs.addParameter(new Parameter(OUTPUT_PARAM, conf.getString("featuresPath")));
        featureEngineering.setArguments(feArgs);
        ft.addStep(featureEngineering);

        Step modelTraining = new Step("model-training", "beam-template");
        Arguments mtArgs = new Arguments();
        mtArgs.addParameter(new Parameter(JAR_PARAM, conf.getString("learningJar")));
        mtArgs.addParameter(new Parameter(INPUT_PARAM, conf.getString("featuresPath")));
        mtArgs.addParameter(new Parameter(OUTPUT_PARAM, conf.getString("modelPath")));
        modelTraining.setArguments(mtArgs);
        ft.addStep(modelTraining);

        Step buildAndPush = new Step("build-and-push", "build-push");
        Arguments bapArgs = new Arguments();
        bapArgs.addParameter(new Parameter(JAR_PARAM, conf.getString("modelJar")));
        bapArgs.addParameter(new Parameter(MODEL_PARAM, conf.getString("modelPath")));
        bapArgs.addParameter(new Parameter(DOCKER_REPO_PARAM, conf.getString("dockerRepo")));
        bapArgs.addParameter(new Parameter(DOCKER_IMAGE_PARAM, conf.getString("dockerImage")));
        bapArgs.addParameter(new Parameter(DOCKER_VERS_PARAM, conf.getString("dockerVersion")));
        buildAndPush.setArguments(bapArgs);
        ft.addStep(buildAndPush);

        Step modelServing = new Step("model-serving", "serving-template");
        Arguments msArgs = new Arguments();
        msArgs.addParameter(new Parameter(KUBE_PARAM, conf.getString("kubeWfName") + RandomStringUtils.randomAlphanumeric(5)));
        msArgs.addParameter(new Parameter(DOCKER_REPO_PARAM, conf.getString("dockerRepo")));
        msArgs.addParameter(new Parameter(DOCKER_IMAGE_PARAM, conf.getString("dockerImage")));
        msArgs.addParameter(new Parameter(DOCKER_VERS_PARAM, conf.getString("dockerVersion")));
        modelServing.setArguments(msArgs);
        ft.addStep(modelServing);


        Template bt = new Template("beam-template");
        Inputs btInputs = new Inputs();
        btInputs.addParameter(new Parameter(JAR_PARAM));
        btInputs.addParameter(new Parameter(INPUT_PARAM));
        btInputs.addParameter(new Parameter(OUTPUT_PARAM));

        S3 s3 = new S3(conf.getString("s3Endpoint"), conf.getString("s3Bucket"), "{{inputs.parameters.jar}}", new Secret("s3-credentials", "accessKey"), new Secret("s3-credentials", "secretKey"));
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

        Resources resources = new Resources(4096l, 0.3f);
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
