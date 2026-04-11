package com.enterprise.workflow.event;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

public class ApprovalRejectedEvent extends ApplicationEvent {

    private final UUID instanceId;
    private final String businessType;
    private final String businessId;
    private final String comment;

    public ApprovalRejectedEvent(Object source, UUID instanceId, String businessType, String businessId, String comment) {
        super(source);
        this.instanceId = instanceId;
        this.businessType = businessType;
        this.businessId = businessId;
        this.comment = comment;
    }

    public UUID getInstanceId() {
        return instanceId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public String getBusinessId() {
        return businessId;
    }

    public String getComment() {
        return comment;
    }
}
