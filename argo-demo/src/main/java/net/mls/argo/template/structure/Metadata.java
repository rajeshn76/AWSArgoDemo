package net.mls.argo.template.structure;

public final class Metadata {
   private String generateName;

    public Metadata(String generateName) {
        this.generateName = "mls-pipelines-"+generateName+"-";
    }
}
