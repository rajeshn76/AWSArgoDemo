package net.mls.argo.service;

import net.mls.argo.operation.WorkflowOperation;
import net.mls.argo.util.WorkflowConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkflowService {

    @Autowired
    private WorkflowConfig config;

    @Autowired
    private WorkflowOperation wfop;

    @RequestMapping(value = "argo", method = RequestMethod.POST)
    public void execute(@RequestBody WorkflowConfig wc) throws Exception {
        wfop.executePipeline(config.merge(wc));
    }
}
