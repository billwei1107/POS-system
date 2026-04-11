package com.enterprise.auth.repository;

import com.enterprise.auth.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, UUID> {
    List<LoginLog> findByUserIdOrderByLoginTimeDesc(UUID userId);
}
