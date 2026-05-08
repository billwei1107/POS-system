/**
 * @file LeaveCancelledEvent.java
 * @description 請假銷假事件 / Leave cancelled Spring event
 * @description_en Published when an approved leave request is cancelled
 * @description_zh 已核准的請假申請被銷假後發布的 Spring 事件
 */
package com.enterprise.leave.event;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

public class LeaveCancelledEvent extends ApplicationEvent {

    private final UUID requestId;
    private final UUID employeeId;

    public LeaveCancelledEvent(Object source, UUID requestId, UUID employeeId) {
        super(source);
        this.requestId  = requestId;
        this.employeeId = employeeId;
    }

    public UUID getRequestId()  { return requestId; }
    public UUID getEmployeeId() { return employeeId; }
}
