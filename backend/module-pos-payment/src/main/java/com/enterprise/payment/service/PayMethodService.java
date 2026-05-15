/**
 * @file PayMethodService.java
 * @description 支付方式管理服務 / Pay method management service
 * @description_en CRUD operations for store-level payment method configuration
 * @description_zh 門店支付方式的 CRUD 管理服務
 */
package com.enterprise.payment.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.payment.dto.request.CreatePayMethodRequest;
import com.enterprise.payment.entity.PayMethod;
import com.enterprise.payment.repository.PayMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayMethodService {

    private final PayMethodRepository payMethodRepository;

    // ========================================
    // 查詢門店支付方式 / List active pay methods by store
    // ========================================
    @Transactional(readOnly = true)
    public List<PayMethod> listByStore(UUID storeId) {
        return payMethodRepository.findByStoreIdAndIsActiveTrueOrderBySortOrder(storeId);
    }

    // ========================================
    // 查詢支付方式門店 / Find pay method store
    // ========================================
    @Transactional(readOnly = true)
    public UUID findStoreId(UUID id) {
        return payMethodRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PayMethod not found: " + id))
            .getStoreId();
    }

    // ========================================
    // 建立支付方式 / Create pay method
    // ========================================
    @Transactional
    public PayMethod create(CreatePayMethodRequest req) {
        if (payMethodRepository.existsByStoreIdAndCode(req.storeId(), req.code())) {
            throw new BusinessException("Pay method code already exists: " + req.code());
        }
        PayMethod pm = new PayMethod();
        pm.setStoreId(req.storeId());
        pm.setCode(req.code());
        pm.setName(req.name());
        pm.setMethodType(req.methodType());
        pm.setGatewayId(req.gatewayId());
        pm.setChangeBack(req.isChangeBack());
        pm.setSortOrder(req.sortOrder());
        return payMethodRepository.save(pm);
    }

    // ========================================
    // 停用支付方式 / Deactivate pay method
    // ========================================
    @Transactional
    public void deactivate(UUID id) {
        PayMethod pm = payMethodRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PayMethod not found: " + id));
        pm.setActive(false);
        payMethodRepository.save(pm);
    }
}
