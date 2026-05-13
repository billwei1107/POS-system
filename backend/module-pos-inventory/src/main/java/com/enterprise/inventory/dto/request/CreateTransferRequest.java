/**
 * @file CreateTransferRequest.java
 * @description 建立調撥申請 DTO / Create transfer request DTO
 * @description_en Request body for creating an inter-store stock transfer
 * @description_zh 建立門店調撥申請的請求體
 */
package com.enterprise.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateTransferRequest(
        @NotNull UUID fromStoreId,
        @NotNull UUID toStoreId,
        String notes,
        UUID requestedBy,
        @NotNull @Size(min = 1) List<TransferLineItem> items
) {
    public record TransferLineItem(
            @NotNull UUID itemId,
            @NotNull BigDecimal requestedQty
    ) {}
}
