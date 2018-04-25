package net.mls.performance.repo;

import net.mls.performance.domain.ModelCount;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

//@Component("count")
public interface CountRepository extends CrudRepository<ModelCount, String> {
    @Query("SELECT sum(count) as sum, model, count, converted, time from model_count where model in (?0) and converted in (false)")
    ModelCount findNonConversionCount(String model);

    @Query("SELECT sum(count) as sum, model, count, converted, time from model_count where model in (?0) and converted in (true)")
    ModelCount findConversionCount(String model);


}
