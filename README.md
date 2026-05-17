# POS System

POS System 是基於企業模塊化架構建置的 POS (Point of Sale) 系統，涵蓋 Spring Boot 後端 API、React 管理後台、Docker 本地部署、需求規格與 AI 協作流程。

POS 專案根目錄固定為本資料夾：

```text
/Users/wei/Desktop/code/POS/
```

舊模板資料已併入正式位置；後續不要再以模板資料夾作為專案根目錄。

## 技術棧

| 層級 | 技術 |
|------|------|
| 後端 | Spring Boot 3.2.4 / Java 21 / Maven multi-module |
| 資料庫 | PostgreSQL 15 / Flyway |
| 快取 | Redis 7 |
| 前端 Web | React 19 / TypeScript 6 / Vite 8 / MUI 7 / Zustand |
| 前端 App | Flutter 3.41 / Dart 3.11 / Android SDK 36 |
| 部署 | Docker Compose |
| 模塊 reference | `billwei1107/module` tag `module-v2026.05.10.4` |

## 目錄結構

```text
POS/
├── AGENTS.md                 # AI Agent 第一入口
├── CLAUDE.md                 # Claude 規劃入口
├── ai-project-start.md       # Claude 規劃與 Codex 執行的啟動流程
├── docs/project-map.md       # 整個資料夾結構與維護地圖
├── .agent/                   # 專案工作流與本地 Skill
├── .cursor/                  # Cursor 專案規則
├── backend/                  # Spring Boot 後端模塊
├── frontend-web/             # React 管理後台與 Web POS
├── frontend-app/             # Flutter Android-first POS 終端
├── docker/                   # Docker 部署設定檔
├── env/                      # 環境變數範本與本地 env 位置
├── 需求/                      # Claude 規劃與 Codex 執行交接文件
├── reference/                # AI 參考資料，不作為正式源碼
├── scripts/                  # 專案輔助腳本
└── devlog/                   # 開發日誌
```

完整資料夾地圖請看 `docs/project-map.md`。

## 快速啟動

1. 初始化模塊化組件 reference：

```bash
bash scripts/setup-module-reference.sh
```

2. 複製並設定本地環境變數：

```bash
cp env/.env.example env/local/.env
```

3. 啟動本地 Docker 服務：

```bash
cd docker/local
docker compose up -d
```

4. 後端驗證：

```bash
cd backend
mvn clean verify
```

5. 前端驗證：

```bash
cd frontend-web
npm run build
npm run dev
```

6. Flutter Android POS 驗證：

```bash
cd frontend-app
flutter analyze
flutter test
flutter build apk --debug
flutter emulators --launch pos_android_tablet
flutter run -d emulator-5554
```

本地服務預設：

| 服務 | URL |
|------|-----|
| Backend | `http://localhost:38080` |
| Frontend | `http://localhost:38082` |
| PostgreSQL | `localhost:5432` |
| Redis | `localhost:6379` |

POS demo 登入：

| 項目 | 值 |
|------|-----|
| PIN | `1234` |
| 門店 | `00000000-0000-0000-0000-000000000001` |
| 終端 | `00000000-0000-0000-0000-000000000101` |
| 員工 | `00000000-0000-0000-0000-000000000201` |

## 目前完成狀態

截至 2026-05-17，POS Web 主流程已完成到可驗證閉環，Flutter Android POS 終端已建立第一版骨架：

```text
PIN 登入 → 商品載入 → 加入購物車 → 折扣/會員 MVP → 掛單/取回 →
現金付款 → payment transaction → mock invoice/tax →
inventory 扣庫存 → staff shift/cash drawer/reconciliation → 訂單查詢/退款
```

Flutter app 目前已完成 Android-first scaffold、平板收銀首屏、demo 商品加入購物車、金額試算、widget tests 與 debug APK build；後續會逐步接 PIN auth、API、SQLite/SQLCipher 離線快取與 sync engine。

正式化狀態以 `需求/開發進度對照.md` 為準。仍屬 MVP 或待正式化的項目包括：CRM 會員後端、促銷引擎、正式電子發票字軌/Turnkey、完整盤點/調撥、硬體列印/錢箱、多支付方式與更完整日結情境。

## 測試與驗證

前端：

```bash
cd frontend-web
npm test
npm run lint
npm run build
```

Flutter app：

```bash
cd frontend-app
flutter analyze
flutter test
flutter build apk --debug
```

後端 POS 核心模組：

```bash
cd backend
mvn -pl module-pos-core,module-pos-payment,module-pos-tax,module-pos-inventory,module-pos-staff -am test
mvn -pl app -am test
```

Docker 本地環境：

```bash
docker compose -f docker/local/docker-compose.yml up -d --build backend frontend
curl http://localhost:38080/actuator/health
```

資料庫驗證需使用 `env/local/.env` 的 `DB_USER` / `DB_NAME`，本地預設為 `pos_user` / `pos_db`。

更多導入交接、測試清單與人工確認事項請看 `docs/production-handoff.md`。

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

## Reference 與母體

- `reference/模塊化組件/`：專案內參考資料與匯出工具來源，ignored，不是正式源碼。
- `/Users/wei/Desktop/code/模塊化組件/`：真正母體倉庫，通用 bug 與可重用能力需回寫這裡。
- POS 專案正式開發位置永遠是 `/Users/wei/Desktop/code/POS/`。
