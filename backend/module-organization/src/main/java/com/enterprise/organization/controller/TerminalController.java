package com.enterprise.organization.controller;

import com.enterprise.common.dto.ApiResponse;
import com.enterprise.organization.entity.Terminal;
import com.enterprise.organization.service.TerminalService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/terminals")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pos.organization.terminal-monitor", havingValue = "true")
public class TerminalController {

    private final TerminalService terminalService;

    @PostMapping
    public ApiResponse<Terminal> register(@RequestBody Terminal terminal) {
        return ApiResponse.success(terminalService.register(terminal));
    }

    @PutMapping("/{id}")
    public ApiResponse<Terminal> update(@PathVariable UUID id, @RequestBody Terminal terminal) {
        return ApiResponse.success(terminalService.update(id, terminal));
    }

    @GetMapping("/{id}")
    public ApiResponse<Terminal> getById(@PathVariable UUID id) {
        return ApiResponse.success(terminalService.getById(id));
    }

    @GetMapping("/code/{terminalCode}")
    public ApiResponse<Terminal> getByTerminalCode(@PathVariable String terminalCode) {
        return ApiResponse.success(terminalService.getByTerminalCode(terminalCode));
    }

    @GetMapping("/store/{storeId}")
    public ApiResponse<List<Terminal>> listByStore(@PathVariable UUID storeId) {
        return ApiResponse.success(terminalService.listByStore(storeId));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable UUID id, @RequestParam String status) {
        terminalService.updateStatus(id, status);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/heartbeat")
    public ApiResponse<Void> recordHeartbeat(@PathVariable UUID id,
                                              @RequestParam(required = false) String ipAddress,
                                              @RequestParam(required = false) String appVersion) {
        terminalService.recordHeartbeat(id, ipAddress, appVersion);
        return ApiResponse.success(null);
    }

    @GetMapping("/offline")
    public ApiResponse<List<Terminal>> findOfflineTerminals(@RequestParam(defaultValue = "300") int thresholdSeconds) {
        return ApiResponse.success(terminalService.findOfflineTerminals(thresholdSeconds));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        terminalService.delete(id);
        return ApiResponse.success(null);
    }
}
