package com.enterprise.organization.repository;

import com.enterprise.organization.entity.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, UUID> {

    List<Terminal> findByStoreId(UUID storeId);

    Optional<Terminal> findByTerminalCode(String terminalCode);

    List<Terminal> findByStoreIdAndStatus(UUID storeId, String status);

    List<Terminal> findByLastHeartbeatAtBefore(LocalDateTime threshold);
}
