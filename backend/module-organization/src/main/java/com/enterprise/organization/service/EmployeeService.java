package com.enterprise.organization.service;

import com.enterprise.organization.entity.Employee;
import java.util.List;
import java.util.UUID;

public interface EmployeeService {
    Employee create(Employee employee);
    Employee update(UUID id, Employee employee);
    Employee getById(UUID id);
    List<Employee> listByCompany(UUID companyId);
    List<Employee> listByDepartment(UUID departmentId);
    void resignEmployee(UUID id);
    void delete(UUID id);
}
