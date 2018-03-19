package net.mls.argo.template.structure;


public class Git {
    public String repo;
    public String revision;

    public Git(String repo, String revision) {
        this.repo = repo;
        this.revision = revision;
    }
}
