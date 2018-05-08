package net.mls.argo.util;

public enum PipelineType {
    BUILDING ("building"),
    SERVING("serving"),
    BUILDING_SERVING("building-serving");

    private final String type;

    PipelineType(String type) {
        this.type = type;
    }
    public String toString() {
        return this.type;
    }

    public static PipelineType fromString(String type) {
        for (PipelineType t : PipelineType.values()) {
            if (t.type.equalsIgnoreCase(type)) {
                return t;
            }
        }
        return null;
    }
}
