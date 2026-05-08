/**
 * @file LeaveTypeRepository.java
 * @description 假別類型資料存取層 / Leave type repository
 * @description_en JPA repository for leave type CRUD operations
 * @description_zh 假別類型的 JPA 資料存取層
 */
package com.enterprise.leave.repository;

import com.enterprise.leave.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, UUID> {

    @Query("SELECT t FROM LeaveType t WHERE t.deletedAt IS NULL ORDER BY t.name")
    List<LeaveType> findAllActive();

    Optional<LeaveType> findByCodeAndDeletedAtIsNull(String code);
}
