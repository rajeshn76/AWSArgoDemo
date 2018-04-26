package net.mls.argo.template.structure;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

public final class Step {
    public String name;
    public String template;

    public Step(String name, String template) {
        this.name = name;
        this.template = template;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Arguments arguments;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Map<String, String>> withItems;

    public void setArguments(Arguments args) {
        this.arguments = args;
    }

    public void setItems(List<Map<String, String>> items) {
        this.withItems = items;
    }
}
