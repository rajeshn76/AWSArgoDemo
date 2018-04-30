package net.mls.stats.operation;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.mls.performance.domain.ModelCount;
import net.mls.performance.domain.ModelInfo;
import net.mls.stats.domain.PerformanceResults;
import net.mls.performance.repo.CountRepository;

import net.mls.performance.repo.ModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service("model-op")
public class GetResultsOperation implements Function<String, PerformanceResults> {

    @NonNull
    private final ModelRepository modelRepo;

    @NonNull
    private final CountRepository countRepo;

    @Override
    public PerformanceResults apply(String s) {
        Map<String, Float> map = new HashMap<>();
        ModelCount conversions = countRepo.findConversionCount(s);
        map.put("conversions", conversions.getSum());
        ModelCount views = countRepo.findNonConversionCount(s);
        map.put("views", views.getSum()+conversions.getSum());

        ModelInfo modelInfo = modelRepo.findByModelID(s);

        return new PerformanceResults(modelInfo, map);
    }
}
