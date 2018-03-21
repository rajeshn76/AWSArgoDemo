package net.mls.argo.template;

import net.mls.argo.template.structure.Pipeline;
import net.mls.argo.util.WorkflowConfig;
import net.mls.argo.util.YAMLGenerator;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class PipelineYAMLTest {
    @Test
    public void executeGeneratedPipelineYAML() throws Exception {
        Pipeline p = new PipelineYAML().createPipeline(new WorkflowConfig());

        String p_yaml = YAMLGenerator.asYaml(p);
        assertNotNull("Generated YAML cannot be null", p_yaml);
    }
}
