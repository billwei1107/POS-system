/**
 * @file index.ts
 * @description POS 認證型別 / POS authentication types
 * @description_en Defines request and response contracts for POS PIN login
 * @description_zh 定義 POS PIN 登入請求與回應資料結構
 */

export interface PinLoginRequest {
    pin: string;
    terminalId?: string;
    terminalCode?: string;
}

export interface PinLoginResponse {
    token: string;
    refreshToken: string;
    userId: string;
    username: string;
    storeId: string;
    terminalId: string;
    employeeId?: string | null;
    role: string;
}
