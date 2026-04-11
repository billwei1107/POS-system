package com.enterprise.attendance.controller;

import com.enterprise.attendance.entity.Geofence;
import com.enterprise.attendance.repository.GeofenceRepository;
import com.enterprise.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @file GeofenceController.java
 * @description 地理圍欄管理控制器 / Geofence management controller
 */
@RestController
@RequestMapping("/api/v1/attendance/geofences")
@RequiredArgsConstructor
public class GeofenceController {

    private final GeofenceRepository geofenceRepository;

    @GetMapping
    public ApiResponse<List<Geofence>> getAllGeofences() {
        return ApiResponse.success(geofenceRepository.findByActiveTrue());
    }

    @PostMapping
    public ApiResponse<Geofence> createGeofence(@RequestBody Geofence geofence) {
        return ApiResponse.success(geofenceRepository.save(geofence));
    }

    @PutMapping("/{id}")
    public ApiResponse<Geofence> updateGeofence(@PathVariable UUID id, @RequestBody Geofence updated) {
        Geofence geo = geofenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Geofence not found"));
        geo.setName(updated.getName());
        geo.setLatitude(updated.getLatitude());
        geo.setLongitude(updated.getLongitude());
        geo.setRadiusMeters(updated.getRadiusMeters());
        geo.setWifiBssids(updated.getWifiBssids());
        geo.setActive(updated.getActive());
        return ApiResponse.success(geofenceRepository.save(geo));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteGeofence(@PathVariable UUID id) {
        geofenceRepository.findById(id).ifPresent(g -> {
            g.setDeletedAt(LocalDateTime.now());
            g.setActive(false);
            geofenceRepository.save(g);
        });
        return ApiResponse.success(null);
    }
}
