package net.mls.stats.domain;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by char on 4/23/18.
 */
@Data
@Builder
public class ConfidenceInterval implements Serializable{

    private Double lower;
    private Double upper;
    private Double center;
    private Double error;

    public static ConfidenceInterval map(Double center, Double error, Double lower, Double upper) {
        return ConfidenceInterval.builder()
                .center(center)
                .error(error)
                .lower(lower)
                .upper(upper)
                .build();
    }
}
