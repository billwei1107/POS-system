package com.enterprise.auth.event;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * 店長權限覆蓋事件 / Manager override event for audit trail
 */
public class ManagerOverrideEvent extends ApplicationEvent {

    private final UUID managerId;
    private final String action;
    private final UUID targetOrderId;

    public ManagerOverrideEvent(Object source, UUID managerId, String action, UUID targetOrderId) {
        super(source);
        this.managerId = managerId;
        this.action = action;
        this.targetOrderId = targetOrderId;
    }

    public UUID getManagerId() { return managerId; }
    public String getAction() { return action; }
    public UUID getTargetOrderId() { return targetOrderId; }
}
