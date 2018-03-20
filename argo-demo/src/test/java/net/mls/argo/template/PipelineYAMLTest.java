package net.mls.argo.template;

import net.mls.argo.ShellCommandExecutor;
import net.mls.argo.YAMLGenerator;
import net.mls.argo.template.structure.Pipeline;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import static org.junit.Assert.*;

public class PipelineYAMLTest {
    @Test
    public void executeGeneratedPipelineYAML() throws Exception {
        Pipeline p = new PipelineYAML().createPipeline();

        String p_yaml = YAMLGenerator.asYaml(p);
        assertNotNull("Generated YAML cannot be null", p_yaml);

        String tmpFileName = RandomStringUtils.randomAlphanumeric(8);
        File yamlFile = File.createTempFile(tmpFileName, ".yaml");
        System.out.println(yamlFile.getAbsolutePath());

        BufferedWriter bw = new BufferedWriter(new FileWriter(yamlFile));
        bw.write(p_yaml);
        bw.close();

        ShellCommandExecutor.execute("argo submit " + yamlFile.getAbsolutePath());
        String output = ShellCommandExecutor.execute("argo list --running");
        System.out.println(output);
        assertTrue(output.contains("mls-pipelines-direct"));
    }
}
