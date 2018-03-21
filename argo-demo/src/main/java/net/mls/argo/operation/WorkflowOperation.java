package net.mls.argo.operation;

import net.mls.argo.template.PipelineYAML;
import net.mls.argo.template.structure.Pipeline;
import net.mls.argo.util.ShellCommandExecutor;
import net.mls.argo.util.WorkflowConfig;
import net.mls.argo.util.YAMLGenerator;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

@Service("wfop")
public class WorkflowOperation {

    private static final Logger LOG = LoggerFactory.getLogger(WorkflowOperation.class);

    public void executePipeline(WorkflowConfig config) throws Exception {
        Pipeline p = new PipelineYAML().createPipeline(config);
        String p_yaml = YAMLGenerator.asYaml(p);

        String tmpFileName = RandomStringUtils.randomAlphanumeric(8);
        File yamlFile = File.createTempFile(tmpFileName, ".yaml");
        LOG.info("Generated pipeline -> " + yamlFile.getAbsolutePath());

        BufferedWriter bw = new BufferedWriter(new FileWriter(yamlFile));
        bw.write(p_yaml);
        bw.close();

        ShellCommandExecutor.execute("argo submit " + yamlFile.getAbsolutePath());

        String output = ShellCommandExecutor.execute("argo list --running");
        LOG.info(output);
    }
}
