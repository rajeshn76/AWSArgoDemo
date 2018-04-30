package net.mls.argo.template;

import net.mls.argo.WorkflowConfig;

public interface WorkflowFactory {
    WorkflowSpec createBuildingAndServing(WorkflowConfig config);
    WorkflowSpec createServingPipeline(WorkflowConfig config);
}
