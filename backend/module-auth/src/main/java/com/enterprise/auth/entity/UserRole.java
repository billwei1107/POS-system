package com.enterprise.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "auth_user_roles")
@IdClass(UserRole.UserRoleId.class)
@Data
public class UserRole {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "role_id")
    private UUID roleId;

    @Data
    public static class UserRoleId implements Serializable {
        private UUID userId;
        private UUID roleId;
    }
}
