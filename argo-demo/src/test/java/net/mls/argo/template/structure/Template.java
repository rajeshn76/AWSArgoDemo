package net.mls.argo.template.structure;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Template {
    public String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<List<Step>> steps;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Arguments arguments;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Inputs inputs;

    public Template(String name, boolean hasSteps) {
        this.name = name;

        if (hasSteps) {
            steps = new ArrayList<>();
        }
    }

    public void addStep(Step s) {
        steps.add(Arrays.asList(s));
    }

    public void setArguments(Arguments args) {
        this.arguments = args;
    }
    public void setInputs(Inputs inputs) {
        this.inputs = inputs;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Container container;
    public void setContainer(Container container) {
        this.container = container;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Resources resources;
    public void setResources(Resources resources) {
        this.resources = resources;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Sidecar> sidecars;
    public void setSidecars(List<Sidecar>  sidecars) {
        this.sidecars = sidecars;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Resource resource;
    public void setResource(Resource  resource) {
        this.resource = resource;
    }

}
