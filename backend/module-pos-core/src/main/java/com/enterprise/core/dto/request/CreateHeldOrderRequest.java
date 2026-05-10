/**
 * @file CreateHeldOrderRequest.java
 * @description 建立掛單請求 / Create held order request
 * @description_en Request payload used to persist a parked POS cart
 * @description_zh 將 POS 購物車序列化後保存為可取回掛單
 */
package com.enterprise.core.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateHeldOrderRequest(
    @NotNull UUID storeId,
    UUID terminalId,
    String label,
    @NotBlank String payload
) {}
