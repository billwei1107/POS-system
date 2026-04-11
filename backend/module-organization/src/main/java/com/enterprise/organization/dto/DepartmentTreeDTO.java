package com.enterprise.organization.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DepartmentTreeDTO {
    private UUID id;
    private String name;
    private String code;
    private UUID parentId;
    private Integer sortOrder;
    private UUID managerId;
    private List<DepartmentTreeDTO> children;
}
