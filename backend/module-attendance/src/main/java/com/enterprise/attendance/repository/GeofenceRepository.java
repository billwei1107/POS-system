package com.enterprise.attendance.repository;

import com.enterprise.attendance.entity.Geofence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @file GeofenceRepository.java
 * @description 地理圍欄資料存取 / Geofence repository
 */
@Repository
public interface GeofenceRepository extends JpaRepository<Geofence, UUID> {
    List<Geofence> findByActiveTrue();
}
