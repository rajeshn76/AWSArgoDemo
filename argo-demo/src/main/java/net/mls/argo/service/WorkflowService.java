package net.mls.argo.service;

import net.mls.argo.operation.MLWorkflowOperation;
import net.mls.argo.operation.SentimentAnalysisWorkflowOperation;
import net.mls.argo.WorkflowConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkflowService {


    private static final Logger LOG = LoggerFactory.getLogger(WorkflowService.class);
    @Autowired
    private WorkflowConfig config;

    @Autowired
    private SentimentAnalysisWorkflowOperation wfop;

    @Autowired
    private MLWorkflowOperation mlop;

    @RequestMapping(value = "argo", method = RequestMethod.POST)
    public void execute(@RequestBody WorkflowConfig wc) throws Exception {
        wfop.apply(config.mergeWith(wc));
    }

    @RequestMapping(value = "wf", method = RequestMethod.POST)
    public void executeWF(@RequestBody String modelType) throws Exception {
        LOG.info("Creating workflow for "+modelType);
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
