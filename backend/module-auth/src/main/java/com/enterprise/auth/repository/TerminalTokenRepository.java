package com.enterprise.auth.repository;

import com.enterprise.auth.entity.TerminalToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TerminalTokenRepository extends JpaRepository<TerminalToken, UUID> {

    Optional<TerminalToken> findByTerminalIdAndActiveTrue(UUID terminalId);

    List<TerminalToken> findByStoreId(UUID storeId);
}
