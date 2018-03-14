package net.mls.argo.template.structure;

import com.fasterxml.jackson.annotation.JsonInclude;

public final class Step {
    public String name;
    public String template;

    public Step(String name, String template) {
        this.name = name;
        this.template = template;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Arguments arguments;

    public void setArguments(Arguments args) {
        this.arguments = args;
    }
}
