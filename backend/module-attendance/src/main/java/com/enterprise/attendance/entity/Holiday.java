package com.enterprise.attendance.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * @file Holiday.java
 * @description 假日設定實體 / Holiday entity
 * @description_zh 國定假日與公司假日設定，用於排除打卡日期
 */
@Entity
@Table(name = "att_holidays")
@Data
@EqualsAndHashCode(callSuper = true)
public class Holiday extends BaseEntity {

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String type = "NATIONAL"; // NATIONAL / COMPANY

    @Column(nullable = false)
    private Integer year;
}
