package net.mls.argo.template.structure;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Inputs {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Parameter> parameters = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Artifact> artifacts;

    public void addParameter(Parameter p) {
        parameters.add(p);
    }

    public void addArtifact(Artifact a) {
        if( !Optional.ofNullable(artifacts).isPresent()) {
            artifacts = new ArrayList<>();
        }
        artifacts.add(a);
    }
}
