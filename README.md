# POS System

這是一個基於模塊化架構建置的 POS (Point of Sale) 系統，涵蓋後端 API 及前端管理後台。

## 目錄結構
- `backend/`: 後端 Spring Boot 模塊
- `frontend-web/`: 前端 React 網頁應用程式
- `docker/`: Docker 部署設定檔
- `env/`: 環境變數設定檔
- `devlog/`: 開發日誌

## 快速啟動
1. 複製並設定 `env/.env.example` 為 `env/local/.env`。
2. 透過 `docker` 資料夾中設定，啟動依賴服務。
3. 進入 `backend` 編譯與啟動服務。
4. 進入 `frontend-web` 並運行 `npm run dev` 啟動前端介面。
