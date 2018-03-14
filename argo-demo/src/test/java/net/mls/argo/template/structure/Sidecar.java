package net.mls.argo.template.structure;


import java.util.Map;

public class Sidecar {
    public String name;
    public String image;
    public Map<String, Boolean> securityContext;
    public Boolean mirrorVolumeMounts;

    public Sidecar(String name, String image, Map<String, Boolean> securityContext, Boolean mirrorVolumeMounts) {
        this.name = name;
        this.image = image;
        this.securityContext = securityContext;
        this.mirrorVolumeMounts = mirrorVolumeMounts;
    }
}
