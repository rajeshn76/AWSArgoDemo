package net.mls.argo.template;

import net.mls.argo.template.structure.*;
import org.junit.Test;

import java.util.*;

public class PipelineYamlTest {

    String jarParam = "jar";
    String inputParam = "input-path";
    String outputParam = "output-path";
    String modelParam = "model";
    String dRepoParam = "docker-repo";
    String dImageParam = "docker-image";
    String dVersionParam = "docker-version";
    String wfParam = "wf-name";



    String featureJar = "jars/feature-pipeline-direct.jar";
    String dataPath = "chase_ios_app_reviews-partial.csv";
    String featuresPath = "TestDataModel-direct.csv";

    String learningJar = "jars/learning-pipeline-direct.jar";
    String modelPath = "model/lreg-flink.zip";

    String modelJar = "jars/model-serving.jar";
    String dockerRepo = "pqchat";
    String dockerImage = "model-serving";
    String dockerVersion = "v2";
    String kubeWfName = "model-endpoint2";

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

        Template ft = new Template("feature-training", true);

        Step featureEngineering = new Step("feature-engineering", "beam-template");
        Arguments feArgs =  new Arguments();
        feArgs.addParameter( new Parameter(jarParam, featureJar));
        feArgs.addParameter( new Parameter(inputParam, dataPath));
        feArgs.addParameter( new Parameter(outputParam, featuresPath));
        featureEngineering.setArguments(feArgs);
        ft.addStep(featureEngineering);

        Step modelTraining = new Step("model-training", "beam-template");
        Arguments mtArgs =  new Arguments();
        mtArgs.addParameter( new Parameter(jarParam,learningJar ));
        mtArgs.addParameter( new Parameter(inputParam, featuresPath));
        mtArgs.addParameter( new Parameter(outputParam, modelPath));
        modelTraining.setArguments(mtArgs);
        ft.addStep(modelTraining);

        Step buildAndPush = new Step("build-and-push", "build-push");
        Arguments bapArgs =  new Arguments();
        bapArgs.addParameter( new Parameter(jarParam, modelJar));
        bapArgs.addParameter( new Parameter(modelParam, modelPath));
        bapArgs.addParameter( new Parameter(dRepoParam, dockerRepo));
        bapArgs.addParameter( new Parameter(dImageParam, dockerImage));
        bapArgs.addParameter( new Parameter(dVersionParam, dockerVersion));
        buildAndPush.setArguments(bapArgs);
        ft.addStep(buildAndPush);

        Step modelServing = new Step("model-serving", "serving-template");
        Arguments msArgs =  new Arguments();
        msArgs.addParameter( new Parameter(wfParam, kubeWfName));
        msArgs.addParameter( new Parameter(dRepoParam, dockerRepo));
        msArgs.addParameter( new Parameter(dImageParam, dockerImage));
        msArgs.addParameter( new Parameter(dVersionParam, dockerVersion));
        modelServing.setArguments(msArgs);
        ft.addStep(modelServing);


        Template bt = new Template("beam-template", false);
        Inputs btInputs = new Inputs();
        btInputs.addParameter(new Parameter(jarParam));
        btInputs.addParameter( new Parameter(inputParam));
        btInputs.addParameter( new Parameter(outputParam));

        S3 s3 = new S3(s3Endpoint, s3Bucket, "{{inputs.parameters.jar}}", new Secret("s3-credentials", "accessKey"), new Secret("s3-credentials", "secretKey") );
        Artifact btArtifact = new Artifact("my-art", "/pipeline.jar", s3);
        btInputs.addArtifact(btArtifact);
        bt.setInputs(btInputs);

        Container btContainer = new Container("java:8", Arrays.asList("bash", "-c"), Collections.singletonList("java -jar pipeline.jar --inputFile={{inputs.parameters.input-path}} --outputFile={{inputs.parameters.output-path}}") );
        bt.setContainer(btContainer);

        Resources resources = new Resources(4096l, 0.3f);
        bt.setResources(resources);




        Template bp = new Template("build-push", false);
        Inputs bpInputs = new Inputs();
        bpInputs.addParameter(new Parameter(jarParam));
        bpInputs.addParameter( new Parameter(modelParam));
        bpInputs.addParameter( new Parameter(dRepoParam));
        bpInputs.addParameter( new Parameter(dImageParam));
        bpInputs.addParameter( new Parameter(dVersionParam));

        Artifact bpJarArtifact = new Artifact("jar-artifact", "/model-serving.jar", s3);
        Git git = new Git("https://github.com/venci6/demos.git", "master");
        Artifact bpGitArtifact = new Artifact("docker-files", "/docker-files", git);
        bpInputs.addArtifact(bpGitArtifact);
        bpInputs.addArtifact(bpJarArtifact);
        bp.setInputs(bpInputs);


        String shCmd = "cp model-serving.jar docker-files/model-serving.jar ; cd /docker-files ; chmod +x wrap.sh ; ./wrap.sh {{inputs.parameters.model}} {{inputs.parameters.docker-repo}} {{inputs.parameters.docker-image}} {{inputs.parameters.docker-version}}";
        Container bpContainer = new Container("docker:17.10", Arrays.asList("sh", "-c"), Collections.singletonList(shCmd) );
        Map<String, Secret> userMap = new HashMap<>();
        userMap.put("secretKeyRef", new Secret("docker-credentials", "username"));
        Map<String, Secret> pwMap = new HashMap<>();
        pwMap.put("secretKeyRef", new Secret("docker-credentials", "password"));
        bpContainer.setEnv(Arrays.asList(new Env("DOCKER_HOST", "127.0.0.1"), new Env("DOCKER_USERNAME", userMap), new Env("DOCKER_PASSWORD", pwMap) ));

        bp.setContainer(bpContainer);
        Map<String, Boolean> securityContext = new HashMap<>();
        securityContext.put("privileged", true);
        Sidecar sc = new Sidecar("dind", "docker:17.10-dind",securityContext, true );
        List<Sidecar> sidecars = Collections.singletonList(sc);
        bp.setSidecars(sidecars);

        Template st = new Template("serving-template", false);
        Inputs stInputs = new Inputs();
        stInputs.addParameter( new Parameter("wf-name"));
        stInputs.addParameter( new Parameter("docker-repo"));
        stInputs.addParameter( new Parameter("docker-image"));
        stInputs.addParameter( new Parameter("docker-version"));
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
