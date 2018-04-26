package net.mls.performance.repo;


import net.mls.performance.domain.ModelInfo;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository("model")
public interface ModelRepository extends CrudRepository<ModelInfo, String> {
    @Query("select * from model where model_id=?0 allow filtering")
    ModelInfo findByModelID(String modelID);

    @Override
    Collection<ModelInfo> findAll();
}
