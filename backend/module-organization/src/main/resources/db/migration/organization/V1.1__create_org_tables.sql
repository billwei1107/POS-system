-- 組織架構模組資料表

-- 公司表
CREATE TABLE org_companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(100),
    legal_person VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    deleted_at TIMESTAMP
);
CREATE INDEX idx_org_companies_code ON org_companies(code);

-- 職位表
CREATE TABLE org_positions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    level INT DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    deleted_at TIMESTAMP
);

-- 部門表
CREATE TABLE org_departments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES org_companies(id),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    parent_id UUID REFERENCES org_departments(id),
    sort_order INT DEFAULT 0,
    manager_id UUID, -- 後續指向 employees
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    deleted_at TIMESTAMP,
    UNIQUE (company_id, code)
);
CREATE INDEX idx_org_departments_parent_id ON org_departments(parent_id);
CREATE INDEX idx_org_departments_company_id ON org_departments(company_id);

-- 員工表
CREATE TABLE org_employees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_no VARCHAR(50) UNIQUE NOT NULL,
    user_id UUID, -- 可能關聯至 auth_users
    company_id UUID NOT NULL REFERENCES org_companies(id),
    department_id UUID REFERENCES org_departments(id),
    position_id UUID REFERENCES org_positions(id),
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(50),
    email VARCHAR(100),
    hire_date DATE,
    resign_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, RESIGNED, ON_LEAVE
    emergency_contact VARCHAR(100),
    emergency_phone VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    deleted_at TIMESTAMP
);
-- 為了讓 department 的 manager_id 加上外鍵限制 (選用，不限制也行避免循環依賴)
-- ALTER TABLE org_departments ADD CONSTRAINT fk_org_departments_manager FOREIGN KEY (manager_id) REFERENCES org_employees(id);

CREATE INDEX idx_org_employees_company_id ON org_employees(company_id);
CREATE INDEX idx_org_employees_department_id ON org_employees(department_id);
CREATE INDEX idx_org_employees_user_id ON org_employees(user_id);

-- 員工合約表
CREATE TABLE org_employee_contracts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES org_employees(id),
    type VARCHAR(50) NOT NULL, -- FULL_TIME, PART_TIME, PROBATION
    start_date DATE NOT NULL,
    end_date DATE,
    renewal_reminder BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    deleted_at TIMESTAMP
);

-- 員工附件表
CREATE TABLE org_employee_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES org_employees(id),
    document_type VARCHAR(50) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT NOW(),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    deleted_at TIMESTAMP
);
