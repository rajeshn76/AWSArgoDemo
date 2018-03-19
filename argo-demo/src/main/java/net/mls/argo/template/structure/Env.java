package net.mls.argo.template.structure;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

public class Env {
    public String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String value;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Map<String, Secret> valueFrom;


    public Env(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Env(String name, Map<String, Secret> valueFrom) {
        this.name = name;
        this.valueFrom = valueFrom;
    }
}
