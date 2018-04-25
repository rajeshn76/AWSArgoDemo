package net.mls.performance.collector.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import net.mls.performance.domain.ModelInfo;

import java.util.Map;

@Builder
@Value
@AllArgsConstructor
public class PerformanceResults {
    ModelInfo model;
    Map<String, Float> counts;

}
