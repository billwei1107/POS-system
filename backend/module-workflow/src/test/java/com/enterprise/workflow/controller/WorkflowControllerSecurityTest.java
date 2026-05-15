/**
 * @file WorkflowControllerSecurityTest.java
 * @description 工作流控制器權限註解測試 / Workflow controller permission annotation tests
 * @description_en Verifies workflow definition, start, and task endpoints are protected by permission and audit annotations
 * @description_zh 驗證工作流定義、啟動與任務審批端點具備權限與稽核註解
 */
package com.enterprise.workflow.controller;

import com.enterprise.common.annotation.Auditable;
import com.enterprise.common.annotation.RequirePermission;
import com.enterprise.workflow.dto.ApprovalRequest;
import com.enterprise.workflow.dto.ForwardRequest;
import com.enterprise.workflow.dto.StartWorkflowRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WorkflowControllerSecurityTest {

    @Test
    void definitionEndpointUsesWorkflowReadPermission() throws NoSuchMethodException {
        assertPermission(DefinitionController.class.getDeclaredMethod("listDefinitions"),
                "system:workflow:read");
    }

    @Test
    void workflowStartEndpointUsesStartPermissionAndAudit() throws NoSuchMethodException {
        assertMutation(WorkflowController.class.getDeclaredMethod("startWorkflow", StartWorkflowRequest.class),
                "system:workflow:start", "workflow-instance", "start");
    }

    @Test
    void workflowTaskEndpointsUseWorkflowPermissions() throws NoSuchMethodException {
        assertPermission(TaskController.class.getDeclaredMethod("getMyPendingTasks", UUID.class),
                "system:workflow:read");
        assertMutation(TaskController.class.getDeclaredMethod("approve", UUID.class, ApprovalRequest.class),
                "system:workflow:approve", "workflow-task", "approve");
        assertMutation(TaskController.class.getDeclaredMethod("reject", UUID.class, ApprovalRequest.class),
                "system:workflow:approve", "workflow-task", "reject");
        assertMutation(TaskController.class.getDeclaredMethod("forward", UUID.class, ForwardRequest.class),
                "system:workflow:approve", "workflow-task", "forward");
    }

    private void assertMutation(Method method, String permissionCode, String module, String action) {
        assertPermission(method, permissionCode);

        Auditable auditable = method.getAnnotation(Auditable.class);
        assertNotNull(auditable, "Mutation endpoint should be audited");
        assertEquals(module, auditable.module());
        assertEquals(action, auditable.action());
    }

    private void assertPermission(Method method, String permissionCode) {
        RequirePermission permission = method.getAnnotation(RequirePermission.class);
        assertNotNull(permission, "Endpoint should declare a permission requirement");
        assertEquals(permissionCode, permission.value());
    }
}
