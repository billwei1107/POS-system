package com.enterprise.organization.service.impl;

import com.enterprise.organization.entity.Employee;
import com.enterprise.organization.event.EmployeeCreatedEvent;
import com.enterprise.organization.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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
}
