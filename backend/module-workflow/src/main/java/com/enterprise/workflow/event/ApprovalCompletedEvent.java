package com.enterprise.workflow.event;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

public class ApprovalCompletedEvent extends ApplicationEvent {

    private final UUID instanceId;
    private final String businessType;
    private final String businessId;

    public ApprovalCompletedEvent(Object source, UUID instanceId, String businessType, String businessId) {
        super(source);
        this.instanceId = instanceId;
        this.businessType = businessType;
        this.businessId = businessId;
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
}
