package com.enterprise.organization.controller;

import com.enterprise.common.dto.ApiResponse;
import com.enterprise.organization.entity.Company;
import com.enterprise.organization.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ApiResponse<Company> create(@RequestBody Company company) {
        return ApiResponse.success(companyService.create(company));
    }

    @PutMapping("/{id}")
    public ApiResponse<Company> update(@PathVariable UUID id, @RequestBody Company company) {
        return ApiResponse.success(companyService.update(id, company));
    }

    @GetMapping("/{id}")
    public ApiResponse<Company> getById(@PathVariable UUID id) {
        return ApiResponse.success(companyService.getById(id));
    }

    @GetMapping
    public ApiResponse<List<Company>> listAll() {
        return ApiResponse.success(companyService.listAll());
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        companyService.delete(id);
        return ApiResponse.success(null);
    }
}
