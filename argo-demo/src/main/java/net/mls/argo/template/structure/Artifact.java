package net.mls.argo.template.structure;

import com.fasterxml.jackson.annotation.JsonInclude;

public final class Artifact {
    public String name;
    public String path;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public S3 s3;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Git git;


    public Artifact(String name, String path, S3 s3) {
        this.name = name;
        this.path = path;
        this.s3 = s3;
    }

    public Artifact(String name, String path, Git git) {
        this.name = name;
        this.path = path;
        this.git = git;
    }
}
