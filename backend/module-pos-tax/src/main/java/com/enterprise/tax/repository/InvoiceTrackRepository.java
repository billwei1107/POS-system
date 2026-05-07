/**
 * @file InvoiceTrackRepository.java
 * @description 發票字軌資料存取層 / Invoice track repository
 * @description_en Queries active invoice character tracks; used by track allocation algorithm
 * @description_zh 查詢有效字軌；供字軌配發演算法使用
 */
package com.enterprise.tax.repository;

import com.enterprise.tax.entity.InvoiceTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceTrackRepository extends JpaRepository<InvoiceTrack, UUID> {

    List<InvoiceTrack> findByStoreIdAndIsActiveTrueOrderByTrackPrefix(UUID storeId);

    // 鎖定字軌列以避免號碼重複分配（同步由 InvoiceService 呼叫）
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM InvoiceTrack t WHERE t.storeId = :storeId AND t.isActive = true " +
           "AND t.currentNo < t.endNo ORDER BY t.trackPrefix ASC")
    Optional<InvoiceTrack> findAvailableTrackForUpdate(@Param("storeId") UUID storeId);
}
