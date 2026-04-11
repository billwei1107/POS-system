package com.enterprise.organization.event;

import com.enterprise.organization.entity.Store;
import org.springframework.context.ApplicationEvent;

/**
 * 門店建立事件 / Store created event
 */
public class StoreCreatedEvent extends ApplicationEvent {
    private final Store store;

    public StoreCreatedEvent(Object source, Store store) {
        super(source);
        this.store = store;
    }

    public Store getStore() { return store; }
}
