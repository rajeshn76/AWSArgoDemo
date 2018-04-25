package net.mls.stats.operation;

import net.mls.stats.domain.ConfidenceIntervalMapper;
import net.mls.performance.domain.ModelCount;
import net.mls.stats.domain.Variation;
import net.mls.performance.repo.CountRepository;
import net.mls.scala.RateInfo;
import net.mls.scala.RateStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

//@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service("ab-op")
public class GetComparisonOperation implements Function<List<String>, List<Variation>> {
//    @NonNull
    private final CountRepository countRepo;

    @Autowired
    public GetComparisonOperation(CountRepository countRepo) {
        this.countRepo = countRepo;
    }

    @Override
    public List<Variation> apply(List<String> strings) {
        try {
            List<ConfidenceIntervalMapper> iMapper = getTotals(strings).get();
            return iMapper.stream()
                    .filter(ConfidenceIntervalMapper::isControl)
                    .flatMap(control -> iMapper.stream()
                    .map(treatment -> Variation.map(treatment, control)))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    CompletableFuture<List<ConfidenceIntervalMapper>> getTotals(List<String> modelIds) {

        List<CompletableFuture<ConfidenceIntervalMapper>> mapped = modelIds.stream()
                .map(model -> getConversions(model).thenCombineAsync(getNonConversions(model), (a,b) ->
                        getConfidenceIntervalMapper(a.getSum().intValue()+b.getSum().intValue(), a.getSum().intValue(), model, modelIds.get(0)))).collect(Collectors.toList());

        return sequence(mapped);
    }


    private ConfidenceIntervalMapper getConfidenceIntervalMapper(int trials, int successes, String model, String controlModel) {
        System.out.println(String.format("For model %s, %d total trials and %d successes", model, trials, successes));
        RateInfo type = new RateInfo(trials, successes);
        ConfidenceIntervalMapper mapper = RateStats.confidenceInterval(type, 0.95, model, controlModel);
        System.out.println(String.format("Model: %s Control: %s Mapper: %s", model, controlModel, mapper.toString()));
        return mapper;
    }


    private CompletableFuture<ModelCount> getNonConversions(String model) {
        return CompletableFuture.supplyAsync(() -> countRepo.findNonConversionCount(model));

    }

    private CompletableFuture<ModelCount> getConversions(String model) {
        System.out.print(model);
        return CompletableFuture.supplyAsync(() -> countRepo.findConversionCount(model));
    }


    private static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        return allDoneFuture.thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
    }


}
