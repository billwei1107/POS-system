/**
 * @file ProductUpdatedEvent.java
 * @description 商品更新事件 / Product updated Spring event
 */
package com.enterprise.product.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class ProductUpdatedEvent extends ApplicationEvent {

    private final UUID productId;
    private final String sku;

    public ProductUpdatedEvent(Object source, UUID productId, String sku) {
        super(source);
        this.productId = productId;
        this.sku = sku;
    }
}
