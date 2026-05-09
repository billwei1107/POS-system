# POS 專案資料夾總覽

本文件是 POS 專案的資料夾地圖。AI 接手時應先讀 `AGENTS.md`、`ai-project-start.md`，再讀本文件確認目前專案結構與資料來源。

## 1. 根目錄定位

```text
/Users/wei/Desktop/code/POS/
```

此資料夾是 POS 專案唯一根目錄。舊模板資料已併入正式位置，不再保留獨立模板資料夾。

`reference/模塊化組件/` 只作為 AI 參考與匯出工具來源，不是 POS 正式源碼，也不是母體回寫位置。真正母體位於：

```text
/Users/wei/Desktop/code/模塊化組件/
```

## 2. 頂層結構

```text
POS/
├── AGENTS.md                    # AI Agent 第一入口
├── CLAUDE.md                    # Claude 規劃入口
├── ai-project-start.md          # AI 啟動順序與 Claude/Codex 分工
├── README.md                    # 專案快速說明
├── .agent/                      # 本地工作流與 Skill
├── .cursor/                     # Cursor 規則
├── backend/                     # Spring Boot Maven multi-module 後端
├── frontend-web/                # React + TypeScript + Vite 管理後台
├── docker/                      # Docker 本地部署設定
├── env/                         # 環境變數範本與本地 env 位置
├── reference/                   # AI 參考資料，不進正式源碼
├── scripts/                     # 專案輔助腳本
├── 需求/                         # 需求、規格、規劃與驗收文件
├── docs/                        # 專案導覽與長期維護文件
└── devlog/                      # 開發日誌與 troubleshooting
```

## 3. 後端結構

後端位於 `backend/`，使用 Maven multi-module 與 Spring Boot 3.2.4，Java 21。

```text
backend/
├── pom.xml
├── app/                         # Spring Boot 啟動模組
├── module-common/               # 共用基礎、回應格式、安全工具
├── module-auth/                 # 認證、RBAC、POS PIN/Terminal 擴展
├── module-organization/         # 公司、部門、員工、門店、終端
├── module-workflow/             # 審批流程
├── module-notification/         # 通知與 WebSocket
├── module-attendance/           # 考勤模組，目前 POS 預設停用
├── module-leave/                # 請假模組
├── module-pos-product/          # POS 商品、分類、套餐、價格規則
├── module-pos-core/             # POS 訂單、明細、退款、掛單、日結
├── module-pos-payment/          # 支付、錢櫃、對帳、金流設定
├── module-pos-tax/              # 稅別、稅規、電子發票
├── module-pos-inventory/        # 門店庫存、調撥、盤點、批號序號
└── module-pos-staff/            # POS 班次、排班、交接、X/Z 報表
```

主要設定：

- `backend/app/src/main/resources/application.yml`
- `backend/app/src/main/resources/application-retail.yml`
- `backend/app/src/main/resources/application-fastfood.yml`
- `backend/app/src/main/resources/application-restaurant.yml`
- `backend/app/src/main/resources/application-cafe.yml`
- `backend/app/src/main/resources/application-chain-hq.yml`

Flyway migration 依模組分散在各 `module-*/src/main/resources/db/migration/`。

## 4. 前端結構

前端位於 `frontend-web/`，使用 React 19、TypeScript 6、Vite 8、MUI 7、Zustand、Axios、React Router。

```text
frontend-web/src/
├── App.tsx
├── main.tsx
├── layouts/
├── shared/
│   ├── api/
│   ├── auth/
│   ├── components/
│   ├── hooks/
│   ├── i18n/
│   ├── store/
│   ├── theme/
│   ├── types/
│   └── utils/
└── features/
    ├── attendance/
    ├── auth/
    ├── leave/
    ├── notification/
    ├── organization/
    ├── workflow/
    ├── pos-auth/
    ├── pos-orders/
    ├── pos-products/
    ├── pos-payment/
    ├── pos-tax/
    ├── pos-inventory/
    └── pos-staff/
```

常用指令：

```bash
cd frontend-web
npm run build
npm run lint
npm run dev
```

## 5. Docker 與環境

本地 Docker 設定位於 `docker/local/`。

| 服務 | 容器名稱 | 對外埠 |
|------|----------|--------|
| Postgres | `pos-postgres` | `5432` |
| Redis | `pos-redis` | `6379` |
| Backend | `pos-backend` | `38080 -> 8080` |
| Frontend | `pos-frontend` | `38082 -> 80` |

環境變數範本：

```text
env/.env.example
```

本地實際環境檔：

```text
env/local/.env
docker/local/.env
```

上述本地環境檔不應提交。

## 6. 需求與規格文件

```text
需求/
├── README.md
├── AI協作交接規範.md
├── 需求文檔.md
├── 系統規格表.md
├── 技術規格.md
├── 開發流程規劃.md
├── 功能檢驗流程書.md
├── UIUX-設計提示詞.md
├── 流程圖/
└── 頁面結構/
```

使用方式：

- Claude 優先補齊需求、規格、規劃、驗收標準。
- Codex 根據文件自行設計實作、測試、提交與更新 devlog。
- 若規劃和現有架構衝突，Codex 應記錄 devlog 並說明調整方案。

## 7. Reference 與母體同步

初始化 reference：

```bash
bash scripts/setup-module-reference.sh
```

預設 reference tag：

```text
module-v2026.05.10.2
```

同步原則：

- POS 專案正式開發在 `/Users/wei/Desktop/code/POS/`。
- `reference/模塊化組件/` 可讀、可用匯出腳本，但不可當母體修改。
- 通用 bug 或可重用模組能力需回寫 `/Users/wei/Desktop/code/模塊化組件/`。

## 8. 忽略與產物

以下屬於本地或產物資料，應維持 ignored：

- `.DS_Store`
- `backend/*/target/`
- `frontend-web/node_modules/`
- `frontend-web/dist/`
- `env/local/`
- `docker/local/.env`
- `reference/模塊化組件/`

## 9. 快速驗證

```bash
bash -n scripts/setup-module-reference.sh
git diff --check
cd backend && mvn clean verify
cd frontend-web && npm run build
```

前端有 UI 修改時，除 build 外也應用瀏覽器實際操作驗證。
