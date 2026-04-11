package com.enterprise.organization.service.impl;

import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.organization.dto.DepartmentTreeDTO;
import com.enterprise.organization.entity.Department;
import com.enterprise.organization.repository.DepartmentRepository;
import com.enterprise.organization.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public Department create(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    public Department update(UUID id, Department department) {
        Department existing = getById(id);
        existing.setName(department.getName());
        existing.setCode(department.getCode());
        existing.setParentId(department.getParentId());
        existing.setSortOrder(department.getSortOrder());
        existing.setManagerId(department.getManagerId());
        return departmentRepository.save(existing);
    }

    @Override
    public Department getById(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    @Override
    public List<Department> listByCompany(UUID companyId) {
        return departmentRepository.findByCompanyId(companyId);
    }

    @Override
    public void delete(UUID id) {
        departmentRepository.deleteById(id);
    }

    @Override
    public List<DepartmentTreeDTO> getDepartmentTree(UUID companyId) {
        List<Department> allDepts = listByCompany(companyId);
        List<DepartmentTreeDTO> allDtos = allDepts.stream().map(this::convertToDTO).collect(Collectors.toList());

        List<DepartmentTreeDTO> treeRoots = new ArrayList<>();
        for (DepartmentTreeDTO dto : allDtos) {
            if (dto.getParentId() == null) {
                treeRoots.add(dto);
            } else {
                for (DepartmentTreeDTO parentDto : allDtos) {
                    if (parentDto.getId().equals(dto.getParentId())) {
                        if (parentDto.getChildren() == null) {
                            parentDto.setChildren(new ArrayList<>());
                        }
                        parentDto.getChildren().add(dto);
                        break;
                    }
                }
            }
        }
        return treeRoots;
    }

    private DepartmentTreeDTO convertToDTO(Department dept) {
        DepartmentTreeDTO dto = new DepartmentTreeDTO();
        BeanUtils.copyProperties(dept, dto);
        dto.setChildren(new ArrayList<>());
        return dto;
    }
}
