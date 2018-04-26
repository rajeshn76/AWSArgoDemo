package net.mls.store;

import com.datastax.driver.mapping.annotations.Table;

import java.io.Serializable;
import java.util.Date;

@Table(name = "experiment_count")
public class EventCount implements Serializable {
    private Date time;
    private String experiment;
    private String variant;
    private Float count;
    private Boolean converted;

    public EventCount(Date time, String experiment, String variant, Float count, Boolean converted) {
        this.time = time;
        this.experiment = experiment;
        this.variant = variant;
        this.count = count;
        this.converted = converted;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
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

    public Float getCount() {
        return count;
    }

    public void setCount(Float count) {
        this.count = count;
    }

    public Boolean getConverted() {
        return converted;
    }

    public void setConverted(Boolean converted) {
        this.converted = converted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventCount that = (EventCount) o;

        if (time != null ? !time.equals(that.time) : that.time != null) return false;
        if (experiment != null ? !experiment.equals(that.experiment) : that.experiment != null) return false;
        if (variant != null ? !variant.equals(that.variant) : that.variant != null) return false;
        if (count != null ? !count.equals(that.count) : that.count != null) return false;
        return converted != null ? converted.equals(that.converted) : that.converted == null;
    }

    @Override
    public int hashCode() {
        int result = time != null ? time.hashCode() : 0;
        result = 31 * result + (experiment != null ? experiment.hashCode() : 0);
        result = 31 * result + (variant != null ? variant.hashCode() : 0);
        result = 31 * result + (count != null ? count.hashCode() : 0);
        result = 31 * result + (converted != null ? converted.hashCode() : 0);
        return result;
    }
}
