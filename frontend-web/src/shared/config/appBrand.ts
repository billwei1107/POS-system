/**
 * @file appBrand.ts
 * @description 應用程式品牌文字設定 / Application brand copy configuration
 * @description_en Centralizes POS brand labels used by shared entry pages
 * @description_zh 集中管理共用入口頁使用的 POS 品牌文案
 */
export const APP_BRAND = {
  productName: 'Titanium POS',
  productNameParts: {
    primary: 'Titanium',
    accent: ' POS',
  },
  subtitle: '咖啡門市收銀與後台管理系統',
  copyright: '© 2026 Titanium POS. All rights reserved.',
  colors: {
    accent: '#FF6D00',
    accentHover: '#E85F00',
  },
} as const;

export const LOGIN_COPY = {
  usernameLabel: '使用者帳號',
  passwordLabel: '登入密碼',
  submitLabel: '登入系統',
} as const;
