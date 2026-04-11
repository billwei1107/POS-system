package com.enterprise.organization.event;

import com.enterprise.organization.entity.Store;
import org.springframework.context.ApplicationEvent;

/**
 * 門店更新事件 / Store updated event
 */
public class StoreUpdatedEvent extends ApplicationEvent {
    private final Store store;

    public StoreUpdatedEvent(Object source, Store store) {
        super(source);
        this.store = store;
    }

    public Store getStore() { return store; }
}
