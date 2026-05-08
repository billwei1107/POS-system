/**
 * @file TransferRequestRepository.java
 * @description 調撥申請 Repository / Transfer request JPA repository
 * @description_en JPA repository for inter-store transfer requests
 * @description_zh 門店調撥申請 JPA 儲存層
 */
package com.enterprise.inventory.repository;

import com.enterprise.inventory.entity.TransferRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferRequestRepository extends JpaRepository<TransferRequest, UUID> {

    Optional<TransferRequest> findByTransferNo(String transferNo);

    List<TransferRequest> findAllByFromStoreIdOrderByCreatedAtDesc(UUID fromStoreId);

    List<TransferRequest> findAllByToStoreIdOrderByCreatedAtDesc(UUID toStoreId);

    List<TransferRequest> findAllByStatus(TransferRequest.TransferStatus status);
}
