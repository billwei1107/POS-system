/**
 * @file InvoiceTrackController.java
 * @description 發票字軌 REST 控制器 / Invoice track REST controller
 * @description_en Endpoints for registering and listing bi-monthly MoF-allocated character tracks
 * @description_zh 登記與查詢財政部配發的電子發票字軌號碼範圍
 */
package com.enterprise.tax.controller;

import com.enterprise.common.dto.ApiResponse;
import com.enterprise.tax.dto.request.AddInvoiceTrackRequest;
import com.enterprise.tax.entity.InvoiceTrack;
import com.enterprise.tax.service.InvoiceTrackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pos/invoice-tracks")
@RequiredArgsConstructor
public class InvoiceTrackController {

    private final InvoiceTrackService trackService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceTrack>>> list(@RequestParam UUID storeId) {
        return ResponseEntity.ok(ApiResponse.success(trackService.listByStore(storeId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceTrack>> add(@Valid @RequestBody AddInvoiceTrackRequest req) {
        return ResponseEntity.ok(ApiResponse.success(trackService.addTrack(req)));
    }
}
