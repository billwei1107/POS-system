package com.enterprise.organization.service.impl;

import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.organization.entity.Store;
import com.enterprise.organization.event.StoreCreatedEvent;
import com.enterprise.organization.event.StoreUpdatedEvent;
import com.enterprise.organization.repository.StoreRepository;
import com.enterprise.organization.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 門店管理服務實作 / Store management service implementation
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pos.organization.multi-store", havingValue = "true")
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Store create(Store store) {
        Store saved = storeRepository.save(store);
        eventPublisher.publishEvent(new StoreCreatedEvent(this, saved));
        return saved;
    }

    @Override
    public Store update(UUID id, Store store) {
        Store existing = getById(id);
        existing.setName(store.getName());
        existing.setRegionId(store.getRegionId());
        existing.setAddress(store.getAddress());
        existing.setCity(store.getCity());
        existing.setDistrict(store.getDistrict());
        existing.setPostalCode(store.getPostalCode());
        existing.setPhone(store.getPhone());
        existing.setEmail(store.getEmail());
        existing.setTimezone(store.getTimezone());
        existing.setCurrency(store.getCurrency());
        existing.setOpeningTime(store.getOpeningTime());
        existing.setClosingTime(store.getClosingTime());
        existing.setTaxRate(store.getTaxRate());
        existing.setReceiptHeader(store.getReceiptHeader());
        existing.setReceiptFooter(store.getReceiptFooter());
        Store saved = storeRepository.save(existing);
        eventPublisher.publishEvent(new StoreUpdatedEvent(this, saved));
        return saved;
    }

    @Override
    public Store getById(UUID id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + id));
    }

    @Override
    public Store getByStoreCode(String storeCode) {
        return storeRepository.findByStoreCode(storeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with code: " + storeCode));
    }

    @Override
    public List<Store> listByCompany(UUID companyId) {
        return storeRepository.findByCompanyId(companyId);
    }

    @Override
    public List<Store> listByRegion(UUID regionId) {
        return storeRepository.findByRegionId(regionId);
    }

    @Override
    public void updateStatus(UUID id, String status) {
        Store store = getById(id);
        store.setStatus(status);
        storeRepository.save(store);
    }

    @Override
    public void delete(UUID id) {
        storeRepository.deleteById(id);
    }
}
