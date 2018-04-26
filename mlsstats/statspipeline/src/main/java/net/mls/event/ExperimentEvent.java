package net.mls.event;

import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

@DefaultCoder(AvroCoder.class)
public class ExperimentEvent implements IConverted {

    private String experiment;
    private String variant;
    private boolean isView;

    public ExperimentEvent() {
        super();
    }

    public ExperimentEvent(String experiment, String variant, boolean isView) {
        this.experiment = experiment;
        this.variant = variant;
        this.isView = isView;
    }

    public String getExperiment() {
        return experiment;
    }

    public void setExperiment(String experiment) {
        this.experiment = experiment;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    @Override
    public boolean isView() {
        return isView;
    }

    public void setView(boolean view) {
        isView = view;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExperimentEvent that = (ExperimentEvent) o;

        if (isView != that.isView) return false;
        if (!experiment.equals(that.experiment)) return false;
        if (!variant.equals(that.variant)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result =  experiment.hashCode();
        result = 31 * result + variant.hashCode();
        result = 31 * result + (isView ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ExperimentEvent{" +
                "experiment='" + experiment + '\'' +
                ", variant='" + variant + '\'' +
                ", isView=" + isView +
                '}';
    }
}
