package com.enterprise.organization.service.impl;

import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.organization.entity.Region;
import com.enterprise.organization.repository.RegionRepository;
import com.enterprise.organization.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 區域管理服務實作 / Region management service implementation
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pos.organization.multi-region", havingValue = "true")
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;

    @Override
    public Region create(Region region) {
        return regionRepository.save(region);
    }

    @Override
    public Region update(UUID id, Region region) {
        Region existing = getById(id);
        existing.setRegionName(region.getRegionName());
        existing.setRegionCode(region.getRegionCode());
        existing.setManagerEmployeeId(region.getManagerEmployeeId());
        existing.setDescription(region.getDescription());
        existing.setActive(region.getActive());
        return regionRepository.save(existing);
    }

    @Override
    public Region getById(UUID id) {
        return regionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Region not found with id: " + id));
    }

    @Override
    public List<Region> listByCompany(UUID companyId) {
        return regionRepository.findByCompanyIdAndActiveTrue(companyId);
    }

    @Override
    public void delete(UUID id) {
        regionRepository.deleteById(id);
    }
}
