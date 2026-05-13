/**
 * @file vitest.config.ts
 * @description 前端測試設定 / Frontend test configuration
 * @description_en Configures Vitest for React component and store tests
 * @description_zh 設定 Vitest 以執行 React 組件與狀態測試
 */
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import { resolve } from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@shared': resolve(__dirname, 'src/shared'),
      '@features': resolve(__dirname, 'src/features'),
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: ['./test/setup.ts'],
    restoreMocks: true,
    clearMocks: true,
  },
});
