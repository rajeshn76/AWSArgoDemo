package net.mls.argo.template;

import net.mls.argo.template.structure.Pipeline;
import net.mls.argo.template.structure.Step;
import net.mls.argo.template.structure.Template;
import org.junit.Test;

public class PipelineYamlTest {

    @Test
    public void printPipeline() throws Exception {
        Pipeline p = createPipeline();
        System.out.println(YAMLGenerator.asYaml(p));
    }

    private Pipeline createPipeline() {
        Pipeline p = new Pipeline();

        Template ft = new Template("feature-training");
        ft.addStep(new Step("feature-engineering", "beam-template"));
        ft.addStep(new Step("model-training", "beam-template"));
        ft.addStep(new Step("model-serving", "serving-template"));
        p.spec.addTemplate(ft);

        return p;
    }
}
