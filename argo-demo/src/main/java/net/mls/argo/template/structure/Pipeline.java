package net.mls.argo.template.structure;

public final class Pipeline {
    public String apiVersion = "argoproj.io/v1alpha1";
    public String kind = "Workflow";

    public Metadata metadata = new Metadata();

    public Spec spec = new Spec();
}
