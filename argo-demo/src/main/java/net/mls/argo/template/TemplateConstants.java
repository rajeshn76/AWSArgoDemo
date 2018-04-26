package net.mls.argo.template;

public interface TemplateConstants {

    String FE_CMD_ARGS = "--inputFile={{inputs.parameters.input-path}} --outputFile={{workflow.parameters.features-path}} " +
            "--featureColumns={{workflow.parameters.feature-columns}} --awsRegion=us-east-1 " +
            "--funcName={{inputs.parameters.func-name}}";

    String FE_DIRECT_CMD = "java -cp pipeline.jar:feature-engineering.jar:* net.mls.pipeline.feature.FeaturePipeline " + FE_CMD_ARGS;

    String FE_FLINK_CMD = "cp /feature-engineering.jar lib/feature-engineering.jar && " +
            "bin/start-local.sh && flink run /pipeline.jar --runner=FlinkRunner " + FE_CMD_ARGS;


    String MT_CMD_ARGS = "--inputFile={{workflow.parameters.features-path}} "
            + "--outputFile={{workflow.parameters.model-path}} --modelType={{workflow.parameters.model-type}} "
            + "--featureColumns={{workflow.parameters.feature-columns}} --awsRegion=us-east-1 " ;

    String MT_DIRECT_CMD = "java -jar pipeline.jar " + MT_CMD_ARGS;

    String MT_FLINK_CMD = "bin/start-local.sh && flink run /pipeline.jar --runner=FlinkRunner "
            + MT_CMD_ARGS;

    String MT_SPARK_CMD = "wget -nv -O spark.tgz 'http://ftp.wayne.edu/apache/spark/spark-2.2.1/spark-2.2.1-bin-hadoop2.7.tgz' "
            + "&& tar -xf spark.tgz && cd spark-2.2.1-bin-hadoop2.7 &&  "
            + "bin/spark-submit --master local[2] /pipeline.jar --runner=SparkRunner "
            + MT_CMD_ARGS;


    String BUILD_PUSH_CMD = "cp app.jar docker-files/model-serving.jar ; cd /docker-files ; chmod +x wrap.sh ;" +
            " ./wrap.sh {{workflow.parameters.model-path}} {{workflow.parameters.docker-repo}} {{workflow.parameters.docker-image}} " +
            "{{workflow.parameters.docker-version}} {{workflow.parameters.model-type}}";

    String BUILD_PUSH_STATS_CMD = "cp app.jar docker-files/app.jar ; cd /docker-files ; chmod +x wrap.sh ; ./wrap.sh " +
            "{{workflow.parameters.docker-repo}} {{workflow.parameters.docker-image}} {{workflow.parameters.docker-version}}";

    String BP_SA_PARAMS = " {{workflow.parameters.feature-columns}}";

    String IMAGE_DOCKER = "docker:17.10";
    String IMAGE_JAVA = "java:8";
    String IMAGE_FLINK = "flink:1.4.0";
    String IMAGE_DIND = "docker:17.10-dind";

    String JAR_PARAM = "jar";
    String FEATURES_PARAM = "features-path";
    String MODEL_PATH = "model-path";

    String INPUT_PARAM = "input-path";
    String COLUMNS_PARAM = "feature-columns";
    String FE_JAR_PARAM = "feature-engineering-jar";
    String FUNC_PARAM = "func-name";
    String MODEL_TYPE_PARAM = "model-type";
    String JAR_ART = "jar-artifact";
    String FUNC_ART = "fe-artifact";
    String DOCKER_REPO_PARAM = "docker-repo";
    String DOCKER_IMAGE_PARAM = "docker-image";
    String DOCKER_VERS_PARAM = "docker-version";
    String KUBE_PARAM = "wf-name";

    String S3_ACCESS = "AWS_ACCESS_KEY_ID";
    String S3_SECRET = "AWS_SECRET_ACCESS_KEY";
    String DOCKER_HOST = "DOCKER_HOST";
    String DOCKER_USERNAME = "DOCKER_USERNAME";
    String DOCKER_PASSWORD = "DOCKER_PASSWORD";

    String KUBE_MANIFEST = "---\n" +
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
            "    name: http\n\n" +
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
            "        image: {{workflow.parameters.docker-repo}}/{{workflow.parameters.docker-image}}:{{workflow.parameters.docker-version}}\n" +
            "        ports:\n" +
            "        - containerPort: 8080";

}
