package com.enterprise.organization.service.impl;

import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.organization.entity.Terminal;
import com.enterprise.organization.event.TerminalOfflineEvent;
import com.enterprise.organization.event.TerminalRegisteredEvent;
import com.enterprise.organization.repository.TerminalRepository;
import com.enterprise.organization.service.TerminalService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 終端機管理服務實作 / Terminal management service implementation
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pos.organization.terminal-monitor", havingValue = "true")
public class TerminalServiceImpl implements TerminalService {

    private final TerminalRepository terminalRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Terminal register(Terminal terminal) {
        terminal.setRegisteredAt(LocalDateTime.now());
        terminal.setStatus("OFFLINE");
        Terminal saved = terminalRepository.save(terminal);
        eventPublisher.publishEvent(new TerminalRegisteredEvent(this, saved));
        return saved;
    }

    @Override
    public Terminal update(UUID id, Terminal terminal) {
        Terminal existing = getById(id);
        existing.setName(terminal.getName());
        existing.setDeviceType(terminal.getDeviceType());
        existing.setDeviceModel(terminal.getDeviceModel());
        existing.setHardwareProfileJson(terminal.getHardwareProfileJson());
        return terminalRepository.save(existing);
    }

    @Override
    public Terminal getById(UUID id) {
        return terminalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Terminal not found with id: " + id));
    }

    @Override
    public Terminal getByTerminalCode(String terminalCode) {
        return terminalRepository.findByTerminalCode(terminalCode)
                .orElseThrow(() -> new ResourceNotFoundException("Terminal not found with code: " + terminalCode));
    }

    @Override
    public List<Terminal> listByStore(UUID storeId) {
        return terminalRepository.findByStoreId(storeId);
    }

    @Override
    public void updateStatus(UUID id, String status) {
        Terminal terminal = getById(id);
        terminal.setStatus(status);
        terminalRepository.save(terminal);
    }

    @Override
    public void recordHeartbeat(UUID id, String ipAddress, String appVersion) {
        Terminal terminal = getById(id);
        terminal.setLastHeartbeatAt(LocalDateTime.now());
        terminal.setStatus("ONLINE");
        if (ipAddress != null) terminal.setIpAddress(ipAddress);
        if (appVersion != null) terminal.setAppVersion(appVersion);
        terminalRepository.save(terminal);
    }

    @Override
    public List<Terminal> findOfflineTerminals(int thresholdSeconds) {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(thresholdSeconds);
        List<Terminal> offlineTerminals = terminalRepository.findByLastHeartbeatAtBefore(threshold);
        offlineTerminals.forEach(t -> {
            if ("ONLINE".equals(t.getStatus())) {
                t.setStatus("OFFLINE");
                terminalRepository.save(t);
                eventPublisher.publishEvent(new TerminalOfflineEvent(this, t, t.getLastHeartbeatAt()));
            }
        });
        return offlineTerminals;
    }

    @Override
    public void delete(UUID id) {
        terminalRepository.deleteById(id);
    }
}
