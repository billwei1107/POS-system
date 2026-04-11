package com.enterprise.auth.entity;

import com.enterprise.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_login_logs")
@Data
@EqualsAndHashCode(callSuper = true)
public class LoginLog extends BaseEntity {

    @Column(name = "user_id")
    private UUID userId;

    @Column(length = 50)
    private String ip;

    @Column(length = 255)
    private String device;

    @Column(name = "login_time")
    private LocalDateTime loginTime;

    @Column(length = 100)
    private String location;

    private Boolean success;
}
