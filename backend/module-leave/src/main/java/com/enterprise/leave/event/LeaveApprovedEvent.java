/**
 * @file LeaveApprovedEvent.java
 * @description 請假核准事件 / Leave approved Spring event
 * @description_en Published when a leave request is approved via workflow
 * @description_zh 請假申請通過審批後發布的 Spring 事件
 */
package com.enterprise.leave.event;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

public class LeaveApprovedEvent extends ApplicationEvent {

    private final UUID requestId;
    private final UUID employeeId;

    public LeaveApprovedEvent(Object source, UUID requestId, UUID employeeId) {
        super(source);
        this.requestId  = requestId;
        this.employeeId = employeeId;
    }

    public UUID getRequestId()  { return requestId; }
    public UUID getEmployeeId() { return employeeId; }
}
