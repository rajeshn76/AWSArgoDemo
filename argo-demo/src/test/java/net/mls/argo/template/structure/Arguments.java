package net.mls.argo.template.structure;

import java.util.ArrayList;
import java.util.List;

public final class Arguments {
    public List<Parameter> parameters = new ArrayList<>();

    public void addParameter(Parameter p) {
        parameters.add(p);
    }
}
