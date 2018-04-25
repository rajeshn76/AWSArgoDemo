package net.mls.argo.template.structure;

public final class Metadata {
   public String generateName;

    public Metadata(String generateName) {
        this.generateName = "mls-pipelines-"+generateName+"-";
    }
}
