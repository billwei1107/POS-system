package com.enterprise.auth.repository;

import com.enterprise.auth.entity.PinCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PinCodeRepository extends JpaRepository<PinCode, UUID> {

    Optional<PinCode> findByUserIdAndTerminalTypeAndActiveTrue(UUID userId, String terminalType);

    Optional<PinCode> findByUserIdAndActiveTrue(UUID userId);

    List<PinCode> findByUserId(UUID userId);

    List<PinCode> findByUserIdInAndActiveTrue(List<UUID> userIds);
}
