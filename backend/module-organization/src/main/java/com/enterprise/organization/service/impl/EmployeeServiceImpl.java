package com.enterprise.organization.service.impl;

import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.organization.entity.Employee;
import com.enterprise.organization.event.EmployeeCreatedEvent;
import com.enterprise.organization.repository.EmployeeRepository;
import com.enterprise.organization.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Employee create(Employee employee) {
        // 自增員工編號: EX: EMP-2026-1234
        if (employee.getEmployeeNo() == null || employee.getEmployeeNo().isEmpty()) {
            String empNo = "EMP-" + LocalDate.now().getYear() + "-" + String.format("%04d", new Random().nextInt(10000));
            employee.setEmployeeNo(empNo);
        }
        
        Employee saved = employeeRepository.save(employee);
        
        // 觸發事件讓其他模組 (如 module-attendance) 可隨後進行串聯建置
        eventPublisher.publishEvent(new EmployeeCreatedEvent(this, saved));
        
        return saved;
    }

    @Override
    public Employee update(UUID id, Employee employee) {
        Employee existing = getById(id);
        existing.setName(employee.getName());
        existing.setPhone(employee.getPhone());
        existing.setEmail(employee.getEmail());
        existing.setDepartmentId(employee.getDepartmentId());
        existing.setPositionId(employee.getPositionId());
        existing.setEmergencyContact(employee.getEmergencyContact());
        existing.setEmergencyPhone(employee.getEmergencyPhone());
        return employeeRepository.save(existing);
    }

    @Override
    public Employee getById(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    @Override
    public List<Employee> listByCompany(UUID companyId) {
        return employeeRepository.findByCompanyId(companyId);
    }

    @Override
    public List<Employee> listByDepartment(UUID departmentId) {
        return employeeRepository.findByDepartmentId(departmentId);
    }

    @Override
    public void resignEmployee(UUID id) {
        Employee employee = getById(id);
        employee.setStatus("RESIGNED");
        employee.setResignDate(LocalDate.now());
        employeeRepository.save(employee);
    }

    @Override
    public void delete(UUID id) {
        employeeRepository.deleteById(id);
    }
}
