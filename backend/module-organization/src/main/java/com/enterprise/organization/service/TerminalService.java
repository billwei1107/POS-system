package com.enterprise.organization.service;

import com.enterprise.organization.entity.Terminal;

import java.util.List;
import java.util.UUID;

/**
 * 終端機管理服務介面 / Terminal management service interface
 */
public interface TerminalService {
    Terminal register(Terminal terminal);
    Terminal update(UUID id, Terminal terminal);
    Terminal getById(UUID id);
    Terminal getByTerminalCode(String terminalCode);
    List<Terminal> listByStore(UUID storeId);
    void updateStatus(UUID id, String status);
    void recordHeartbeat(UUID id, String ipAddress, String appVersion);
    List<Terminal> findOfflineTerminals(int thresholdSeconds);
    void delete(UUID id);
}
