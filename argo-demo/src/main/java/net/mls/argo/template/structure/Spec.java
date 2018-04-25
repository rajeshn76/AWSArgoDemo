package net.mls.argo.template.structure;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

public final class Spec {
    public String entrypoint;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Arguments arguments;

    public List<Template> templates = new ArrayList<>();


    public Spec(String entryPoint) {
        this.entrypoint = entryPoint;
    }



    public void addTemplate(Template t) {
        templates.add(t);
    }

    public void setArguments(Arguments arguments) {
        this.arguments = arguments;
    }
}
