/**
 * @file ProductCreatedEvent.java
 * @description 商品建立事件 / Product created Spring event
 */
package com.enterprise.product.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class ProductCreatedEvent extends ApplicationEvent {

    private final UUID productId;
    private final String sku;

    public ProductCreatedEvent(Object source, UUID productId, String sku) {
        super(source);
        this.productId = productId;
        this.sku = sku;
    }
}
