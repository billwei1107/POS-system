package com.enterprise.auth.event;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * POS 使用者切換事件 / User switched event on POS terminal
 */
public class UserSwitchedEvent extends ApplicationEvent {

    private final UUID previousUserId;
    private final UUID newUserId;
    private final UUID terminalId;

    public UserSwitchedEvent(Object source, UUID previousUserId, UUID newUserId, UUID terminalId) {
        super(source);
        this.previousUserId = previousUserId;
        this.newUserId = newUserId;
        this.terminalId = terminalId;
    }

    public UUID getPreviousUserId() { return previousUserId; }
    public UUID getNewUserId() { return newUserId; }
    public UUID getTerminalId() { return terminalId; }
}
