# POS 導入交接

> 最後更新：2026-05-11 03:30 CST

本文件供新 AI、Claude、Codex 或人工開發者接手 POS 系統時使用。進度事實源仍以 `需求/開發進度對照.md` 為準。

## 1. 啟動順序

```bash
cd /Users/wei/Desktop/code/POS
git status --short --branch
git checkout feature
git pull origin feature
```

初始化模塊化組件 reference：

```bash
bash scripts/setup-module-reference.sh
```

準備本地環境變數：

```bash
cp env/.env.example env/local/.env
```

啟動 Docker 本地環境：

```bash
docker compose -f docker/local/docker-compose.yml up -d --build backend frontend
curl http://localhost:38080/actuator/health
```

本地服務：

| 服務 | URL |
|------|-----|
| Frontend | `http://localhost:38082` |
| Backend | `http://localhost:38080` |
| PostgreSQL | `localhost:5432` |
| Redis | `localhost:6379` |

POS demo 登入：

| 項目 | 值 |
|------|-----|
| PIN | `1234` |
| Store ID | `00000000-0000-0000-0000-000000000001` |
| Terminal ID | `00000000-0000-0000-0000-000000000101` |
| Employee ID | `00000000-0000-0000-0000-000000000201` |

## 2. 必跑測試

前端：

```bash
cd frontend-web
npm test
npm run lint
npm run build
```

後端：

```bash
cd backend
mvn -pl module-pos-core,module-pos-payment,module-pos-tax,module-pos-inventory,module-pos-staff -am test
mvn -pl app -am test
```

Docker：

```bash
docker compose -f docker/local/docker-compose.yml up -d --build backend frontend
curl http://localhost:38080/actuator/health
```

DB 查詢請依 `env/local/.env` 使用正確資料庫帳密；本地預設是 `pos_user` / `pos_db`。

常用 POS 驗證表：

| 資料 | 表 |
|------|----|
| 訂單 | `pos_orders`、`pos_order_items` |
| 付款 | `pos_payment_transactions`，現金實收欄位為 `tendered` |
| 發票 | `pos_invoices` |
| 庫存 | `pos_inv_store_stock`、`pos_inv_stock_movements` |
| 對帳 | `pos_reconciliation` |

## 3. 已完成閉環

已可驗證的 POS 主流程：

```text
PIN 登入 → 商品載入 → 加入購物車 → 折扣或會員 MVP →
掛單/取回 → 現金付款 → 訂單完成 →
payment transaction → mock invoice/tax →
inventory 扣庫存 → staff shift/cash drawer/reconciliation →
訂單查詢 → 全額退款與發票作廢
```

Checkpoint 完成狀態：

| Checkpoint | 狀態 | 備註 |
|------------|------|------|
| 1 Payment Transaction | 完成 | CASH 付款會建立 `pos_payment_transactions`，含 paid/tendered/change/order/store/terminal/employee |
| 2 Tax / Invoice | 完成 | 訂單完成後建立 mock invoice，金額與 `tax_total` 對齊 |
| 3 Inventory | 完成 | 完成訂單扣減庫存，庫存不足會阻止付款 |
| 4 Staff / Cash Drawer / Reconciliation | 完成 | 開班、SALE event、關班與每日 CASH 對帳可查 |
| 5 Orders / Refund | 完成 | 訂單列表顯示付款/發票狀態，全額退款會作廢發票 |
| 6 Test Baseline | 完成 | Vitest/RTL 與後端 controller 測試基線已建立 |
| 7 Handoff Docs | 完成 | 啟動、測試、導入交接與風險整理 |

## 4. MVP 與待正式化項目

| 項目 | 目前狀態 | 導入前建議 |
|------|----------|------------|
| 會員 | 前端 demo 會員與 10% 折扣 MVP | 建立 `module-pos-crm` 後端、會員查詢、點數、儲值與消費歷史 |
| 促銷 | 手動折扣與會員折扣互斥 | 建立促銷引擎與折扣來源審計 |
| 電子發票 | mock invoice happy path | 補正式字軌、Turnkey 串接、上傳錯誤補償 |
| 庫存 | 收銀扣庫與不足攔截完成 | 補完整異動查詢 UX、盤點、調撥與進階補貨回歸 |
| 支付 | CASH 已正式化 | 補多支付方式、金流 gateway 與失敗補償情境 |
| 硬體 | 未正式串接 | 補列印、錢箱、發票機與硬體事件 |
| 測試 | 核心基線完成 | 擴大 MSW/API 整合、更多頁面與退款/庫存異常測試 |

## 5. 文件入口

| 文件 | 用途 |
|------|------|
| `AGENTS.md` | POS AI Agent 入口與分支規則 |
| `需求/開發進度對照.md` | 目前進度事實源 |
| `需求/開發流程規劃.md` | 長期 phase / sprint 規劃 |
| `需求/功能檢驗流程書.md` | 功能驗收流程 |
| `devlog/2026-05-11-devlog.md` | 本輪 checkpoint 實作與驗證記錄 |
| `devlog/troubleshooting.md` | 已遇到的問題與修復方式 |
| `docker/local/README.md` | 本地 Docker 啟動與維護指令 |

## 6. 母體同步評估

本輪 Checkpoint 1-7 的變更主要是 POS 專案串接、測試與文件整理，沒有把通用模組 bug 修正回寫到 `/Users/wei/Desktop/code/模塊化組件/` 的必要項目。

若後續抽出通用能力，需先確認是否屬於專案無關的模組能力，再依 `reference/模塊化組件/ai-handoff.md` 回寫真正母體倉庫，不得把 POS 專屬 UI、demo seed 或專案資料硬搬進母體。
