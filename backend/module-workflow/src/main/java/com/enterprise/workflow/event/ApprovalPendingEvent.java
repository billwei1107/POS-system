package com.enterprise.workflow.event;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

public class ApprovalPendingEvent extends ApplicationEvent {

    private final UUID instanceId;
    private final UUID taskId;
    private final UUID assigneeId;

    public ApprovalPendingEvent(Object source, UUID instanceId, UUID taskId, UUID assigneeId) {
        super(source);
        this.instanceId = instanceId;
        this.taskId = taskId;
        this.assigneeId = assigneeId;
    }

    public UUID getInstanceId() {
        return instanceId;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }
}
