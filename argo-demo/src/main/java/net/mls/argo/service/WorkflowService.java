package net.mls.argo.service;

import net.mls.argo.WorkflowConfig;
import net.mls.argo.operation.MLWorkflowOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class WorkflowService {


    private static final Logger LOG = LoggerFactory.getLogger(WorkflowService.class);
    @Autowired
    private WorkflowConfig config;

    @Autowired
    private MLWorkflowOperation mlop;

    @RequestMapping(value = "wf/{pipelineType}/{modelType}", method = RequestMethod.POST)
    public void executeWF(@RequestBody WorkflowConfig wc, @PathVariable String modelType,
                                       @PathVariable String pipelineType) throws Exception {
        LOG.info("Creating {} workflow for {} ", pipelineType, modelType);

        wc.setModelType(modelType);
        wc.setPipelineType(pipelineType);
        mlop.apply(config.mergeWith(wc));
    }

    @RequestMapping(value = "wf", method = RequestMethod.POST)
    public void executeDefaultWF(@RequestBody String modelType) throws Exception {
        LOG.info("Creating default workflow for "+modelType);

        if(modelType.equalsIgnoreCase("recommender")) {
            WorkflowConfig wc = new WorkflowConfig("movielens/100k/u.data",
                    "RatingsDataModel-direct.csv", "user,product,rating",
                    "net.mls.pipeline.feature.recommender.ProcessRatingsFn",
                    "model/recommender-movies", "model-rec-endpoint-", "vrec",
                    "recommender");
            mlop.apply(config.mergeWith(wc));
        } else {
            mlop.apply(config);
        }
    }
}
