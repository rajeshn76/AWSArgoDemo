package net.mls.argo.template;

import net.mls.argo.WorkflowConfig;
import net.mls.argo.util.PipelineType;

public interface WorkflowFactory {
    WorkflowSpec create(PipelineType pipelineType, WorkflowConfig config);
}
