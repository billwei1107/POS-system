/**
 * @file CreatePayMethodRequest.java
 * @description 建立支付方式請求 DTO / Create pay method request DTO
 * @description_en Request body for creating a new store payment method
 * @description_zh 建立門店支付方式的請求 DTO
 */
package com.enterprise.payment.dto.request;

import com.enterprise.payment.entity.PayMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePayMethodRequest(
    @NotNull UUID storeId,
    @NotBlank String code,
    @NotBlank String name,
    @NotNull PayMethod.MethodType methodType,
    UUID gatewayId,
    boolean isChangeBack,
    int sortOrder
) {}
