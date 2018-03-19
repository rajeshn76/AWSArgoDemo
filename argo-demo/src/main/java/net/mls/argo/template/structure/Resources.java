package net.mls.argo.template.structure;

public final class Resources {
    public Long mem_mib;
    public Float cpu_cores;

    public Resources(Long mem_mib, Float cpu_cores) {
        this.mem_mib = mem_mib;
        this.cpu_cores = cpu_cores;
    }
}
