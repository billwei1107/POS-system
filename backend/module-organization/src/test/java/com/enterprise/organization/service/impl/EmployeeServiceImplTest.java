package com.enterprise.organization.service.impl;

import com.enterprise.organization.entity.Employee;
import com.enterprise.organization.event.EmployeeCreatedEvent;
import com.enterprise.organization.repository.EmployeeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testCreateEmployee_generatesEmpNoAndPublishesEvent() {
        Employee emp = new Employee();
        emp.setName("Alice");
        emp.setCompanyId(UUID.randomUUID());

        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee result = employeeService.create(emp);

        assertNotNull(result.getEmployeeNo(), "Employee number should be automatically generated");
        assertTrue(result.getEmployeeNo().startsWith("EMP-"), "Employee number should contain standard prefix");

        ArgumentCaptor<EmployeeCreatedEvent> eventCaptor = ArgumentCaptor.forClass(EmployeeCreatedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        EmployeeCreatedEvent publishedEvent = eventCaptor.getValue();
        assertEquals(result, publishedEvent.getEmployee(), "Published event should attach the new employee");
    }

    @Test
    public void testListAllReturnsRepositoryEmployees() {
        Employee alice = new Employee();
        alice.setName("Alice");
        Employee bob = new Employee();
        bob.setName("Bob");

        when(employeeRepository.findAll()).thenReturn(List.of(alice, bob));

        List<Employee> result = employeeService.listAll();

        assertEquals(2, result.size(), "Unfiltered employee list should return all visible employees");
        assertEquals("Alice", result.get(0).getName());
        verify(employeeRepository).findAll();
    }

    @Test
    public void testGetCurrentEmployeeReturnsEmployeeLinkedToCurrentUser() {
        UUID userId = UUID.randomUUID();
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setUserId(userId);
        employee.setName("Alice");

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userId, null));
        when(employeeRepository.findByUserId(userId)).thenReturn(Optional.of(employee));

        Employee result = employeeService.getCurrentEmployee();

        assertEquals(employee.getId(), result.getId());
        verify(employeeRepository).findByUserId(userId);
    }
}
