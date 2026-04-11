package com.enterprise.organization.service.impl;

import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.organization.entity.Company;
import com.enterprise.organization.repository.CompanyRepository;
import com.enterprise.organization.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    @Override
    public Company create(Company company) {
        return companyRepository.save(company);
    }

    @Override
    public Company update(UUID id, Company company) {
        Company existing = getById(id);
        existing.setName(company.getName());
        existing.setCode(company.getCode());
        existing.setAddress(company.getAddress());
        existing.setPhone(company.getPhone());
        existing.setEmail(company.getEmail());
        existing.setLegalPerson(company.getLegalPerson());
        return companyRepository.save(existing);
    }

    @Override
    public Company getById(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
    }

    @Override
    public List<Company> listAll() {
        return companyRepository.findAll();
    }

    @Override
    public void delete(UUID id) {
        // Soft delete is handled by @SQLDelete
        companyRepository.deleteById(id);
    }
}
