-- ============================================================
-- POS 組織擴展資料表 / POS Organization Extension Tables
-- Sprint 1-1: 區域、門店、門店員工、終端機管理
-- ============================================================

-- 區域表 / Regions for multi-store management
CREATE TABLE org_regions (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    region_name VARCHAR(100) NOT NULL,
    region_code VARCHAR(50) NOT NULL,
    manager_employee_id UUID,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    UNIQUE (company_id, region_code)
);

CREATE INDEX idx_org_regions_company_id ON org_regions(company_id);

-- 門店表 / Store locations
CREATE TABLE org_stores (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    region_id UUID,
    store_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(500),
    city VARCHAR(100),
    district VARCHAR(100),
    postal_code VARCHAR(20),
    phone VARCHAR(50),
    email VARCHAR(100),
    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Taipei',
    currency VARCHAR(10) NOT NULL DEFAULT 'TWD',
    opening_time TIME,
    closing_time TIME,
    tax_rate DECIMAL(5, 4) DEFAULT 0.05,
    receipt_header TEXT,
    receipt_footer TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_org_stores_company_id ON org_stores(company_id);
CREATE INDEX idx_org_stores_region_id ON org_stores(region_id);
CREATE INDEX idx_org_stores_status ON org_stores(status);

-- 門店員工關聯表 / Store-employee assignment
CREATE TABLE org_store_employees (
    id UUID PRIMARY KEY,
    store_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    is_primary_store BOOLEAN NOT NULL DEFAULT FALSE,
    role_at_store VARCHAR(50),
    assigned_at TIMESTAMP DEFAULT NOW(),
    unassigned_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    UNIQUE (store_id, employee_id)
);

CREATE INDEX idx_org_store_employees_store_id ON org_store_employees(store_id);
CREATE INDEX idx_org_store_employees_employee_id ON org_store_employees(employee_id);

-- 終端機表 / POS Terminals
CREATE TABLE org_terminals (
    id UUID PRIMARY KEY,
    store_id UUID NOT NULL,
    terminal_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100),
    device_type VARCHAR(30) NOT NULL DEFAULT 'POS',
    device_model VARCHAR(100),
    hardware_profile_json JSONB,
    ip_address VARCHAR(50),
    app_version VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE',
    last_heartbeat_at TIMESTAMP,
    registered_at TIMESTAMP DEFAULT NOW(),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_org_terminals_store_id ON org_terminals(store_id);
CREATE INDEX idx_org_terminals_status ON org_terminals(status);
