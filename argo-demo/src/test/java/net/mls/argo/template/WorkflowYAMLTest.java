package net.mls.argo.template;

import net.mls.argo.WorkflowConfig;
import net.mls.argo.util.PipelineType;
import net.mls.argo.util.YAMLGenerator;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class WorkflowYAMLTest {
    @Test
    public void executeGeneratedPipelineYAML() throws Exception {
        WorkflowSpec p = new MLWorkflow().create(PipelineType.BUILDING_SERVING, new WorkflowConfig());

        String p_yaml = YAMLGenerator.asYaml(p);
        assertNotNull("Generated YAML cannot be null", p_yaml);
    }
}
