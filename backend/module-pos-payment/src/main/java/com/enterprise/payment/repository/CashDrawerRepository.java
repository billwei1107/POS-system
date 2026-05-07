/**
 * @file CashDrawerRepository.java
 * @description 現金抽屜 Repository / Cash drawer JPA repository
 * @description_en Data access for pos_cash_drawers; finds currently open drawer for a terminal
 * @description_zh 存取 pos_cash_drawers，可查詢終端機目前開啟中的抽屜
 */
package com.enterprise.payment.repository;

import com.enterprise.payment.entity.CashDrawer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CashDrawerRepository extends JpaRepository<CashDrawer, UUID> {

    Optional<CashDrawer> findByTerminalIdAndStatus(UUID terminalId, CashDrawer.DrawerStatus status);

    boolean existsByTerminalIdAndStatus(UUID terminalId, CashDrawer.DrawerStatus status);
}
