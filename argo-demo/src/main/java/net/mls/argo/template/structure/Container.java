package net.mls.argo.template.structure;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public final class Container {
    public String image;
    public List<String> command;
    public List<String> args;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Env> env;



    public Container(String image, List<String> command, List<String> args) {
        this.image = image;
        this.command = command;
        this.args = args;
    }

    public void setEnv(List<Env> env) {
        this.env = env;
    }
}
