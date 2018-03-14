package net.mls.argo.template.structure;

import java.util.ArrayList;
import java.util.List;

public final class Spec {
    public String entrypoint = "feature-training";
    public List<Template> templates = new ArrayList<>();

    public void addTemplate(Template t) {
        templates.add(t);
    }
}
