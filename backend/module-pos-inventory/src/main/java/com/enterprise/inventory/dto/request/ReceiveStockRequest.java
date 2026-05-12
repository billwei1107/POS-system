/**
 * @file ReceiveStockRequest.java
 * @description 進貨驗收入庫請求 DTO / Purchase receiving stock request DTO
 * @description_en Request body for receiving counted inbound goods into store inventory
 * @description_zh 進貨驗收完成後，將實際點收數量批次入庫的請求體
 */
package com.enterprise.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ReceiveStockRequest(
        @NotNull UUID storeId,
        UUID operatedBy,
        String notes,
        @NotEmpty List<@Valid Item> items
) {
    public record Item(
            @NotNull UUID itemId,
            @NotNull @DecimalMin(value = "0.001") BigDecimal receivedQty
    ) {}
}
