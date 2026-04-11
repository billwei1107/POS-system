export interface Company {
    id: string;
    name: string;
    code: string;
    address?: string;
    phone?: string;
    email?: string;
    legalPerson?: string;
    createdAt?: string;
}

export interface Department {
    id: string;
    companyId: string;
    name: string;
    code: string;
    parentId?: string;
    sortOrder: number;
    managerId?: string;
}

export interface DepartmentTreeDTO extends Department {
    children?: DepartmentTreeDTO[];
}

export interface Position {
    id: string;
    name: string;
    code: string;
    level: number;
    description?: string;
}

export interface Employee {
    id: string;
    employeeNo: string;
    userId?: string;
    companyId: string;
    departmentId?: string;
    positionId?: string;
    name: string;
    phone?: string;
    email?: string;
    hireDate?: string;
    resignDate?: string;
    status: 'ACTIVE' | 'RESIGNED' | 'ON_LEAVE';
    emergencyContact?: string;
    emergencyPhone?: string;
    createdAt?: string;
}
