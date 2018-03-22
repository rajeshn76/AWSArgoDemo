package net.mls.argo.template;

import net.mls.argo.WorkflowConfig;
import net.mls.argo.util.YAMLGenerator;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class WorkflowYAMLTest {
    @Test
    public void executeGeneratedPipelineYAML() throws Exception {
        WorkflowSpec p = new SentimentAnalysisWorkflow().create(new WorkflowConfig());

        String p_yaml = YAMLGenerator.asYaml(p);
        assertNotNull("Generated YAML cannot be null", p_yaml);
    }
}
