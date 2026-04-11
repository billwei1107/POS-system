package com.enterprise.organization.event;

import com.enterprise.organization.entity.Terminal;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 終端機離線事件 / Terminal went offline event
 */
public class TerminalOfflineEvent extends ApplicationEvent {
    private final Terminal terminal;
    private final LocalDateTime lastSeen;

    public TerminalOfflineEvent(Object source, Terminal terminal, LocalDateTime lastSeen) {
        super(source);
        this.terminal = terminal;
        this.lastSeen = lastSeen;
    }

    public Terminal getTerminal() { return terminal; }
    public LocalDateTime getLastSeen() { return lastSeen; }
}
