/**
 * @file TaxClassService.java
 * @description 稅率類別服務 / Tax class service
 * @description_en CRUD for tax classes and tax rule-based resolution for products
 * @description_zh 稅率類別 CRUD，並提供依商品/分類的稅率解析邏輯
 */
package com.enterprise.tax.service;

import com.enterprise.tax.dto.request.CreateTaxClassRequest;
import com.enterprise.tax.entity.TaxClass;
import com.enterprise.tax.entity.TaxRule;
import com.enterprise.tax.repository.TaxClassRepository;
import com.enterprise.tax.repository.TaxRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaxClassService {

    private final TaxClassRepository taxClassRepository;
    private final TaxRuleRepository taxRuleRepository;

    @Transactional(readOnly = true)
    public List<TaxClass> listByStore(UUID storeId) {
        return taxClassRepository.findByStoreIdAndIsActiveTrueOrderByName(storeId);
    }

    @Transactional
    public TaxClass create(CreateTaxClassRequest req) {
        TaxClass tc = new TaxClass();
        tc.setStoreId(req.storeId());
        tc.setName(req.name());
        tc.setTaxType(req.taxType());
        tc.setRate(req.rate());
        tc.setDescription(req.description());
        tc.setDefault(req.isDefault());
        return taxClassRepository.save(tc);
    }

    @Transactional
    public void deactivate(UUID id) {
        taxClassRepository.deleteById(id);
    }

    // ========================================
    // 依商品/分類解析適用稅率類別 / Resolve applicable tax class by product/category
    // ========================================
    @Transactional(readOnly = true)
    public TaxClass resolveForProduct(UUID storeId, UUID productId, UUID categoryId) {
        List<TaxRule> rules = taxRuleRepository.findApplicableRules(storeId, productId, categoryId);
        if (!rules.isEmpty()) {
            return rules.get(0).getTaxClass();
        }
        // 沒有規則則使用預設稅率
        Optional<TaxClass> defaultTc = taxClassRepository.findByStoreIdAndIsDefaultTrue(storeId);
        return defaultTc.orElseThrow(() ->
                new IllegalStateException("No default tax class configured for store: " + storeId));
    }
}
