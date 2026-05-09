# POS System

這是一個基於模塊化架構建置的 POS (Point of Sale) 系統，涵蓋後端 API 及前端管理後台。

## 目錄結構
- `AGENTS.md`: AI Agent 第一入口
- `CLAUDE.md`: Claude 規劃入口
- `ai-project-start.md`: Claude 規劃與 Codex 執行的啟動流程
- `.agent/`: 專案工作流與本地 Skill
- `.cursor/`: Cursor 專案規則
- `backend/`: 後端 Spring Boot 模塊
- `frontend-web/`: 前端 React 網頁應用程式
- `docker/`: Docker 部署設定檔
- `env/`: 環境變數設定檔
- `需求/`: Claude 規劃與 Codex 執行交接文件
- `需求/技術規格.md`: POS 詳細資料表、Feature Toggle、API 與技術規格
- `需求/UIUX-設計提示詞.md`: POS UI/UX 設計參考提示詞
- `reference/`: AI 參考資料；`reference/模塊化組件/` 由腳本自動抓取，不作為正式源碼
- `scripts/`: 專案輔助腳本
- `devlog/`: 開發日誌

> 舊模板資料夾 已整理併入上述正式位置；POS 專案根目錄固定為本資料夾，不再以模板資料夾作為根目錄。

## 快速啟動
1. 初始化模塊化組件 reference：`bash scripts/setup-module-reference.sh`。
2. 複製並設定 `env/.env.example` 為 `env/local/.env`。
3. 透過 `docker` 資料夾中設定，啟動依賴服務。
4. 進入 `backend` 編譯與啟動服務。
5. 進入 `frontend-web` 並運行 `npm run dev` 啟動前端介面。

## AI 協作流程

使用 Claude 規劃、Codex 執行時，請先閱讀：

```text
AGENTS.md
ai-project-start.md
需求/AI協作交接規範.md
```

Claude 規劃階段請補齊：

- `需求/需求文檔.md`
- `需求/系統規格表.md`
- `需求/開發流程規劃.md`
- `需求/功能檢驗流程書.md`

Codex 執行階段會依上述文件自行設計實作方案、導入模塊、測試並更新 devlog。
