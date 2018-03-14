package net.mls.argo.template.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Template {
    public String name;
    public List<List<Step>> steps = new ArrayList<>();

    public Template(String name) {
        this.name = name;
    }

    public void addStep(Step s) {
        steps.add(Arrays.asList(s));
    }
}
