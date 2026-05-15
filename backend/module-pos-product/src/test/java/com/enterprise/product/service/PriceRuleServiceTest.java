/**
 * @file PriceRuleServiceTest.java
 * @description 價格規則服務測試 / Price rule service tests
 * @description_en Verifies price rule validation and persistence behavior
 * @description_zh 驗證價格規則驗證與儲存行為
 */
package com.enterprise.product.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.product.dto.PriceRuleRequest;
import com.enterprise.product.dto.PriceRuleResponse;
import com.enterprise.product.entity.PriceRule;
import com.enterprise.product.repository.PriceRuleRepository;
import com.enterprise.product.repository.ProductItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceRuleServiceTest {

    @Mock private PriceRuleRepository priceRuleRepository;
    @Mock private ProductItemRepository productItemRepository;

    @Test
    void create_persistsDefaultMinimumQuantityAndActiveFlag() {
        PriceRuleService service = new PriceRuleService(priceRuleRepository, productItemRepository);
        UUID itemId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        PriceRuleRequest request = new PriceRuleRequest(
                itemId,
                storeId,
                PriceRule.PriceType.MEMBER,
                new BigDecimal("88.00"),
                null,
                null,
                null,
                null
        );
        PriceRule saved = new PriceRule();
        saved.setItemId(itemId);
        saved.setStoreId(storeId);
        saved.setPriceType(PriceRule.PriceType.MEMBER);
        saved.setPrice(new BigDecimal("88.00"));
        saved.setMinQty(1);
        saved.setActive(true);
        when(productItemRepository.existsById(itemId)).thenReturn(true);
        when(priceRuleRepository.save(any(PriceRule.class))).thenReturn(saved);

        PriceRuleResponse response = service.create(request);

        assertEquals(itemId, response.itemId());
        assertEquals(storeId, response.storeId());
        assertEquals(PriceRule.PriceType.MEMBER, response.priceType());
        assertEquals(1, response.minQty());
        assertEquals(true, response.active());
        verify(priceRuleRepository).save(any(PriceRule.class));
    }

    @Test
    void create_rejectsInvalidEffectiveWindow() {
        PriceRuleService service = new PriceRuleService(priceRuleRepository, productItemRepository);
        UUID itemId = UUID.randomUUID();
        when(productItemRepository.existsById(itemId)).thenReturn(true);

        PriceRuleRequest request = new PriceRuleRequest(
                itemId,
                null,
                PriceRule.PriceType.HAPPY_HOUR,
                new BigDecimal("80.00"),
                1,
                LocalDateTime.of(2026, 5, 14, 18, 0),
                LocalDateTime.of(2026, 5, 14, 12, 0),
                true
        );

        assertThrows(BusinessException.class, () -> service.create(request));
    }

    @Test
    void listByItem_withoutStoreIdReturnsGlobalRulesOnly() {
        PriceRuleService service = new PriceRuleService(priceRuleRepository, productItemRepository);
        UUID itemId = UUID.randomUUID();
        when(productItemRepository.existsById(itemId)).thenReturn(true);
        when(priceRuleRepository.findByItemIdAndStoreIdIsNullAndActiveTrue(itemId)).thenReturn(List.of());

        service.listByItem(itemId, null);

        verify(priceRuleRepository).findByItemIdAndStoreIdIsNullAndActiveTrue(itemId);
    }

    @Test
    void listByItem_rejectsMissingProduct() {
        PriceRuleService service = new PriceRuleService(priceRuleRepository, productItemRepository);
        UUID itemId = UUID.randomUUID();
        when(productItemRepository.existsById(itemId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.listByItem(itemId, null));
    }
}
