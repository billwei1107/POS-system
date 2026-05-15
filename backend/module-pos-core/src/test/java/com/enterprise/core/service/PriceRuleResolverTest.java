/**
 * @file PriceRuleResolverTest.java
 * @description 價格規則解析器測試 / Price rule resolver tests
 * @description_en Verifies effective order item pricing from product master data and price rules
 * @description_zh 驗證訂單明細會依商品主檔與價格規則重新定價
 */
package com.enterprise.core.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.core.dto.request.OrderItemRequest;
import com.enterprise.product.entity.PriceRule;
import com.enterprise.product.entity.ProductItem;
import com.enterprise.product.repository.PriceRuleRepository;
import com.enterprise.product.repository.ProductItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceRuleResolverTest {

    @Mock
    private ProductItemRepository productItemRepository;

    @Mock
    private PriceRuleRepository priceRuleRepository;

    private PriceRuleResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new PriceRuleResolver(productItemRepository, priceRuleRepository);
    }

    // ========================================
    // 價格規則套用 / Price rule application
    // ========================================
    @Test
    void resolveOrderItems_memberOrder_usesBestActiveApplicableRule() {
        UUID itemId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        LocalDateTime orderedAt = LocalDateTime.of(2026, 5, 14, 12, 0);
        ProductItem product = product(itemId, "Americano", "COF-001", "120.00", true, true);
        OrderItemRequest request = request(itemId, "999.00", "2.00");

        when(productItemRepository.findById(itemId)).thenReturn(Optional.of(product));
        when(priceRuleRepository.findByItemIdAndStoreIdIsNullAndActiveTrue(itemId))
                .thenReturn(List.of(rule(itemId, null, PriceRule.PriceType.MEMBER, "95.00", 1, orderedAt.minusDays(1), orderedAt.plusDays(1))));
        when(priceRuleRepository.findByItemIdAndStoreIdAndActiveTrue(itemId, storeId))
                .thenReturn(List.of(rule(itemId, storeId, PriceRule.PriceType.BULK, "88.00", 2, orderedAt.minusDays(1), orderedAt.plusDays(1))));

        List<OrderItemRequest> result = resolver.resolveOrderItems(List.of(request), storeId, memberId, orderedAt);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().itemNameSnapshot()).isEqualTo("Americano");
        assertThat(result.getFirst().skuSnapshot()).isEqualTo("COF-001");
        assertThat(result.getFirst().unitPrice()).isEqualByComparingTo("88.00");
    }

    @Test
    void resolveOrderItems_nonMemberOrder_ignoresMemberAndStaffRules() {
        UUID itemId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        LocalDateTime orderedAt = LocalDateTime.of(2026, 5, 14, 12, 0);
        ProductItem product = product(itemId, "Latte", "COF-002", "130.00", true, true);
        OrderItemRequest request = request(itemId, "999.00", "1.00");

        when(productItemRepository.findById(itemId)).thenReturn(Optional.of(product));
        when(priceRuleRepository.findByItemIdAndStoreIdIsNullAndActiveTrue(itemId))
                .thenReturn(List.of(rule(itemId, null, PriceRule.PriceType.MEMBER, "80.00", 1, null, null)));
        when(priceRuleRepository.findByItemIdAndStoreIdAndActiveTrue(itemId, storeId))
                .thenReturn(List.of(rule(itemId, storeId, PriceRule.PriceType.STAFF, "60.00", 1, null, null)));

        List<OrderItemRequest> result = resolver.resolveOrderItems(List.of(request), storeId, null, orderedAt);

        assertThat(result.getFirst().unitPrice()).isEqualByComparingTo("130.00");
    }

    @Test
    void resolveOrderItems_unsellableProduct_throwsBusinessException() {
        UUID itemId = UUID.randomUUID();
        ProductItem product = product(itemId, "Hidden Item", "HID-001", "50.00", true, false);
        when(productItemRepository.findById(itemId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> resolver.resolveOrderItems(
                List.of(request(itemId, "50.00", "1.00")),
                UUID.randomUUID(),
                null,
                LocalDateTime.now()
        )).isInstanceOf(BusinessException.class)
          .hasMessageContaining("not sellable");
    }

    private ProductItem product(UUID id,
                                String name,
                                String sku,
                                String basePrice,
                                boolean active,
                                boolean sellable) {
        ProductItem product = new ProductItem();
        product.setId(id);
        product.setName(name);
        product.setSku(sku);
        product.setBasePrice(new BigDecimal(basePrice));
        product.setActive(active);
        product.setSellable(sellable);
        return product;
    }

    private PriceRule rule(UUID itemId,
                           UUID storeId,
                           PriceRule.PriceType priceType,
                           String price,
                           int minQty,
                           LocalDateTime effectiveFrom,
                           LocalDateTime effectiveTo) {
        PriceRule rule = new PriceRule();
        rule.setItemId(itemId);
        rule.setStoreId(storeId);
        rule.setPriceType(priceType);
        rule.setPrice(new BigDecimal(price));
        rule.setMinQty(minQty);
        rule.setEffectiveFrom(effectiveFrom);
        rule.setEffectiveTo(effectiveTo);
        rule.setActive(true);
        return rule;
    }

    private OrderItemRequest request(UUID itemId, String unitPrice, String quantity) {
        return new OrderItemRequest(
                itemId,
                null,
                "Client Snapshot",
                "CLIENT-SKU",
                new BigDecimal(unitPrice),
                new BigDecimal(quantity),
                BigDecimal.ZERO,
                null,
                List.of()
        );
    }
}
