package com.enterprise.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * 跨模組共用的通知事件
 * 任何包含 module-common 的業務模組都能直接發出此事件
 */
@Getter
public class SystemNotificationEvent extends ApplicationEvent {
    private final String userId;
    private final String templateCode;
    private final String channel;
    private final String moduleSource;
    private final Map<String, Object> variables;

    public SystemNotificationEvent(Object source, String userId, String templateCode, String channel,
            String moduleSource, Map<String, Object> variables) {
        super(source);
        this.userId = userId;
        this.templateCode = templateCode;
        this.channel = channel;
        this.moduleSource = moduleSource;
        this.variables = variables;
    }
}
