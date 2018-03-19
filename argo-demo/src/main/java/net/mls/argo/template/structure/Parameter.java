package net.mls.argo.template.structure;


import com.fasterxml.jackson.annotation.JsonInclude;

public final class Parameter {
    public String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String value;

    public Parameter(String name) {
        this.name = name;
    }

    public Parameter(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
