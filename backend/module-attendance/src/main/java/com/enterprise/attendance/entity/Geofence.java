package com.enterprise.attendance.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @file Geofence.java
 * @description 地理圍欄實體 / Geofence entity
 * @description_zh GPS 打卡範圍設定，包含中心經緯度與半徑
 */
@Entity
@Table(name = "att_geofences")
@Data
@EqualsAndHashCode(callSuper = true)
public class Geofence extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "radius_meters", nullable = false)
    private Integer radiusMeters = 200;

    @Column(name = "wifi_bssids", columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String wifiBssids;

    @Column(nullable = false)
    private Boolean active = true;
}
