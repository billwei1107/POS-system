package com.enterprise.organization.repository;

import com.enterprise.organization.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

    List<Store> findByCompanyId(UUID companyId);

    List<Store> findByRegionId(UUID regionId);

    Optional<Store> findByStoreCode(String storeCode);

    List<Store> findByStatus(String status);
}
