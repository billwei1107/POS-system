package com.enterprise.organization.service;

import com.enterprise.organization.entity.Company;
import java.util.List;
import java.util.UUID;

public interface CompanyService {
    Company create(Company company);
    Company update(UUID id, Company company);
    Company getById(UUID id);
    List<Company> listAll();
    void delete(UUID id);
}
