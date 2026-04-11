CREATE TABLE wf_definitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL,
    version INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE wf_nodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    definition_id UUID NOT NULL REFERENCES wf_definitions(id),
    type VARCHAR(20) NOT NULL, /* START, APPROVAL, CONDITION, END */
    name VARCHAR(100) NOT NULL,
    approver_type VARCHAR(50), /* ROLE, POSITION, DEPARTMENT_MANAGER, SPECIFIC_USER */
    approver_id VARCHAR(50),   
    approval_strategy VARCHAR(20) DEFAULT 'ANY', /* ANY, ALL */
    sort_order INT NOT NULL DEFAULT 0,
    timeout_hours INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE wf_conditions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_node_id UUID NOT NULL REFERENCES wf_nodes(id),
    target_node_id UUID NOT NULL REFERENCES wf_nodes(id),
    field VARCHAR(50),
    operator VARCHAR(20),
    value VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE wf_instances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    definition_id UUID NOT NULL REFERENCES wf_definitions(id),
    business_type VARCHAR(50) NOT NULL,
    business_id VARCHAR(100) NOT NULL,
    initiator_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    current_node_id UUID REFERENCES wf_nodes(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_wf_instances_initiator ON wf_instances(initiator_id, status);

CREATE TABLE wf_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instance_id UUID NOT NULL REFERENCES wf_instances(id),
    node_id UUID NOT NULL REFERENCES wf_nodes(id),
    assignee_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    comment TEXT,
    operated_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_wf_tasks_assignee ON wf_tasks(assignee_id, status);

CREATE TABLE wf_histories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instance_id UUID NOT NULL REFERENCES wf_instances(id),
    node_id UUID NOT NULL REFERENCES wf_nodes(id),
    operator_id UUID NOT NULL,
    action VARCHAR(20) NOT NULL,
    comment TEXT,
    operated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);
