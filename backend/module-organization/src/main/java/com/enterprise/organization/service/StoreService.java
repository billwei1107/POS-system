package com.enterprise.organization.service;

import com.enterprise.organization.entity.Store;

import java.util.List;
import java.util.UUID;

/**
 * 門店管理服務介面 / Store management service interface
 */
public interface StoreService {
    Store create(Store store);
    Store update(UUID id, Store store);
    Store getById(UUID id);
    Store getByStoreCode(String storeCode);
    List<Store> listByCompany(UUID companyId);
    List<Store> listByRegion(UUID regionId);
    void updateStatus(UUID id, String status);
    void delete(UUID id);
}
