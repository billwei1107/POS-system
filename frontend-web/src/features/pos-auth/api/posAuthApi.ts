/**
 * @file posAuthApi.ts
 * @description POS 認證 API / POS authentication API
 * @description_en Wraps POS terminal PIN login endpoints
 * @description_zh 封裝 POS 終端 PIN 登入相關 API
 */

import axiosInstance from '../../../shared/api/axiosInstance';
import type { ApiResponse } from '../../../shared/types';
import type { PinLoginRequest, PinLoginResponse } from '../types';

export const pinLoginApi = async (request: PinLoginRequest): Promise<PinLoginResponse> => {
    const response = await axiosInstance.post<ApiResponse<PinLoginResponse>>(
        '/v1/pos/auth/pin-login',
        request
    ) as unknown as ApiResponse<PinLoginResponse>;

    return response.data;
};
