package com.enterprise.organization.event;

import com.enterprise.organization.entity.Terminal;
import org.springframework.context.ApplicationEvent;

/**
 * 終端機註冊事件 / Terminal registered event
 */
public class TerminalRegisteredEvent extends ApplicationEvent {
    private final Terminal terminal;

    public TerminalRegisteredEvent(Object source, Terminal terminal) {
        super(source);
        this.terminal = terminal;
    }

    public Terminal getTerminal() { return terminal; }
}
