/**
 * @file ProductItemServiceTest.java
 * @description 商品服務單元測試 / Product item service unit tests
 */
package com.enterprise.product.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.product.dto.ProductItemRequest;
import com.enterprise.product.dto.ProductItemResponse;
import com.enterprise.product.entity.ProductItem;
import com.enterprise.product.repository.ProductItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductItemServiceTest {

    @Mock
    private ProductItemRepository itemRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProductItemService service;

    private ProductItemRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new ProductItemRequest();
        validRequest.setSku("TEST-001");
        validRequest.setName("測試商品");
        validRequest.setBasePrice(new BigDecimal("100.00"));
    }

    @Test
    void testCreate_DuplicateSku_ThrowsException() {
        when(itemRepository.existsBySku("TEST-001")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create(validRequest));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void testCreate_Success() {
        when(itemRepository.existsBySku("TEST-001")).thenReturn(false);

        ProductItem saved = new ProductItem();
        saved.setSku("TEST-001");
        saved.setName("測試商品");
        saved.setBasePrice(new BigDecimal("100.00"));

        try {
            java.lang.reflect.Field idField = com.enterprise.common.entity.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(saved, UUID.randomUUID());
        } catch (Exception ignored) {}

        when(itemRepository.save(any(ProductItem.class))).thenReturn(saved);

        ProductItemResponse response = service.create(validRequest);

        assertNotNull(response);
        assertEquals("TEST-001", response.getSku());
        assertEquals("測試商品", response.getName());
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void testFindById_NotFound_ThrowsException() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(com.enterprise.common.exception.ResourceNotFoundException.class,
                () -> service.findById(id));
    }
}
