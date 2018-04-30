package net.mls.stats.domain;

import lombok.Value;
import net.mls.scala.RateInfo;

import java.io.Serializable;

/**
 * Created by char on 4/20/18.
 */
@Value
public class ConfidenceIntervalMapper  implements Serializable{
    private final Double lower;
    private final Double upper;
    private final Double center;
    private final Double error;
    private final String model;
    private final String controlModel;
    private final RateInfo rateInfo;
    private final boolean control;

    public ConfidenceIntervalMapper(Double center, Double error, String model, String controlModel, RateInfo info) {
        this.center = center;
        this.error = error;
        this.rateInfo = info;
        this.lower = center - error;
        this.upper = center + error;
        this.model = model;
        this.controlModel=controlModel;
        this.control = model.equalsIgnoreCase(controlModel);
    }


}
