/**
 * @file CashDrawerEventRepository.java
 * @description 現金抽屜事件 Repository / Cash drawer event JPA repository
 * @description_en Data access for pos_cash_drawer_events; chronological event log per drawer
 * @description_zh 存取 pos_cash_drawer_events，按時間順序記錄每次抽屜操作
 */
package com.enterprise.payment.repository;

import com.enterprise.payment.entity.CashDrawerEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CashDrawerEventRepository extends JpaRepository<CashDrawerEvent, UUID> {

    List<CashDrawerEvent> findByDrawerIdOrderByOccurredAtAsc(UUID drawerId);
}
