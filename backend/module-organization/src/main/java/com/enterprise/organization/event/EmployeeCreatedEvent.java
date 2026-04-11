package com.enterprise.organization.event;

import com.enterprise.organization.entity.Employee;
import org.springframework.context.ApplicationEvent;

public class EmployeeCreatedEvent extends ApplicationEvent {
    private final Employee employee;

    public EmployeeCreatedEvent(Object source, Employee employee) {
        super(source);
        this.employee = employee;
    }

    public Employee getEmployee() {
        return employee;
    }
}
