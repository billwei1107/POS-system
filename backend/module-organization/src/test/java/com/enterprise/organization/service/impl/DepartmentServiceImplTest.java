package com.enterprise.organization.service.impl;

import com.enterprise.organization.dto.DepartmentTreeDTO;
import com.enterprise.organization.entity.Department;
import com.enterprise.organization.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private UUID companyId;

    @BeforeEach
    public void setup() {
        companyId = UUID.randomUUID();
    }

    @Test
    public void testGetDepartmentTree() {
        Department root = new Department();
        root.setId(UUID.randomUUID());
        root.setName("Headquarters");
        root.setCompanyId(companyId);

        Department child1 = new Department();
        child1.setId(UUID.randomUUID());
        child1.setName("Human Resources");
        child1.setCompanyId(companyId);
        child1.setParentId(root.getId());

        when(departmentRepository.findByCompanyId(companyId)).thenReturn(Arrays.asList(root, child1));

        List<DepartmentTreeDTO> tree = departmentService.getDepartmentTree(companyId);

        assertEquals(1, tree.size(), "Should have exactly 1 root node");
        assertEquals("Headquarters", tree.get(0).getName());
        assertEquals(1, tree.get(0).getChildren().size(), "Root node should contain 1 child node");
        assertEquals("Human Resources", tree.get(0).getChildren().get(0).getName());
    }
}
