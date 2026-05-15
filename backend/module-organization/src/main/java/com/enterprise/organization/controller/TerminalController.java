package com.enterprise.organization.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
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
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-terminal", action = "register")
    public ApiResponse<Terminal> register(@RequestBody Terminal terminal) {
        return ApiResponse.success(terminalService.register(terminal));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-terminal", action = "update")
    public ApiResponse<Terminal> update(@PathVariable UUID id, @RequestBody Terminal terminal) {
        return ApiResponse.success(terminalService.update(id, terminal));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:organization:read")
    public ApiResponse<Terminal> getById(@PathVariable UUID id) {
        return ApiResponse.success(terminalService.getById(id));
    }

    @GetMapping("/code/{terminalCode}")
    @RequirePermission("system:organization:read")
    public ApiResponse<Terminal> getByTerminalCode(@PathVariable String terminalCode) {
        return ApiResponse.success(terminalService.getByTerminalCode(terminalCode));
    }

    @GetMapping("/store/{storeId}")
    @RequirePermission("system:organization:read")
    public ApiResponse<List<Terminal>> listByStore(@PathVariable UUID storeId) {
        return ApiResponse.success(terminalService.listByStore(storeId));
    }

    @PatchMapping("/{id}/status")
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-terminal", action = "update-status")
    public ApiResponse<Void> updateStatus(@PathVariable UUID id, @RequestParam String status) {
        terminalService.updateStatus(id, status);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/heartbeat")
    @RequirePermission("system:organization:manage")
    public ApiResponse<Void> recordHeartbeat(@PathVariable UUID id,
                                              @RequestParam(required = false) String ipAddress,
                                              @RequestParam(required = false) String appVersion) {
        terminalService.recordHeartbeat(id, ipAddress, appVersion);
        return ApiResponse.success(null);
    }

    @GetMapping("/offline")
    @RequirePermission("system:organization:read")
    public ApiResponse<List<Terminal>> findOfflineTerminals(@RequestParam(defaultValue = "300") int thresholdSeconds) {
        return ApiResponse.success(terminalService.findOfflineTerminals(thresholdSeconds));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:organization:manage")
    @Auditable(module = "organization-terminal", action = "delete")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        terminalService.delete(id);
        return ApiResponse.success(null);
    }
}
