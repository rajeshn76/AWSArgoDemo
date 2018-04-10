package net.mls.argo.operation;

import net.mls.argo.template.MLWorkflow;
import net.mls.argo.template.WorkflowSpec;
import net.mls.argo.util.ShellCommandExecutor;
import net.mls.argo.WorkflowConfig;
import net.mls.argo.util.YAMLGenerator;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.function.Function;

@Service("mlwfop")
public class MLWorkflowOperation implements Function<WorkflowConfig, Boolean> {

    private static final Logger LOG = LoggerFactory.getLogger(MLWorkflowOperation.class);

    public Boolean apply(WorkflowConfig config) {
        try {
            WorkflowSpec p = new MLWorkflow().create(config);
            String p_yaml = YAMLGenerator.asYaml(p);

            String tmpFileName = RandomStringUtils.randomAlphanumeric(8);
            File yamlFile = File.createTempFile(tmpFileName, ".yaml");
            LOG.info("Generated pipeline -> {}", yamlFile.getAbsolutePath());

            BufferedWriter bw = new BufferedWriter(new FileWriter(yamlFile));
            bw.write(p_yaml);
            bw.close();

            ShellCommandExecutor.execute("argo submit " + yamlFile.getAbsolutePath());

            String output = ShellCommandExecutor.execute("argo list --running");
            LOG.info("Command output:\n {}", output);
            return Boolean.TRUE;
        } catch (Exception e) {
            LOG.error("Failed to execute ML pipeline -> {}",  e.getMessage());
            return Boolean.FALSE;
        }
    }
}
