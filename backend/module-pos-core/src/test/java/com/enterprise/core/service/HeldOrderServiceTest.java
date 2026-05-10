/**
 * @file HeldOrderServiceTest.java
 * @description 掛單服務測試 / Held order service tests
 * @description_en Verifies held order persistence, listing and deletion
 * @description_zh 驗證掛單保存、查詢與刪除流程
 */
package com.enterprise.core.service;

import com.enterprise.core.dto.request.CreateHeldOrderRequest;
import com.enterprise.core.dto.response.HeldOrderResponse;
import com.enterprise.core.entity.HeldOrder;
import com.enterprise.core.repository.HeldOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeldOrderServiceTest {

    @Mock
    private HeldOrderRepository heldOrderRepository;

    private HeldOrderService heldOrderService;

    @BeforeEach
    void setUp() {
        heldOrderService = new HeldOrderService(heldOrderRepository);
    }

    @Test
    void create_validRequest_savesPayload() {
        UUID storeId = UUID.randomUUID();
        UUID terminalId = UUID.randomUUID();
        CreateHeldOrderRequest request = new CreateHeldOrderRequest(storeId, terminalId, "H123456", "{\"total\":126}");

        when(heldOrderRepository.save(any(HeldOrder.class))).thenAnswer(invocation -> {
            HeldOrder heldOrder = invocation.getArgument(0);
            heldOrder.setId(UUID.randomUUID());
            return heldOrder;
        });

        HeldOrderResponse response = heldOrderService.create(request);

        ArgumentCaptor<HeldOrder> captor = ArgumentCaptor.forClass(HeldOrder.class);
        verify(heldOrderRepository).save(captor.capture());
        assertThat(captor.getValue().getStoreId()).isEqualTo(storeId);
        assertThat(captor.getValue().getTerminalId()).isEqualTo(terminalId);
        assertThat(captor.getValue().getLabel()).isEqualTo("H123456");
        assertThat(captor.getValue().getPayload()).isEqualTo("{\"total\":126}");
        assertThat(response.id()).isNotNull();
        assertThat(response.heldAt()).isNotNull();
    }

    @Test
    void list_withTerminalId_filtersByStoreAndTerminal() {
        UUID storeId = UUID.randomUUID();
        UUID terminalId = UUID.randomUUID();
        HeldOrder heldOrder = createHeldOrder(storeId, terminalId);

        when(heldOrderRepository.findByStoreIdAndTerminalIdOrderByHeldAtDesc(storeId, terminalId))
            .thenReturn(List.of(heldOrder));

        List<HeldOrderResponse> responses = heldOrderService.list(storeId, terminalId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).storeId()).isEqualTo(storeId);
        verify(heldOrderRepository).findByStoreIdAndTerminalIdOrderByHeldAtDesc(storeId, terminalId);
    }

    @Test
    void delete_existingHeldOrder_deletesEntity() {
        UUID id = UUID.randomUUID();
        HeldOrder heldOrder = createHeldOrder(UUID.randomUUID(), UUID.randomUUID());

        when(heldOrderRepository.findById(id)).thenReturn(Optional.of(heldOrder));

        heldOrderService.delete(id);

        verify(heldOrderRepository).delete(heldOrder);
    }

    private HeldOrder createHeldOrder(UUID storeId, UUID terminalId) {
        HeldOrder heldOrder = new HeldOrder();
        heldOrder.setId(UUID.randomUUID());
        heldOrder.setStoreId(storeId);
        heldOrder.setTerminalId(terminalId);
        heldOrder.setLabel("H123456");
        heldOrder.setPayload("{\"total\":126}");
        return heldOrder;
    }
}
