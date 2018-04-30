package net.mls.stats.domain;

import lombok.Builder;
import lombok.Data;
import net.mls.scala.RateStats;

@Builder
@Data
public class Variation {

    private String model;
    private Integer conversion;
    private Integer trials;
    private ConfidenceInterval convidenceInterval;
    private Double change;
    private Double confidence;

    public static Variation map(ConfidenceIntervalMapper mapper, ConfidenceIntervalMapper control) {
        ConfidenceInterval self = ConfidenceInterval.map(mapper.getCenter(), mapper.getError(), mapper.getLower(), mapper.getUpper());
        return Variation.builder()
                .convidenceInterval(self)
                .trials(mapper.getRateInfo().trials())
                .conversion(mapper.getRateInfo().successes())
                .change(calculateChange(mapper.getCenter(), control.getCenter()))
                .confidence(Math.abs(RateStats.pValueFromZ(mapper.getRateInfo(), control.getRateInfo()).confidence()))
                .model(mapper.getModel())
                .build();
    }

    private static Double calculateChange(Double treatment, Double control) {
        return (treatment-control) / control * 100;
    }
}
