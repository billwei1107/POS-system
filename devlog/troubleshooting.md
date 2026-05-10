# 問題追蹤日誌 (Troubleshooting Log)

> 本文件專門記錄開發過程中遇到的問題與解決方案。
> 遇到新問題時，請先搜尋本文件是否有類似問題的紀錄。

---

# 2026-05-11 POS inventory 訂單完成未扣庫存與超賣問題

## Issue

- 場景：Checkpoint 3 驗證「訂單完成後扣庫存」時，檢查既有 `module-pos-inventory`。
- 問題：
  - `InventoryEventListener` 雖監聽 `OrderCompletedEvent`，但只在 after-commit log，沒有查詢 `pos_order_items` 並扣減 `pos_inv_store_stock`。
  - `StockDeductionService.deductForSale` 在庫存不足時只記錄 warn，仍允許扣到負數。
  - 銷售扣庫找不到庫存紀錄時會自動建立 0 庫存再扣成負數。
  - `/pos/inventory` route 使用靜態 placeholder 頁，不會顯示真實庫存。

## Solution

- `InventoryEventListener` 改用同步 `@EventListener` 在訂單完成交易內扣庫，庫存不足會讓 complete order rollback，避免 payment transaction / invoice 落地。
- 事件監聽器查詢 order items，並只扣 `ProductItem.trackInventory=true` 的商品。
- `StockDeductionService` 銷售扣庫改為要求既有庫存，並以 available quantity 攔截不足庫存，回傳 HTTP 409 業務錯誤。
- Demo seeder 將 demo 商品設為追蹤庫存，並建立本地初始庫存。
- `/pos/inventory` 改接 `StockOverviewPage`，修正 inventory API response 取值與預設門店 fallback。

## Verification

- `mvn -pl module-pos-inventory,module-pos-core,module-pos-product -am test`：通過。
- `mvn -pl app -am test`：通過。
- `npm run lint`、`npm run build`：通過。
- Docker backend/frontend 重建後 health 為 UP。
- Playwright：
  - 拿鐵現金收款 130 結帳成功，庫存頁顯示 99.00。
  - 美式咖啡庫存設為 0 後結帳，前端顯示「庫存不足」，payment transaction 與 completed order 數未增加。

---

<!--
  紀錄格式範例：

  ## YYYY-MM-DD HH:MM — [問題標題]

  ### 問題描述
  - 場景：...
  - 錯誤訊息：
    ```
    [貼上錯誤訊息或 Stack Trace]
    ```

  ### 原因分析
  - ...

  ### 解決方案
  - ...

  ### 驗證結果
  - ...

  紀錄時間：HH:MM

  ---
-->
# 2026-05-10 前端瀏覽器回歸問題

## Issue

- POS 前端瀏覽器測試時發現多個功能問題：
  - 請假與 POS API 端點出現 `/api/api/...` 重複前綴，導致後端回傳 500。
  - 通知 WebSocket 連到 `http://localhost:8080/ws/notifications`，瀏覽器端無法連線。
  - POS 手機版購物車 Drawer 開啟後內容空白。
  - POS 側邊選單含 Dashboard / Settings，但實際沒有註冊路由，點擊後被 wildcard 導回 `/pos/register`。
  - POS Orders 在缺少 `VITE_DEFAULT_STORE_ID` 時送出 `storeId=`，後端 UUID 解析失敗並回 500。

## Solution

- 將 attendance、leave、POS order、inventory、payment、staff、tax API 常數統一改為 `/v1/...`，交由 `axiosInstance` 的 baseURL `/api` 組合完整路徑。
- 將通知 WebSocket 預設 URL 改為同源 `/ws/notifications`，並在 `docker/local/nginx.conf` 新增 `/ws/` proxy 至 backend。
- 在 `RegisterPage` 使用 `MutationObserver` 重新偵測 `cart-root` / `cart-root-mobile`，確保手機 Drawer DOM 掛載後 Cart portal 會渲染。
- 在 `App.tsx` 補上 `/pos/orders` 路由，並移除 POS 側邊選單中尚未實作的 Dashboard / Settings 入口。
- 在 `OrderListPage` 缺少 `VITE_DEFAULT_STORE_ID` 時提前顯示前端提示，不再呼叫後端 API。

## Verification

- `npm run build`：通過。
- `docker compose up -d --build frontend`：通過。
- 瀏覽器回歸測試確認：
  - API 未再出現 `/api/api`
  - 未捕捉到 API 500
  - WebSocket 未再連到 `localhost:8080`
  - POS 手機版購物車可正常顯示 Current Order
  - POS Orders 顯示缺少預設門店提示，不再造成後端錯誤

---

# 2026-05-10 POS 手機購物車按鈕測試定位失敗

## Issue

- 場景：POS 中文優先介面回歸測試時，手機尺寸 `/pos/register` 頁面需要點擊頂部購物車按鈕。
- 錯誤訊息：

```text
locator.waitFor: Timeout 8000ms exceeded.
waiting for locator('button').filter({ has: locator('svg[data-testid="ShoppingCartIcon"]') }).first() to be visible
```

## Solution

- 實際檢查手機 DOM 後確認購物車內容已正常掛載，但頂部購物車 IconButton 沒有可讀取的 `aria-label`，測試只能依賴不穩定的圖示 selector。
- 在 `PosLayout.tsx` 為手機導覽按鈕補上 `aria-label="開啟導覽選單"`。
- 在 `PosLayout.tsx` 為購物車按鈕補上依狀態切換的 `aria-label`：`開啟購物車` / `關閉購物車`。

## Verification

- `npm run build`：通過。
- `git diff --check`：通過。
- `docker compose up -d --build frontend`：通過。
- Playwright 手機回歸測試改用 `getByLabel('開啟購物車')` 後通過，可打開購物車並看到「目前訂單」與「立即結帳」。

---

# 2026-05-10 商品/分類管理未登入 API 403 與 health endpoint 問題

## Issue

- 場景：接續 POS 商品與分類管理頁開發並以瀏覽器測試 `/pos/products`、`/pos/categories`。
- 問題：
  - 商品/分類頁未登入時仍自動呼叫後端 API，造成瀏覽器 console/request 產生 403。
  - `/actuator/health` 起初回傳 403；放行後又因 app 缺少 Actuator dependency，回傳 `No static resource actuator/health`。
  - `npm run lint` 暴露既有 `any`、React Hooks dependency、set-state-in-effect、refs 與 purity 問題。

## Solution

- 商品與分類頁接入 `useAuthStore`，未登入時跳過 API 呼叫、顯示登入提示並停用新增操作。
- 商品與分類頁改用共用 `PageHeader`、`DataTable`、`StatusChip`、`ConfirmDialog`，並調整 dialog form 初始化方式，避免 effect 內同步 setState。
- `SecurityConfig` 放行 `/actuator/health` 與 `/actuator/health/**`。
- `backend/app/pom.xml` 加入 `spring-boot-starter-actuator`。
- 清理前端型別與 hooks lint 問題，將可共用修正同步回母體 `/Users/wei/Desktop/code/模塊化組件/`。

## Verification

- POS 專案：`npm run lint`、`npm run build`、`mvn -pl module-pos-product -am test`、`mvn -pl module-common -am test`、`mvn -pl app -am test -DskipTests` 均通過。
- Docker：`docker compose up -d --build backend frontend` 通過。
- Health check：`curl -fsS http://localhost:38080/actuator/health` 回傳 `{"status":"UP"}`。
- 瀏覽器：商品/分類管理頁未登入時顯示提示、停用新增按鈕，且未再自動打出商品/分類 API 403。
- 母體：frontend lint/build、`module-common` test、`app` compile 均通過。

---

# 2026-05-10 收銀台 API 回應層與 Zustand selector 問題

## Issue

- 場景：將 `/pos/register` 從 mock 商品改成讀取 `pos-products` API，並用本地 cart store 串起購物車與 checkout。
- 問題：
  - 後端 `ApiResponse` 實際格式為 `code/message/data`，前端型別與頁面以 `success` 判斷，導致 API 200 但 UI 仍顯示「目前沒有可銷售商品」。
  - `productApi` / `orderApi` 在 axios interceptor 已回傳 body 後又 `.then(res => res.data)`，造成資料層被多取一次。
  - `useCartStore((state) => state.totals())` 每次 selector 都回傳新物件，在 Zustand v5 + React production build 觸發最大更新深度錯誤。

## Solution

- 在 `axiosInstance` response interceptor 中，對含 `code` 的後端回應補上 `success` 布林值。
- 將 `loginApi` 改為讀取 `ApiResponse<LoginResponse>.data`。
- 將 `productApi` 與 `orderApi` 改為使用 axios generic overload 回傳完整 `ApiResponse<T>`，不再多取一次 `.data`。
- 將 cart totals 與 item count 抽成純函式，元件內用 `useMemo` 基於 `lines/taxRate` 計算，避免 selector 回傳不穩定物件。

## Verification

- `npm run lint`：通過。
- `npm run build`：通過。
- `docker compose up -d --build frontend`：通過。
- Playwright smoke：
  - 登入狀態 `/pos/register` 可載入 API 建立的測試商品。
  - 點選商品後購物車加減數量正常。
  - `/pos/checkout` 顯示同一筆商品與正確 `$126` 應收金額。
  - 未登入狀態 `/pos/register` 顯示登入提示，未再打出商品 API 403。

---

# 2026-05-10 POS checkout 付款完成流程後端交易問題

## Issue

- 場景：Checkout 頁接上真實 `pos-orders` API 後，以 Playwright 執行「收銀台加入商品 → checkout → 確認付款」。
- 問題：
  - `completeOrder` 第一次回 500，錯誤為 `illegal transition CONFIRMED → READY`。
  - 補狀態後仍回 500，錯誤為 `Transaction silently rolled back because it has been marked as rollback-only`。
  - 訂單付款完成後進入列表時，`GET /api/v1/pos/orders?...` 回 500，PostgreSQL 錯誤為 `could not determine data type of parameter $4`。

## Solution

- `OrderService.completeOrder` 改為透過 `advanceToCompleted` 依序推進 `DRAFT → CONFIRMED → PREPARING → READY → COMPLETED`。
- 為 `OrderService.completeOrder` 新增 unit test，驗證付款金額、找零、完成時間、付款紀錄與事件發布。
- 將 `PaymentService`、`InvoiceService`、`InventoryEventListener`、`StaffEventListener` 的訂單完成/退款/作廢事件監聽改為 `@TransactionalEventListener(phase = AFTER_COMMIT)`，並使用 `@Transactional(propagation = REQUIRES_NEW)`，避免周邊副作用污染核心訂單交易。
- 將 `OrderRepository` 擴充 `JpaSpecificationExecutor`，`OrderService.listByStore` 改用 Specification 動態產生 store/status/from/to 條件，避免 nullable JPQL 參數造成 PostgreSQL 型別推斷失敗。

## Verification

- `mvn -pl module-pos-core,module-pos-payment,module-pos-inventory,module-pos-staff,module-pos-tax -am test`：通過。
- `docker compose up -d --build backend`：通過。
- `curl http://localhost:38080/actuator/health`：通過，回傳 `{"status":"UP"}`。
- Playwright checkout payment smoke：通過，建立訂單、完成付款、清空購物車、跳轉訂單列表並看到 `COMPLETED` 訂單。

---

# 2026-05-10 POS PIN 登入 demo seed 與瀏覽器驗證問題

## Issue

- 場景：將 POS PIN 登入從前端模擬改為真實 `/api/v1/pos/auth/pin-login`，並以 Docker + Playwright 測試。
- 問題：
  - `backend/app` 新增 demo seeder 時使用 Lombok 註解，但 app 模組沒有 Lombok dependency，導致編譯失敗。
  - `Terminal.hardwareProfileJson` 對應 PostgreSQL `jsonb` 欄位，Docker 實啟時用 String 寫入造成 `column "hardware_profile_json" is of type jsonb but expression is of type character varying`。
  - Demo seed 重啟時遇到 `org_companies_code_key`、`org_employees_employee_no_key` 等唯一鍵衝突。
  - `BaseEntity` 使用 `@GeneratedValue(strategy = GenerationType.UUID)`，手動指定 demo terminal UUID 不可靠，落庫後仍可能是自動產生 UUID。
  - 錯誤 PIN 回 401 時，全域 axios interceptor 直接導向 `/login`，POS PIN 頁無法顯示自己的錯誤提示。

## Solution

- `PosDemoDataSeeder` 改用手寫 constructor 與 `LoggerFactory`，不在 app 模組依賴 Lombok。
- `Terminal.hardwareProfileJson` 補上 `@JdbcTypeCode(SqlTypes.JSON)`。
- Demo seed 改用唯一代碼冪等查找：company code、store code、terminal code、employee no、username、active PIN 與 active terminal token。
- `PinLoginRequest` 支援 `terminalCode`；`PosAuthServiceImpl` 先解析 `terminalCode` 成實際 terminal UUID，再檢查 terminal token。
- `PosLoginPage` 預設送 `terminalCode=DEMO-T-001`，成功後保存後端回傳的實際 `terminalId` 與 `terminalCode`。
- `axiosInstance` 將 `/v1/pos/auth/**` 從全域 401 導頁排除，讓 POS PIN 頁自行處理 `Invalid PIN`。

## Verification

- `mvn -pl app -am test -DskipTests`：通過。
- `mvn -pl module-auth -am test`：通過。
- `npm run build`：通過。
- `docker compose up -d --build backend frontend`：通過。
- `curl` 正確 PIN 回 200，錯誤 PIN 回 401。
- Playwright 驗證正確 PIN 進入 `/pos/register` 並看到「收銀台」，錯誤 PIN 停留 `/pos/login` 並顯示 `Invalid PIN`。

---

# 2026-05-10 收銀台 demo 商品不可見與商品卡擁擠

## Issue

- 場景：POS PIN 登入後進入 `/pos/register`，資料庫已有 demo 商品，但畫面首屏仍主要顯示舊 smoke/test 商品。
- 問題：
  - 商品 API 未指定前端顯示排序，舊測試資料容易排在 demo 商品之前，導致使用者以為「沒看到商品」。
  - 原商品卡以大面積 SKU 占位圖為主，品名、價格與條碼被擠在下方；在大量測試資料下看起來像商品都擠成一團。

## Solution

- 在 `RegisterPage.tsx` 新增收銀台排序規則：
  - `DEMO-` SKU 商品優先。
  - 「咖啡飲品」「烘焙點心」分類優先。
  - 其餘商品再依中文品名排序。
- 將商品卡改為固定高度資訊卡，移除無圖片時的大 SKU 占位圖，改成分類、Demo 標籤、SKU、品名、價格、條碼與加入購物車圖示的穩定區塊。
- 調整商品 grid 最小欄寬與卡片 `minHeight`，避免內容因卡片高度不足被壓縮。

## Verification

- `npm run build`：通過。
- `docker compose up -d --build frontend`：通過。
- Playwright 驗證 PIN `1234` 登入後商品/分類 API 均回 200。
- `/pos/register` 首排顯示 demo 商品「奶油可頌」「美式咖啡 12oz」「拿鐵 12oz」「燕麥拿鐵 12oz」，首排卡片約 `267x232`，內容完整可見。

---

# 2026-05-10 Docker 重啟後 POS 頁面停留但登入狀態不一致

## Issue

- 場景：開發時重建或重啟 Docker frontend 後，瀏覽器仍停留在原本的 `/pos/register` 頁面。
- 問題：
  - 未登入時 POS 路由沒有被路由守衛強制導向 PIN 登入頁，使用者會留在收銀頁看到空狀態或載入不到商品。
  - 已登入狀態雖有 Zustand persist，但路由與登入頁沒有統一 redirect 邏輯，重整或重新進入登入頁時體驗不一致。
  - 後台「登出」若只是導到 `/login`，在登入頁自動避開已登入使用者後會無法真正登出。

## Solution

- 使用 `ProtectedRoute` 包住後台與 `/pos/*` 路由，POS 未登入時導向 `/pos/login?redirect=<原路徑>`。
- `ProtectedRoute` 改為透過 query string 保存 redirect，與既有登入流程一致。
- `PosLoginPage` 登入成功後讀取 redirect，回到原本要進入的 POS 頁面；若已登入再進 `/pos/login`，自動返回 POS 頁面。
- `LoginPage` 已登入時自動導回 redirect 或預設 `/pos/register`。
- `authStore.logout` 同步清除 `pos-session`，並修正 persisted state merge，確保有 user/token 時才視為已登入。
- 後台「登出」與 POS「鎖定終端」改為先清除登入狀態，再導向登入頁。

## Verification

- `npm run build`：通過。
- `docker compose up -d --build frontend`：通過。
- Playwright 驗證：
  - 未登入 `/pos/register` 會導向 `/pos/login?redirect=%2Fpos%2Fregister`。
  - PIN `1234` 登入後 localStorage 同時存在 `auth-storage` 與 `pos-session`。
  - frontend 容器重啟後 reload 仍停留 `/pos/register` 且商品可見。
  - 清除登入資料後再進 `/pos/register` 會重新導向 POS PIN 登入頁。

## Update 2026-05-10 23:37

- 追加修正：`authStore` 新增 `hasHydrated`，`ProtectedRoute`、`LoginPage`、`PosLoginPage` 在 localStorage 還原完成前不做導頁判斷，避免刷新或 frontend rebuild 後先被誤判未登入。
- 追加修正：local Docker `JWT_EXPIRATION` 從 15 分鐘調整為 24 小時，避免開發中 token 過短造成反覆登入。
- 追加驗證：
  - `npm run build`：通過。
  - `docker compose up -d --build backend frontend`：通過。
  - Playwright 保持同一頁面登入後執行 `docker compose restart backend frontend`，服務回來後 reload 仍停留 `/pos/register` 且商品可見。
  - 新簽發 token 剩餘時間約 24 小時。

---

# 2026-05-11 POS 折扣結帳找零出現 0.40

## Issue

- 場景：收銀台加入「拿鐵 12oz」後套用 10% 折扣，畫面顯示應收 `$113`。
- 瀏覽器自測付款完成後，資料庫最新訂單顯示 `discount_total=12.00`、`tax_total=5.40`、`rounding_adj=-0.40`、`grand_total=113.00`，但 `change_given=0.40`。
- 同時發現 `docker compose -f docker/local/docker-compose.yml up -d --build frontend` 會因 `frontend.depends_on=backend` 一併 build backend，後端 Docker build 又因 Maven DNS 暫時解析不到 `repo.maven.apache.org` 失敗，導致 frontend 沒有成功套用新版 bundle。

## Root Cause

- 原始碼已將前端 `calculateCartTotals` 的 `total` 改成 TWD 整數金額，但 Docker frontend 容器仍跑舊 bundle。
- 舊 bundle 雖然 `formatMoney` 顯示 `$113`，但付款 API 實際送出的現金金額仍是未四捨五入的 `113.40`。
- 後端訂單 `grand_total` 是四捨五入後的 `113.00`，因此計算出 `change_given=113.40-113.00=0.40`。

## Solution

- 只 build frontend image：
  - `docker compose -f docker/local/docker-compose.yml build frontend`
- 不帶依賴重啟 frontend：
  - `docker compose -f docker/local/docker-compose.yml up -d --no-deps frontend`
- 重新執行 Playwright 端到端流程，確認前端付款送出的金額與畫面 `$113` 一致。

## Verification

- `docker compose -f docker/local/docker-compose.yml build frontend`：通過。
- `docker compose -f docker/local/docker-compose.yml up -d --no-deps frontend`：通過。
- Playwright 實測登入、加入商品、套用 10% 折扣、進入結帳、確認付款：通過。
- 資料庫最新訂單：`discount_total=12.00`、`tax_total=5.40`、`rounding_adj=-0.40`、`grand_total=113.00`、`paid_total=113.00`、`change_given=0.00`。

---

# 2026-05-11 POS payment transaction 瀏覽器測試 PIN 送出定位問題

## Issue

- 場景：為 Sprint 1-3 payment transaction 執行 Playwright 瀏覽器端到端測試。
- 問題：PIN `1234` 輸入後，測試腳本用「最後一個含 svg 的 button」定位送出鍵，實際點到其他圖示按鈕，導致等待 `/pos/register` 逾時。

## Root Cause

- POS PIN 頁面有多個純圖示按鈕，送出鍵沒有文字或 aria-label。
- 以 `svg` 或最後一個圖示按鈕作為 selector 不穩定。

## Solution

- 先列出 PIN 頁面所有 button，確認送出鍵是 keypad 第 12 個按鈕。
- 測試腳本改用 `page.locator('button').nth(11)` 點擊送出鍵。
- 後續建議為 PIN 送出鍵補 `aria-label`，讓自動化測試可用語意 selector。

## Verification

- 重新執行瀏覽器測試後，PIN `1234` 成功導向 `/pos/register`。
- 完整 payment transaction 流程通過：登入、加商品、結帳、現金付款、前端查付款記錄、DB 查 `pos_payment_transactions`。

---

# 2026-05-11 POS tax mock invoice 無字軌時未保存

## Issue

- 場景：Checkpoint 2 進行 Tax / Invoice happy path，瀏覽器完成現金付款後，DB `pos_orders` 有完成訂單，但 `pos_invoices` 沒有對應資料。
- 後端 log 顯示 `TransactionSynchronization.afterCompletion threw exception` 與 `UnexpectedRollbackException: Transaction silently rolled back because it has been marked as rollback-only`。

## Root Cause

- `InvoiceService.uploadAndSave` 透過 `InvoiceTrackService.getTrackForStore()` 查字軌。
- 本地 demo 沒有建立 `pos_invoice_tracks` 時，`InvoiceTrackService` 的 `@Transactional` 方法拋出 `IllegalStateException`。
- 即使外層 catch 例外，該交易已被 Spring 標記 rollback-only，導致 mock invoice 保存被整筆回滾。

## Solution

- `InvoiceService` 改為在同一交易中透過 `InvoiceTrackRepository.findAvailableTrackForUpdate` 查可用字軌。
- 無字軌時不拋例外，直接保存 `fullInvoiceNo=null` 的 mock invoice。
- 有字軌時在 `InvoiceService` 內配發下一號並保存 track，避免無字軌路徑污染開票交易。

## Verification

- `mvn -pl module-pos-tax,module-pos-core -am test`：通過。
- Docker backend 重建後，Playwright 完成現金訂單，DB `pos_invoices` 有對應資料。
- 驗證訂單 `000000-20260510183637-6093`：`pos_orders.tax_total=6.00`，`pos_invoices.tax_amount=6.00`。

---

# 2026-05-11 POS 發票頁 Docker 查詢缺 storeId

## Issue

- 場景：發票已在 DB 建立，但 `/pos/invoices` 點擊「查詢」顯示「查詢失敗」。
- 瀏覽器網路回應：`/api/v1/pos/invoices?from=...&to=...` 回 500，訊息為 `Required request parameter 'storeId' ... is not present`。

## Root Cause

- `InvoicePage`、`TaxClassSettingsPage`、`InvoiceTrackPage` 直接讀 `import.meta.env.VITE_DEFAULT_STORE_ID as string`。
- Docker frontend build 未注入該 env 時，`STORE_ID` 為 `undefined`，axios params 省略 `storeId`。
- 收銀流程已有 `DEFAULT_STORE_ID` fallback，但 tax 頁面未共用。

## Solution

- tax 三個頁面改用 `../../pos-orders/config` 的 `DEFAULT_STORE_ID`。
- `taxApi` 改用共用 `axiosInstance`，確保 baseURL 與 JWT interceptor 與其他 POS 模組一致。
- 頁面讀取 axios interceptor 正規化後的 `res.data`。

## Verification

- `npm run lint && npm run build`：通過。
- `docker compose -f docker/local/docker-compose.yml up -d --build frontend`：通過。
- Playwright 登入後進入 `/pos/invoices`，查詢 API 帶 `storeId=00000000-0000-0000-0000-000000000001` 且回 200。
- 畫面可見稅前 `NT$ 120.00`、稅額 `NT$ 6.00`、總額 `NT$ 126.00`。

---

# 2026-05-11 POS staff API response 型別與頁面讀取不一致

## Issue

- 場景：Checkpoint 4 串接班次、錢櫃與對帳頁後執行 `npm run build`。
- 問題：`ZReportPage` 仍以舊型別讀取 staff report API 回應，與 axios interceptor 實際回傳的 `ApiResponse` body 不一致，造成 TypeScript build 失敗。

## Root Cause

- `staffApi` 舊型別宣告為 `{ data: T }`，但專案共用 `axiosInstance` interceptor 會先抽出 response body。
- `ShiftPage` 與 `ReconciliationPage` 已依 `ApiResponse<T>` 修正，但 `ZReportPage` 仍讀取錯誤層級。

## Solution

- `frontend-web/src/features/pos-staff/api/staffApi.ts` 改為回傳 `ApiResponse<T>`。
- `ZReportPage` 改讀 `res.data ?? []`，並統一使用 `DEFAULT_STORE_ID`、`DEFAULT_EMPLOYEE_ID` fallback。
- `ShiftPage` 與 `ReconciliationPage` 同步維持 `ApiResponse<T>` 讀取方式。

## Verification

- `npm run lint`：通過。
- `npm run build`：通過，僅保留 Vite chunk size warning。
- Docker frontend 重建後，`/pos/shifts` 與 `/pos/reconciliation` 可正常開啟與查詢。

---

# 2026-05-11 POS Checkpoint 6 測試基線環境問題

## Issue

- 場景：建立前端 Vitest/RTL 與後端 controller 測試基線。
- 問題：
  - Vitest/jsdom 執行 auth store 測試時出現 `window.localStorage.clear is not a function`。
  - `InvoiceControllerTest` 初版使用 `Invoice.CarrierType.NONE`，編譯失敗。
  - Playwright POS 登入腳本以通用 button selector 送出 PIN 時等待 `/pos/register` 逾時。
  - 發票頁與對帳頁 E2E 初版只進頁面未點擊「查詢」或「產生對帳」，導致驗證不到資料。
  - Docker PostgreSQL 驗證初版使用 `psql -U pos -d pos`，回傳 `role "pos" does not exist`。

## Root Cause

- 測試 runtime 的 `localStorage` 與瀏覽器實作不完全一致，需要測試 setup 明確 mock。
- `CarrierType` enum 只有專案實際支援的載具值，不存在 `NONE`。
- POS PIN 頁面多個圖示按鈕缺少穩定語意 selector，自動化測試用最後一個 button 容易點錯。
- 發票與對帳頁設計為手動查詢，E2E 必須符合使用者實際操作。
- local Docker DB 帳密以 `env/local/.env` 為準，實際為 `DB_USER=pos_user`、`DB_NAME=pos_db`。

## Solution

- 在 `frontend-web/test/setup.ts` 建立可用的 `localStorage` mock，並於每個測試後 cleanup。
- `InvoiceControllerTest` 的 `carrierType` 使用 `null`，符合無載具情境。
- Playwright 測試腳本先定位 PIN keypad 送出鍵；後續建議補 aria-label 提升測試穩定性。
- 發票頁 E2E 加入點擊「查詢」，對帳頁 E2E 加入點擊「產生對帳」。
- DB 驗證改依 `env/local/.env` 使用 `pos_user` / `pos_db`。

## Verification

- `npm test`：通過，4 files / 8 tests passed。
- `mvn -pl module-pos-core,module-pos-payment,module-pos-tax,module-pos-inventory,module-pos-staff -am test`：通過。
- `mvn -pl app -am test`：通過。
- Playwright 完整 E2E 通過：PIN 登入、商品、現金付款、訂單列表、發票頁、對帳頁。
- DB 驗證最新訂單、payment transaction、invoice、reconciliation 與 inventory 皆可查。
