package net.mls.argo.template.structure;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

public final class Inputs {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Parameter> parameters = new ArrayList<>();
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Artifact> artifacts = new ArrayList<>();
    public void addParameter(Parameter p) {
        parameters.add(p);
    }
    public void addArtifact(Artifact a) {
        artifacts.add(a);
    }
}
