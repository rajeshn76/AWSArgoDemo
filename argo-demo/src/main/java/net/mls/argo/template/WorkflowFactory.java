package net.mls.argo.template;

import net.mls.argo.WorkflowConfig;

public interface WorkflowFactory {
    WorkflowSpec create(WorkflowConfig config);
}
