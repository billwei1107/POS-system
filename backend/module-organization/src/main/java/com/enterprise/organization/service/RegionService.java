package com.enterprise.organization.service;

import com.enterprise.organization.entity.Region;

import java.util.List;
import java.util.UUID;

/**
 * 區域管理服務介面 / Region management service interface
 */
public interface RegionService {
    Region create(Region region);
    Region update(UUID id, Region region);
    Region getById(UUID id);
    List<Region> listByCompany(UUID companyId);
    void delete(UUID id);
}
