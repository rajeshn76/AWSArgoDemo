package net.mls.argo.template;

import net.mls.argo.template.structure.*;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import java.util.*;

public class PipelineYamlTest {

    static final String BEAM_DIRECT_CMD = "java -jar pipeline.jar --inputFile={{inputs.parameters.input-path}} "
                                                                + "--outputFile={{inputs.parameters.output-path}}";

    static final String BEAM_FLINK_CMD = "bin/start-local.sh && flink run pipeline.jar --runner=FlinkRunner "
                                                                + "--inputFile={{inputs.parameters.input-path}} "
                                                                + "--outputFile={{inputs.parameters.output-path}} ";

    static final String BEAM_SPARK_CMD = "wget -nv -O spark.tgz 'http://ftp.wayne.edu/apache/spark/spark-2.2.1/spark-2.2.1-bin-hadoop2.7.tgz' "
                                            + "&& tar -xf spark.tgz && cd spark-2.2.1-bin-hadoop2.7 &&  "
                                            + "bin/spark-submit --master local[2] pipeline.jar --runner=SparkRunner "
                                                                + "--inputFile={{inputs.parameters.input-path}} "
                                                                + "--outputFile={{inputs.parameters.output-path}} ";

    static final String BUILD_PUSH_CMD = "cp model-serving.jar docker-files/model-serving.jar ; cd /docker-files ; chmod +x wrap.sh ; ./wrap.sh {{inputs.parameters.model}} {{inputs.parameters.docker-repo}} {{inputs.parameters.docker-image}} {{inputs.parameters.docker-version}}";

    static final String IMAGE_DOCKER = "docker:17.10";
    static final String IMAGE_JAVA = "java:8";
    static final String IMAGE_FLINK = "flink:1.4.0";
    static final String IMAGE_DIND = "docker:17.10-dind";

    static final String JAR_PARAM = "jar";
    static final String INPUT_PARAM = "input-path";
    static final String OUTPUT_PARAM = "output-path";
    static final String MODEL_PARAM = "model";
    static final String JAR_ART = "jar-artifact";
    static final String DOCKER_REPO_PARAM = "docker-repo";
    static final String DOCKER_IMAGE_PARAM = "docker-image";
    static final String DOCKER_VERS_PARAM = "docker-version";
    static final String KUBE_PARAM = "wf-name";
    public static final String DOCKER_HOST = "DOCKER_HOST";
    public static final String DOCKER_USERNAME = "DOCKER_USERNAME";
    public static final String DOCKER_PASSWORD = "DOCKER_PASSWORD";


    String featureJar = "jars/feature-pipeline-direct.jar";
    String dataPath = "chase_ios_app_reviews-partial.csv";
    String featuresPath = "TestDataModel-direct.csv";

    String learningJar = "jars/learning-pipeline-direct.jar";
    String modelPath = "model/lreg-flink.zip";

    String modelJar = "jars/model-serving.jar";
    String dockerRepo = "pqchat";
    String dockerImage = "model-serving";
    String dockerVersion = "v2";
    String kubeWfName = "model-endpoint-" + RandomStringUtils.randomAlphanumeric(5);

    String s3Endpoint = "s3.amazonaws.com";
    String s3Bucket = "argo-flow";

    @Test
    public void printPipeline() throws Exception {
        Pipeline p = createPipeline();
        System.out.println(YAMLGenerator.asYaml(p));
    }

    private Pipeline createPipeline() {
        String kubeManifest = "---\n" +
                "apiVersion: v1\n" +
                "kind: Service\n" +
                "metadata:\n" +
                "  name: {{inputs.parameters.wf-name}}\n" +
                "  labels:\n" +
                "    app: {{inputs.parameters.wf-name}}\n" +
                "spec:\n" +
                "  type: LoadBalancer\n" +
                "  selector:\n" +
                "    app: {{inputs.parameters.wf-name}}\n" +
                "  ports:\n" +
                "  - protocol: TCP\n" +
                "    port: 8080\n" +
                "    name: http\n" +
                "\n" +
                "---\n" +
                "apiVersion: v1\n" +
                "kind: ReplicationController\n" +
                "metadata:\n" +
                "  name: {{inputs.parameters.wf-name}}\n" +
                "spec:\n" +
                "  replicas: 1\n" +
                "  template:\n" +
                "    metadata:\n" +
                "      labels:\n" +
                "        app: {{inputs.parameters.wf-name}}\n" +
                "    spec:\n" +
                "      containers:\n" +
                "      - name: {{inputs.parameters.wf-name}}\n" +
                "        image: {{inputs.parameters.docker-repo}}/{{inputs.parameters.docker-image}}:{{inputs.parameters.docker-version}}\n" +
                "        ports:\n" +
                "        - containerPort: 8080";
        Pipeline p = new Pipeline();

        Template ft = new Template("feature-training");

        Step featureEngineering = new Step("feature-engineering", "beam-template");
        Arguments feArgs = new Arguments();
        feArgs.addParameter(new Parameter(JAR_PARAM, featureJar));
        feArgs.addParameter(new Parameter(INPUT_PARAM, dataPath));
        feArgs.addParameter(new Parameter(OUTPUT_PARAM, featuresPath));
        featureEngineering.setArguments(feArgs);
        ft.addStep(featureEngineering);

        Step modelTraining = new Step("model-training", "beam-template");
        Arguments mtArgs = new Arguments();
        mtArgs.addParameter(new Parameter(JAR_PARAM, learningJar));
        mtArgs.addParameter(new Parameter(INPUT_PARAM, featuresPath));
        mtArgs.addParameter(new Parameter(OUTPUT_PARAM, modelPath));
        modelTraining.setArguments(mtArgs);
        ft.addStep(modelTraining);

        Step buildAndPush = new Step("build-and-push", "build-push");
        Arguments bapArgs = new Arguments();
        bapArgs.addParameter(new Parameter(JAR_PARAM, modelJar));
        bapArgs.addParameter(new Parameter(MODEL_PARAM, modelPath));
        bapArgs.addParameter(new Parameter(DOCKER_REPO_PARAM, dockerRepo));
        bapArgs.addParameter(new Parameter(DOCKER_IMAGE_PARAM, dockerImage));
        bapArgs.addParameter(new Parameter(DOCKER_VERS_PARAM, dockerVersion));
        buildAndPush.setArguments(bapArgs);
        ft.addStep(buildAndPush);

        Step modelServing = new Step("model-serving", "serving-template");
        Arguments msArgs = new Arguments();
        msArgs.addParameter(new Parameter(KUBE_PARAM, kubeWfName));
        msArgs.addParameter(new Parameter(DOCKER_REPO_PARAM, dockerRepo));
        msArgs.addParameter(new Parameter(DOCKER_IMAGE_PARAM, dockerImage));
        msArgs.addParameter(new Parameter(DOCKER_VERS_PARAM, dockerVersion));
        modelServing.setArguments(msArgs);
        ft.addStep(modelServing);


        Template bt = new Template("beam-template");
        Inputs btInputs = new Inputs();
        btInputs.addParameter(new Parameter(JAR_PARAM));
        btInputs.addParameter(new Parameter(INPUT_PARAM));
        btInputs.addParameter(new Parameter(OUTPUT_PARAM));

        S3 s3 = new S3(s3Endpoint, s3Bucket, "{{inputs.parameters.jar}}", new Secret("s3-credentials", "accessKey"), new Secret("s3-credentials", "secretKey"));
        Artifact btArtifact = new Artifact(JAR_ART, "/pipeline.jar", s3);
        btInputs.addArtifact(btArtifact);
        bt.setInputs(btInputs);

//        For DirectRunner
//        Container btContainerDirect = new Container(IMAGE_JAVA, Arrays.asList("bash", "-c"), Collections.singletonList(BEAM_DIRECT_CMD));
//        bt.setContainer(btContainerDirect);

//        For SparkRunner
//        Container btContainerSpark = new Container(IMAGE_JAVA, Arrays.asList("bash", "-c"), Collections.singletonList(BEAM_SPARK_CMD));
//        bt.setContainer(btContainerSpark);

//        For FlinkRunner
        Container btContainerFlink = new Container(IMAGE_FLINK, Arrays.asList("bash", "-c"), Collections.singletonList(BEAM_FLINK_CMD));
        bt.setContainer(btContainerFlink);

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

        Resource r = new Resource("create", kubeManifest);
        st.setResource(r);


        p.spec.addTemplate(ft);
        p.spec.addTemplate(bt);
        p.spec.addTemplate(bp);
        p.spec.addTemplate(st);

        return p;
    }
}
