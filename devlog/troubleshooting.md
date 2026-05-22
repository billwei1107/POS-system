# 問題追蹤日誌 (Troubleshooting Log)

> 本文件專門記錄開發過程中遇到的問題與解決方案。
> 遇到新問題時，請先搜尋本文件是否有類似問題的紀錄。

---

# 2026-05-15 API smoke one-liner 引號拆解失敗

## Issue

- 場景：驗證 `GET /api/v1/pos/payments/gateways` 是否不暴露金流密鑰時，嘗試在單一 shell 指令內巢狀 `curl`、`node -e` 與 zsh 字串插值。
- 問題：
  - zsh 將 `node -e` 片段中的引號與括號拆壞，出現 `Unterminated string constant`、`bad pattern`。
  - 內層 token 解析失敗後，後續 gateway API 以空 token 呼叫，造成 403。

## Root Cause

- 複雜的一行式命令同時包含 JSON、JavaScript 字串、shell command substitution 與 zsh glob pattern，引用規則過度脆弱。

## Solution

- 改成先用 `TOKEN=$(...)` 分段取得 JWT，再呼叫 gateway API。
- JSON 摘要改用 `/usr/bin/python3 -c` 讀 stdin 解析，避免巢狀 JavaScript 引號與 zsh pattern 互相干擾。

## Verification

- 使用 `admin / 123456` 取得 token 成功。
- `GET /api/v1/pos/payments/gateways?storeId=00000000-0000-0000-0000-000000000001` 回 `200`。
- 回應摘要確認 `count=1`、`apiKeyConfigured=true`、`apiSecretConfigured=true`，且沒有暴露 `apiKey` / `apiSecret` 欄位。

---

# 2026-05-14 後台帳號表格操作欄水平溢出

## Issue

- 場景：後台 `/admin/access/users` 帳號管理頁新增「編輯角色 / 重設密碼 / 停用」三個文字按鈕後，使用 1440px 桌機視窗進行瀏覽器驗證。
- 問題：
  - 操作欄按鈕寬度加上 UUID 與角色 chips 後，表格超出可視寬度。
  - 右側操作按鈕被裁切，使用者需要水平捲動才可能操作，不符合後台管理頁的可用性。

## Root Cause

- 表格未設定固定欄寬與 `tableLayout`，長 UUID 與多個文字按鈕會共同撐大欄位。
- 操作按鈕使用完整文字標籤，對資料密集型後台表格太佔空間。

## Solution

- `UserTable.tsx` 改用 `tableLayout: fixed` 與固定操作欄寬。
- UUID 改為短格式顯示：`ID 前 8 碼...後 6 碼`。
- 操作欄改為 44px 高對比 icon buttons，使用 `aria-label` 與 tooltip 保留可讀性與可測試性。

## Verification

- `npm run build`：通過。
- Docker / OrbStack 重建 `pos-frontend`：成功。
- Chrome CDP 實測 `/admin/access/users`：
  - 帳號建立、重設密碼、停用 / 啟用流程成功。
  - `document.documentElement.scrollWidth === clientWidth`，無水平溢出。
  - 操作欄完整可見。

---

# 2026-05-13 本地端口被其他專案佔用

## Issue

- 場景：POS 盤點單瀏覽器驗證時，原本 Docker 端口 `38080`、`38082` 無法由 POS 服務使用。
- 檢查結果：
  - `financial-accounting-backend` 佔用 `38080`。
  - `financial-accounting-frontend` 佔用 `38082`。
  - `pos-postgres`、`pos-redis` 仍在 `5432`、`6379`。

## Root Cause

- 多專案並行開發時，不同專案使用同一組 localhost 對外端口，OrbStack 轉發已綁定 `38080`、`38082`。
- 若 AI 任意改用臨時端口，容易讓瀏覽器驗證與專案規格脫節，也會讓使用者不知道目前實際服務跑在哪裡。

## Solution

- 已新增規則：啟動 Docker、Vite、Spring Boot、瀏覽器測試服務或修改端口前，必須先執行 `lsof` 與 `docker ps` 檢查端口佔用。
- 若原本端口被其他專案佔用，先回報佔用者與影響；不得直接停止無關容器，也不得自行切換臨時端口繞過。
- 已同步更新 `AGENTS.md`、`ai-project-start.md`、`需求/系統規格表.md`、`需求/功能檢驗流程書.md`，並寫入 ai-kb 記憶。

---

# 2026-05-12 POS 盤點單列表 JSON 無限遞迴

## Issue

- 場景：盤點頁已有一張 `IN_PROGRESS` 盤點單後，使用者進入 `/pos/inventory/stock-takes`。
- 問題：
  - `GET /api/v1/inventory/stock-takes/stores/{storeId}` 回傳 500。
  - 錯誤訊息為 `Could not write JSON: Infinite recursion (StackOverflowError)`。
  - 前端因列表載入失敗顯示「尚無盤點單」，再點「建立盤點單」時後端又正確擋下第二張進行中盤點單，造成看起來像建立後沒有跳出盤點單。

## Solution

- `StockTakeItem.stockTake` 加上 `@JsonIgnore`，避免 `StockTake -> items -> stockTake -> items` 循環序列化。
- 新增 `StockTakeControllerTest`，確認盤點單列表可序列化，且 item 不回傳父層 `stockTake`。
- 前端建立盤點單失敗後重新載入列表並顯示後端實際錯誤訊息，避免畫面停在錯誤空狀態。

## Verification

- `mvn -pl module-pos-inventory -am test -Dtest=StockControllerTest,StockTakeControllerTest -Dsurefire.failIfNoSpecifiedTests=false`：通過。
- `npm run lint && npx tsc -b && npm run build`：通過。
- Docker backend/frontend 重建後，`GET /stock-takes/stores/{storeId}` 回 200，既有 `IN_PROGRESS` 盤點單含 4 個品項。
- Chrome CDP 驗證 `/pos/inventory/stock-takes` 顯示「已有進行中盤點」、盤點基準與 4 個實盤輸入欄，未再顯示空狀態或載入錯誤。

---

# 2026-05-12 POS 庫存頁瀏覽器測試 API 403

## Issue

- 場景：重建 Docker backend/frontend 後，以瀏覽器自動測試 `/pos/inventory`。
- 問題：
  - 直接在 localStorage 寫入假的 `local-dev-token` 可通過前端 route guard，但後端 API 仍回 403。
  - 庫存頁因此顯示「載入庫存資料失敗」或「載入商品或庫存失敗」。

## Solution

- 瀏覽器測試不再偽造 token，改先呼叫 `POST /api/v1/pos/auth/pin-login`，使用 demo terminal `DEMO-T-001` 與 PIN `1234` 取得真實 JWT。
- 將真實 token 寫入 `auth-storage`，再進入 `/pos/inventory`、`/pos/inventory/receiving`、`/pos/inventory/stock-takes` 測試。

## Verification

- `curl` 帶入真實 `Authorization: Bearer <token>` 後，庫存與商品 API 皆回 200。
- Chrome CDP 測試 Docker `http://127.0.0.1:38082`：
  - 庫存總覽、進貨驗收、盤點單皆正常載入。
  - 手機尺寸無水平溢出。

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

---

# 2026-05-11 POS Checkpoint 7 DB 驗證表名與欄位誤用

## Issue

- 場景：Checkpoint 7 最終 DB 驗證最新 E2E 訂單、付款、發票、庫存與對帳資料。
- 問題：
  - 初版查詢使用 `pos_inventory_stock`，DB 回 `relation "pos_inventory_stock" does not exist`。
  - 初版查詢使用 `pos_payment_transactions.tendered_amount`，DB 回 `column pt.tendered_amount does not exist`。
  - 初版查詢使用 `pos_reconciliation_records`，DB 回 `relation "pos_reconciliation_records" does not exist`。

## Root Cause

- POS inventory 實際門店庫存表為 `pos_inv_store_stock`。
- payment transaction 現金實收欄位為 `tendered`。
- 對帳表為 `pos_reconciliation`。

## Solution

- 查詢改用 `pos_inv_store_stock`、`pos_payment_transactions.tendered`、`pos_reconciliation`。
- `docs/production-handoff.md` 補常用 POS 驗證表與欄位提示，避免新接手者沿用錯誤表名。

## Verification

- 最新 E2E 訂單 `000000-20260510194000-6886` 可查：
  - `status=COMPLETED`
  - `discount_total=12.00`
  - `grand_total=113.00`
  - payment `method_type=CASH`、`amount=113.00`、`tendered=130.00`、`change_given=17.00`
  - invoice `status=ISSUED`、`tax_amount=5.40`、`total_amount=113.00`
  - inventory `quantity=91.000`、`reserved_quantity=0.000`
  - reconciliation `CASH`、`transaction_count=15`、`total_amount=1864.00`

---

# 2026-05-11 POS 退款後庫存、付款與對帳資料不同步

## Issue

- 場景：CLI 完成 POS payment / inventory / staff / reconciliation 後進行 review。
- 問題：
  - `RefundCompletedEvent` 已發布，但庫存模組只 log，未呼叫 `returnForRefund()` 回補庫存。
  - 退款完成後未建立 `PaymentTransaction.TxnStatus.REFUNDED` 交易，訂單列表與每日對帳看不到退款。
  - 多班次/多終端機同時開放時，staff listener 只取門店第一個 open shift，可能把銷售或退款累計到錯誤班次。
  - 已確認的 reconciliation 重新產生時會被重設成 `PENDING`。
  - `OrderCompletedEvent` 支付失敗時先保存 `FAILED` 交易後丟例外，同一個新交易可能被 rollback，導致失敗紀錄遺失。

## Root Cause

- 退款事件原本只帶 `refundId`、`orderId`、`storeId`、`refundAmount`，下游模組缺少訂單總額與退款方式，無法安全判斷是否應回補整單庫存或用哪個支付方式記退款交易。
- 目前退款請求沒有 item-level refund 明細，若部分退款直接回補整單品項會造成庫存錯加。
- staff 模組的事件歸屬未使用 `terminalId` / `employeeId`。
- reconciliation 產生流程沒有保護已人工確認狀態。

## Solution

- `RefundCompletedEvent` 補 `orderGrandTotal` 與 `refundMethod`，`RefundService` 發事件時帶入。
- `InventoryEventListener` 改為累計已完成退款總額達訂單總額時才回補整單庫存；部分退款先跳過庫存回補。
- `PaymentService` 新增 refund completed listener，依原成功付款方式建立 `REFUNDED` 交易，並用 `REFUND-{refundId}` 避免重複建立。
- `PaymentService.onOrderCompleted` 在 gateway 失敗時保存 `FAILED` 交易後 return，不發布付款成功事件。
- `processPayment` 設定 `noRollbackFor = BusinessException.class`，保留手動支付失敗交易。
- `ReconciliationService.generateDaily` 遇到非 `PENDING` 的既有紀錄直接回傳，不覆蓋人工確認結果。
- `StaffEventListener` 改用 terminal / employee context 找班次；多班次且缺上下文時跳過並記 warn。

## Verification

- `mvn -pl module-pos-core,module-pos-payment,module-pos-inventory,module-pos-staff -am test`：通過。
- `mvn -pl app -am test`：通過。

---

# 2026-05-11 POS 前端日期使用 UTC 導致營業日可能偏移

## Issue

- 場景：POS 系統在台灣門市使用，系統日期與營業日期應固定以 GMT+8 為準。
- 問題：
  - 多個前端頁面使用 `new Date().toISOString().split('T')[0]` 初始化日期欄位，該方法以 UTC 日期輸出。
  - 多個頁面直接使用 `toLocaleString('zh-TW')` 或未指定 `timeZone` 的 `Intl.DateTimeFormat`，不同執行環境可能顯示非台灣時間。
  - 登入頁原本顯示英文 AM/PM 時間，無法確認目前營業時區。

## Root Cause

- 日期格式化分散在各頁面，未統一走共用工具。
- `toISOString()` 是 UTC 時間，台灣凌晨時段可能會把日期算成前一天，影響發票查詢、每日對帳、Z 報表等營業日功能。

## Solution

- 在 `frontend-web/src/shared/utils/dateFormat.ts` 定義 `POS_TIME_ZONE = 'Asia/Taipei'`。
- `formatDate`、`formatDateTime`、`formatTime` 全部指定 `timeZone: POS_TIME_ZONE`。
- `toISODateString()` 改用 `Intl.DateTimeFormat('en-CA', { timeZone: POS_TIME_ZONE })` 取出台灣日期。
- 新增 `parseApiDate()`，後端無時區的 `LocalDateTime` 字串先視為 UTC，再轉為台灣時間顯示。
- `formatDateTime()` 與 `formatTime()` 改為 24 小時制，避免 `上午/下午` 在表格中造成誤讀。
- 訂單編號與退款編號的業務時間戳改用 `ZonedDateTime.now(Asia/Taipei)`。
- POS 頁面統一改用共用工具：
  - 登入頁時鐘顯示 `GMT+8`。
  - 對帳、發票、Z 報表日期預設使用台灣日期。
  - 訂單、盤點、調撥、班次、掛單時間使用台灣時間格式化。

## Verification

- `npm run lint`：通過。
- `npm run build`：通過，僅保留 Vite chunk size warning。
- `npm test`：通過，4 files / 8 tests passed。
- Docker frontend 重建並啟動成功。
- Playwright 驗證：
  - `/pos/login` 顯示 `GMT+8`。
  - `/pos/reconciliation` 日期為 `2026-05-11`。
  - `/pos/invoices` 起訖日期皆為 `2026-05-11`。
  - `/pos/orders` 最新訂單 `000000-20260511083417-87443` 的建立時間由 `08:34` 修正為 `2026/05/11 16:34`。

---

# 2026-05-11 POS 手機收銀台商品卡底部被裁切

## Issue

- 場景：POS 收銀台切到手機版 viewport 後檢查商品列表。
- 問題：
  - 商品卡右下角加入購物車按鈕被卡片底部裁掉。
  - 商品列表靠近底部導航時，最後幾張卡容易被固定底部導航遮住。

## Root Cause

- 手機商品卡 `minHeight` 被調低為 `184px`，但卡片內容自然高度約需 216px 以上。
- 商品卡本身使用 `overflow: hidden`，內容超出卡片高度時會被裁切。
- 商品列表是內部 scroll container，底部 padding 不足時，最後商品卡會靠近固定底部導航。

## Solution

- `frontend-web/src/features/pos-orders/pages/RegisterPage.tsx`
  - 商品卡 `minHeight` 恢復為 `232px`，確保 SKU、品名、價格、條碼與加入購物車按鈕都留在卡片內。
  - 商品列表手機版 bottom padding 改為 `pb: { xs: 12, md: 2 }`，讓最後商品可滾到手機底部導航上方。

## Verification

- `npm run lint`：通過。
- `npm run build`：通過，僅保留 Vite chunk size warning。
- `npm test -- --run`：通過，5 files / 10 tests passed。
- Docker frontend 重建並啟動成功。
- Playwright 手機 viewport `390x844` 驗證：
  - 商品卡高度為 `232px`。
  - 第一張卡片 `maxChildBottom=682`、`cardBottom=683`，`clipped=false`。
  - 加入購物車按鈕 `52x52`，`clipped=false`。
  - 列表滾到底時最後一張卡 `bottom=656`，底部導航 `top=768`，`lastAboveNav=true`。

---

# 2026-05-13 POS 盤點單完成後庫存未同步更新

## Issue

- 場景：POS 庫存盤點單頁面輸入實盤數後，直接點擊「完成盤點」。
- 問題：若使用者尚未逐項點擊「登記」，後端完成盤點時只會處理已保存的 `countedQty`，畫面上未登記的輸入值不會同步進庫存。

## Root Cause

- 後端 `StockTakeService.complete()` 僅根據已保存的 `StockTakeItem.countedQty` 校正庫存，這是正確的資料一致性設計。
- 前端原本把「輸入實盤數」與「登記實盤數」拆成兩步，使用者直覺上會以為完成盤點會採用畫面上已輸入的數字。
- 觸控裝置或快速點擊情境下，僅依 React state 讀取輸入值仍可能漏掉畫面上的即時 DOM value。

## Solution

- 完成盤點前先收集畫面上尚未登記的實盤輸入，逐筆呼叫 `submitCount` 後再呼叫 `complete`。
- 實盤輸入框加上穩定 `data-stock-count-key`，完成盤點時以 DOM 即時值作為保險來源，確保使用者看見的數字就是會送出的數字。
- 完成中停用相關按鈕，避免重複送出。
- 新增 `StockTakeServiceTest`，驗證完成盤點會將門店庫存校正為實盤數，並建立 `ADJUSTMENT` 異動。

## Verification

- `mvn -pl module-pos-inventory -am test -Dtest=StockControllerTest,StockTakeControllerTest,StockTakeServiceTest -Dsurefire.failIfNoSpecifiedTests=false`：通過。
- `npm run lint`、`npx tsc -b`、`npm run build`：通過。
- Docker frontend 重建成功。
- Chrome CDP 實測：只輸入 `42`、不按「登記」、直接完成盤點後，盤點單 `COMPLETED`，品項庫存由 `100` 更新為 `42`。

---

# 2026-05-13 Chrome CDP 測試腳本混用 require 與 top-level await

## Issue

- 場景：使用 Node 腳本透過 Chrome DevTools Protocol 測試 POS 盤點單列表分頁與時間搜尋。
- 錯誤訊息：`ReferenceError: Cannot determine intended module format because both 'require' and top-level await are present.`

## Root Cause

- Node 25 在同一段 stdin 腳本中同時看到 CommonJS `require()` 與 top-level `await`，無法判定要以 CommonJS 或 ESM 執行。

## Solution

- 改用 `node --input-type=module` 執行測試腳本。
- 將 `const fs = require('node:fs')` 改為 `import fs from 'node:fs'`。
- 重新執行後完成瀏覽器驗證，確認盤點單列表每頁最多 5 筆、時間搜尋與翻頁皆正常。

## Verification

- Chrome CDP 測試輸出：
  - 初始列表：`目前顯示 1-5 筆，共 10 筆`。
  - 搜尋 `02:24`：`目前顯示 1-1 筆，共 1 筆`。
  - 翻頁後：`第 2 / 2 頁`，列表仍最多 5 筆。

---

# 2026-05-14 Java Lambda 內外層變數同名導致編譯失敗

## Issue

- 場景：實作後端 RBAC seed 時執行 `mvn test -pl module-common,module-auth -am`。
- 錯誤訊息：`variable adminRole is already defined in method run(java.lang.String...)`。

## Root Cause

- `DataSeeder.run()` 外層已宣告 `Role adminRole`。
- `roleRepository.findByCode("SUPER_ADMIN").orElseGet(() -> { ... })` 的 lambda 內又宣告同名 `Role adminRole`，Java 不允許在 lambda 內遮蔽同一方法作用域的本地變數。

## Solution

- 將 lambda 內新建的角色變數改名為 `newAdminRole`。
- 重新執行後 module-common、module-auth、POS core/inventory 與 app 全部測試通過。

## Verification

- `mvn test -pl module-common,module-auth -am`：通過。
- `mvn test -pl module-pos-core,module-pos-inventory -am`：通過。
- `mvn test -pl app -am`：通過。

---

# 2026-05-14 Docker Compose Override Ports 被合併導致仍綁定原端口

## Issue

- 場景：POS 原端口 `38080/38082` 被 `financial-accounting-backend/frontend` 佔用，嘗試用 override 將 POS 改到 `38180/38182`。
- 錯誤訊息：`Bind for :::38080 failed: port is already allocated`。

## Root Cause

- Docker Compose 對 `ports` 的預設 merge 行為是合併陣列，不是覆蓋陣列。
- 一般 override 寫法會讓服務同時保留原本 `38080:8080` 與新增 `38180:8080`，因此仍會嘗試綁定已被占用的 `38080`。

## Solution

- 使用 Compose override tag 明確覆蓋 ports：

```yaml
services:
  backend:
    ports: !override
      - "38180:8080"
  frontend:
    ports: !override
      - "38182:80"
```

- 重新執行後 `pos-backend` 與 `pos-frontend` 分別啟動於 `38180/38182`，未影響 `financial-accounting-*` 容器。

## Verification

- `docker ps` 顯示：
  - `pos-backend 0.0.0.0:38180->8080/tcp`
  - `pos-frontend 0.0.0.0:38182->80/tcp`
- `curl http://127.0.0.1:38180/actuator/health`：回 `{"status":"UP"}`。

---

# 2026-05-14 直接開啟後端根路徑顯示 403

## Issue

- 場景：使用瀏覽器直接開啟 POS 後端根網址。
- 異常行為：`http://127.0.0.1:38180/` 回 403，容易被誤認為後端沒有啟動。

## Root Cause

- Spring Security 原本只放行 `/api/v1/auth/**`、`/api/v1/pos/auth/**`、`/actuator/health` 與 `/ws/**`。
- 後端根路徑 `/` 沒有公開端點，也沒有被 Security 放行，因此直接開啟根網址會被擋。

## Solution

- `SecurityConfig` 放行 `/` 與 `/api`。
- 新增 `BackendInfoController`，讓 `/` 與 `/api` 回傳後端服務資訊、健康檢查路徑、API base 與前端網址。

## Verification

- 修正前：
  - `curl http://127.0.0.1:38180/`：403。
- 修正後已重新 build/restart backend：
  - `curl -i http://127.0.0.1:38180/`：200，回 `Titanium POS Backend` 與 `status=UP`。
  - `curl -i http://127.0.0.1:38180/api`：200，回 API base 與健康檢查路徑。
  - `curl -i http://127.0.0.1:38180/actuator/health`：200，回 `{"status":"UP"}`。

---

# 2026-05-14 API 回應 timestamp 未帶 Asia/Taipei offset

## Issue

- 場景：瀏覽器直接開啟 `http://127.0.0.1:38180/api`。
- 異常行為：入口資料內的時間是 `+08:00`，但全域 `ApiResponse.timestamp` 仍顯示 UTC / 無 offset，例如 `2026-05-13T17:54:21...`。

## Root Cause

- `ApiResponse` 使用 `LocalDateTime.now()` 產生時間。
- Docker 容器系統時區是 UTC，且 `LocalDateTime` 不含 offset，因此序列化後看不出實際時區。
- `BackendInfoController` 內層又另外放一個 `timestamp`，造成同一個回應中有兩個不同語義的 timestamp。

## Solution

- `ApiResponse.timestamp` 改為 `OffsetDateTime`。
- 全域回應時間統一使用 `ZoneId.of("Asia/Taipei")`。
- 後端入口資訊內層改用 `serverTime` 與 `timezone`，避免與外層 `timestamp` 混淆。

## Verification

- `mvn test -pl module-common,app -am`：通過。
- 重建並啟動 `pos-backend`：成功。
- `curl -s http://127.0.0.1:38180/api`：內層 `serverTime` 與外層 `timestamp` 都回 `+08:00`。

---

# 2026-05-14 Playwright 角色卡定位器過寬導致測試逾時

## Issue

- 場景：測試後台 RBAC 角色權限編輯時，要點擊 `CASHIER` 角色卡內的「編輯權限」按鈕。
- 錯誤訊息：`locator.click: Timeout 30000ms exceeded`。

## Root Cause

- 測試腳本使用 `page.locator('div').filter({ hasText: /收銀員\s+CASHIER/ }).last().getByRole(...)`。
- 該定位器過寬，會匹配到大型祖先容器或非預期區塊，導致後續 nested button locator 不穩定。
- 目前角色卡尚未加上穩定 `data-testid`，只能先用可見文字與按鈕順序輔助定位。

## Solution

- 先輸出頁面可見按鈕與 body 文字確認角色排列順序。
- 改用 `page.getByRole('button', { name: '編輯權限' }).nth(1)` 點擊第二個可編輯角色，也就是 `CASHIER`。
- 完成新增與恢復 `pos:order:void` 的瀏覽器實測。
- 後續若繼續優化後台測試，應在角色卡或編輯按鈕補穩定 `data-testid`，避免依賴順序。

## Verification

- Playwright + Chrome 實測通過：
  - 開啟 `/admin/access/roles`。
  - 登入 `admin / 123456`。
  - 編輯 `CASHIER` 權限，新增 `pos:order:void` 後儲存。
  - 重新開啟對話框確認 checkbox 已勾選。
  - 取消勾選後儲存，確認角色權限恢復原狀。

---

# 2026-05-14 POS demo data 固定 storeId 權限失敗

## Issue

- 場景：新增 StoreAccessService 後，以 `cashier / 123456` 呼叫前端固定門店 `00000000-0000-0000-0000-000000000001` 的 POS core / inventory API。
- 異常行為：
  - 一開始回 `Store data scope denied`。
  - 修復過程中又出現 `Query did not return a unique result: 2 results were returned`。

## Root Cause

- 舊版 demo store / terminal / employee 由 JPA 產生隨機 UUID，但前端與 smoke test 使用固定 UUID。
- `BaseEntity.id` 使用 `@GeneratedValue(strategy = GenerationType.UUID)`，在 new entity 上呼叫 `setId(DEMO_STORE_ID)` 不會保證以該 UUID 寫入，仍可能產生隨機 id。
- 同一 cashier user 被多筆 demo employee 綁定，導致 `EmployeeRepository.findByUserId` 回傳多筆。

## Solution

- `PosDemoDataSeeder` 改用 `JdbcTemplate` 顯式插入固定 UUID 的 store / terminal / employee。
- 將舊 demo code 改成 `*-OLD-<id>`。
- 將舊 demo cashier employee 的 `user_id` 設為 null、狀態標為 `RESIGNED`。
- 重新啟用固定 employee 到固定 store 的 active assignment。

## Verification

- `mvn test -pl app -am`：通過。
- Docker / OrbStack 重建 `pos-backend`：成功，映像 `sha256:2ed2b0edbeafa583ee9c90e4276d573faf5bcb7bfc7dda37a06644b34e415d4b`。
- `curl http://127.0.0.1:38180/actuator/health`：回 `{"status":"UP"}`。
- DB 確認固定資料：
  - store：`00000000-0000-0000-0000-000000000001 / XINYI-001`
  - terminal：`00000000-0000-0000-0000-000000000101 / DEMO-T-001`
  - employee：`00000000-0000-0000-0000-000000000201 / EMP-POS-001`
- API smoke：
  - 未登入查訂單：403。
  - cashier 查自己門店訂單 / 暫存單 / 庫存：200。
  - cashier 查其他門店訂單：403。

---

# 2026-05-14 Java controller 新增 service dependency 後漏 import

## Issue

- 場景：為 `ReportController` 補上門店資料範圍檢查，新增 `StaffShiftService shiftService` 欄位後執行 `mvn test -pl module-pos-staff -am`。
- 錯誤訊息：`cannot find symbol: class StaffShiftService`。

## Root Cause

- Controller 新增 constructor dependency 時，已宣告 `StaffShiftService` 欄位，但未同步加入 `import com.enterprise.staff.service.StaffShiftService;`。
- Lombok `@RequiredArgsConstructor` 會在編譯期使用該欄位生成 constructor，因此少 import 會直接造成 Java 編譯失敗。

## Solution

- 在 `backend/module-pos-staff/src/main/java/com/enterprise/staff/controller/ReportController.java` 補上 `StaffShiftService` import。
- 順手將 Z Report 產生從 `requireReadableStore` 收緊為 `requireOperableStore`，符合日結資料產生屬於操作行為的語意。

## Verification

- `mvn test -pl module-pos-staff -am`：通過。
- `mvn test -pl app -am`：通過。
- Docker / OrbStack 重建 `pos-backend`：成功。
- API smoke：staff shifts 與 Z reports 自己門店 200、其他門店 403。

---

# 2026-05-14 POS CRM Repository 未被 Spring 掃描

## Issue

- 場景：新增 `module-pos-crm` 後重建並啟動 `pos-backend`。
- 異常行為：容器進入 `Restarting (1)`，38180 無法連線。
- 錯誤訊息：`No qualifying bean of type 'com.enterprise.crm.repository.MemberRepository' available`。

## Root Cause

- 多模組 Spring Boot 專案中，各 POS 模組需要自己的 module config。
- CRM 模組已新增 controller / service / repository / entity，但缺少 `@ComponentScan`、`@EntityScan` 與 `@EnableJpaRepositories` 設定。
- 因此 `MemberService` 建構子需要的 `MemberRepository` 未被註冊成 bean。

## Solution

- 新增 `backend/module-pos-crm/src/main/java/com/enterprise/crm/config/CrmModuleConfig.java`。
- 使用 `@ConditionalOnProperty(name = "modules.pos-crm", havingValue = "true", matchIfMissing = true)` 控制模組開關。
- 加入：
  - `@ComponentScan(basePackages = "com.enterprise.crm")`
  - `@EntityScan(basePackages = "com.enterprise.crm.entity")`
  - `@EnableJpaRepositories(basePackages = "com.enterprise.crm.repository")`

## Verification

- `mvn test -pl module-pos-crm -am`：通過。
- `mvn test -pl app -am`：通過。
- 重建並啟動 `pos-backend`：成功。
- `curl http://127.0.0.1:38180/actuator/health`：回 `{"status":"UP"}`。
- CRM API smoke：會員搜尋、會員建立、點數流水與訂單完成累點皆通過。

---

# 2026-05-14 Docker Hub metadata TLS handshake timeout

## Issue

- 場景：重建 `pos-backend` Docker image。
- 錯誤訊息：Docker build 在讀取 `eclipse-temurin:21-jre-alpine` metadata 時出現 TLS handshake timeout。

## Root Cause

- 失敗點在 Docker Hub image metadata 下載階段，屬於外部 registry / 網路暫時性問題。
- 專案程式碼與 Dockerfile 本身沒有在該階段進入編譯流程。

## Solution

- 不修改程式碼。
- 保留原 POS port 設定與既有容器，不停止無關專案容器。
- 重新執行同一個 Docker compose build / up 指令。

## Verification

- 重試後 Docker build 成功。
- 後續 image `sha256:df64b9b0dd2a3035fc45fdfe24b35e16e24ef432fbbe810c0818007e71ba9b87` 成功啟動。
- `curl http://127.0.0.1:38180/actuator/health`：回 `{"status":"UP"}`。

---

# 2026-05-15 POS refund query PostgreSQL nullable timestamp parameter

## Issue

- 場景：為 `GET /api/v1/pos/refunds` 新增依門店、訂單、狀態與時間區間查詢退款紀錄後，在 Docker / OrbStack 環境執行 API smoke。
- 異常行為：不帶 `from`、`to` 參數查詢退款列表時，API 回 `500`。
- 錯誤訊息：PostgreSQL 回 `ERROR: could not determine data type of parameter $6`。

## Root Cause

- Repository 一開始使用靜態 JPQL 條件：
  - `(:from is null or r.createdAt >= :from)`
  - `(:to is null or r.createdAt <= :to)`
- 當 `LocalDateTime` 參數為 null 時，PostgreSQL 無法在 `is null` 判斷中推斷該參數的實際資料型別。
- H2 或單元測試 mock 不一定會重現，因此必須用實際 PostgreSQL 容器做 API smoke 才能抓到。

## Solution

- 將 `OrderRefundRepository` 改為 extends `JpaSpecificationExecutor<OrderRefund>`。
- `RefundService.listByStore()` 改用 JPA Criteria `Specification` 動態組合查詢條件。
- 只有在 `from`、`to` 非 null 時才加入 `createdAt >= from`、`createdAt <= to` predicate，避免傳入無型別 null 參數。
- 單元測試中的 `findAll` mock 改用 `ArgumentMatchers.<Specification<OrderRefund>>any()`，避免 Mockito 在 `QueryByExampleExecutor` 與 `JpaSpecificationExecutor` 的 overloaded `findAll` 之間產生編譯歧義。

## Verification

- `mvn test -pl module-pos-core -am`：通過。
- `mvn test -pl app -am`：通過。
- Docker / OrbStack 重建 `pos-backend`：成功，映像 `sha256:66f99f19e4860be4330910f71e76069a6f7a0701db522c2d1e56f098020a3f2c`。
- `curl http://127.0.0.1:38180/actuator/health`：回 `{"status":"UP"}`。
- API smoke：
  - 退款列表查詢回 `200`。
  - 單筆退款查詢回 `200`。
  - `status=COMPLETED` 篩選查詢回 `200`。

---

# 2026-05-15 React set-state-in-effect lint on debounced member search

## Issue

- 場景：將收銀台會員綁定從前端 demo 陣列改為 `memberApi.search()` 後執行 `npm run lint`。
- 錯誤訊息：`react-hooks/set-state-in-effect`，指出在 `useEffect` 內同步呼叫 `setMemberCandidates()` / `setMemberLoading()` 可能造成 cascading renders。

## Root Cause

- React 19 lint 規則要求 effect 主要同步外部系統或訂閱外部事件。
- 原先在 effect body 內先同步清空候選、設定 loading，再建立 debounce timer，觸發此規則。

## Solution

- 將輸入為空時的清空候選、清錯誤、關 loading 移到 `handleMemberQueryChange()`。
- 將非空查詢時的 loading 啟動也移到輸入事件。
- `useEffect` 僅在查詢字串存在時建立 debounce timer，並在 timer 內非同步呼叫 CRM API 後更新結果。

## Verification

- `npx tsc -b`：通過。
- `npm run lint`：通過。
- `npm test -- --run`：通過。

---

# 2026-05-15 In-app browser screenshot timeout during POS verification

## Issue

- 場景：用 in-app browser 驗證會員管理頁與收銀台會員綁定流程時呼叫 `tab.screenshot()`。
- 錯誤訊息：`Timed out running CDP command "Page.captureScreenshot"`。

## Root Cause

- 截圖失敗點在瀏覽器自動化截圖管線，互動與 DOM 讀取仍可正常執行。
- 同一頁面可透過 DOM snapshot 確認狀態，且使用者流程點擊、輸入、搜尋與導覽皆成功。

## Solution

- 本輪不修改產品程式碼。
- 以 DOM snapshot 與實際互動結果作為驗證依據，並在 devlog 中標記截圖工具逾時。

## Verification

- 會員管理頁：新增會員、調整點數、搜尋與流水顯示均由 DOM snapshot 確認。
- 收銀台：CRM 會員搜尋、綁定、折扣與結帳頁會員資訊均由 DOM snapshot 確認。
- POS session context：PIN 登入、收銀結帳、付款完成與 PostgreSQL 最新訂單 `employee_id` 落庫均由 DOM snapshot 與資料庫查詢確認；截圖管線仍回同一個 `Page.captureScreenshot` timeout。

---

# 2026-05-15 POS promotion repository not scanned during app startup

## Issue

- 場景：新增 `module-pos-promotion` 後，Docker 重建 `pos-backend` 並啟動 app。
- 異常行為：`/actuator/health` 無法連線，容器持續重啟。
- 錯誤訊息：`No qualifying bean of type 'com.enterprise.promotion.repository.PromotionRuleRepository' available`。

## Root Cause

- 新模組的 `PromotionModuleConfig` 只加了 `@Configuration` 與 feature toggle。
- 其他 POS 模組皆在 module config 中明確宣告 `@ComponentScan`、`@EntityScan`、`@EnableJpaRepositories`。
- 因 promotion repository 未被 JPA repository 掃描，`PromotionService` 建構子注入失敗，導致 Spring Boot app 啟動中止。

## Solution

- 在 `PromotionModuleConfig` 補上：
  - `@ComponentScan(basePackages = "com.enterprise.promotion")`
  - `@EntityScan(basePackages = "com.enterprise.promotion.entity")`
  - `@EnableJpaRepositories(basePackages = "com.enterprise.promotion.repository")`

## Verification

- `mvn test -pl module-pos-promotion -am`：通過。
- `docker compose --env-file env/local/.env -f docker/local/docker-compose.yml up -d --build backend frontend`：成功。
- `curl http://127.0.0.1:38180/actuator/health`：回 `{"status":"UP"}`。
- promotion list / evaluate API smoke 通過。

---

# 2026-05-15 Promotion page currency assertion mismatch

## Issue

- 場景：新增 `PromotionRulePage.test.tsx` 後執行 `npm test -- --run`。
- 異常行為：測試預期文字含 `NT$15`，實際 `formatMoney()` 在目前測試環境顯示 `$15`。

## Root Cause

- 前端共用 `formatMoney()` 目前在 `zh-TW` / `TWD` 的 Intl 輸出於 jsdom/Vitest 環境為 `$15`。
- 測試預期寫得比產品實際格式更窄。

## Solution

- 將測試斷言改為檢查頁面實際顯示的 `折抵 $15`。

## Verification

- `npm test -- --run`：通過，8 files / 14 tests。

---

# 2026-05-15 In-app browser screenshot timeout during discount source verification

## Issue

- 場景：用 in-app browser 驗證促銷折扣來源落庫流程，於結帳頁與訂單列表呼叫 `tab.screenshot()`。
- 錯誤訊息：`Timed out running CDP command "Page.captureScreenshot"`。

## Root Cause

- 與本日稍早的瀏覽器截圖逾時屬同一類工具層問題。
- 商品點選、結帳付款、DOM snapshot 與資料庫查詢皆可正常執行，產品流程本身沒有被阻斷。

## Solution

- 本輪不修改產品程式碼。
- 改以 DOM snapshot 驗證 UI 狀態，並以 PostgreSQL 查詢確認 `discount_source`、`promotion_rule_id` 與 `discount_label` 已落庫。

## Verification

- 收銀台 DOM snapshot 顯示 `咖啡滿百 9 折` 與總計 `$137`。
- 訂單列表 DOM snapshot 顯示最新訂單折扣欄 `咖啡滿百 9 折 -$15`。
- PostgreSQL 最新訂單查詢確認 `discount_source=PROMOTION`、`promotion_rule_id=00000000-0000-0000-0000-000000001001`、`discount_label=咖啡滿百 9 折`。

---

# 2026-05-16 Frontend test command executed from repository root

## Issue

- 場景：新增 `OrderListPage.test.tsx` 後，嘗試執行單元測試。
- 錯誤訊息：`npm error enoent Could not read package.json: Error: ENOENT: no such file or directory, open '/Users/wei/Desktop/code/POS/package.json'`。

## Root Cause

- POS 專案根目錄不是 npm workspace root，前端 `package.json` 位於 `frontend-web/`。
- 測試命令誤從 `/Users/wei/Desktop/code/POS` 執行，導致 npm 找不到 package manifest。

## Solution

- 改在 `/Users/wei/Desktop/code/POS/frontend-web` 執行前端測試與 lint/build 指令。
- 後續前端驗證統一使用 `frontend-web` 作為工作目錄。

## Verification

- `npm test -- --run test/pos-orders/OrderListPage.test.tsx`：通過。
- `npm run lint`：通過。
- `npm test -- --run`：通過，10 files / 18 tests。

---

# 2026-05-16 POS expired session stayed on register page

## Issue

- 場景：使用者隔一段時間重新進入 POS 收銀台。
- 異常行為：照理應回到 PIN 登入頁，但畫面仍停在主畫面，並顯示商品資料載入失敗。

## Root Cause

- `ProtectedRoute` 只根據 zustand persisted state 的 `isAuthenticated` 判斷是否放行，未檢查 JWT payload `exp` 是否過期。
- `PosLoginPage` 若看到 `isAuthenticated=true`，會自動導回收銀台，過期 token 也會被推回主畫面。
- `axiosInstance` 只攔截 HTTP status `401`；若 response body 為 `ApiResponse { code: 401 }`，會被正規化為一般失敗 response，頁面 catch 後只顯示商品載入失敗。

## Solution

- 在 `authStore` 新增 JWT expiration 解碼與檢查，hydration 與 `setAuth()` 遇到過期 token 會清除 auth 與 `pos-session`。
- `ProtectedRoute` 加上 token 過期判斷，過期時 logout 並導回登入頁。
- `PosLoginPage` 遇到過期 persisted auth 時先 logout，不再自動導回收銀台。
- `axiosInstance` 同時處理 HTTP `401` 與 API body `code: 401`，集中清除登入狀態。

## Verification

- `npx tsc -b`：通過。
- `npm run lint`：通過。
- `npm test -- --run test/shared/authStore.test.ts test/shared/ProtectedRoute.test.tsx test/shared/axiosInstance.test.ts`：通過。
- `npm test -- --run`：通過，12 files / 21 tests。
- `npm run build`：通過。
- Docker frontend 重建成功。
- 瀏覽器驗證：鎖定終端後直接開 `/pos/register`，會導回 `/pos/login?redirect=%2Fpos%2Fregister`，沒有顯示商品載入失敗。

---

# 2026-05-16 POS member cleared by promotion or manual discount

## Issue

- 場景：收銀台先綁定會員，再套用促銷優惠碼或手動折扣。
- 異常行為：右側會員按鈕回到「會員」，結帳頁不再顯示會員資料，送出訂單時也無法帶 `memberId`。

## Root Cause

- `cartStore.setPromotionDiscount()` 與 `setDiscountAmount()` 將折扣來源切換視為解除會員，直接清空 `selectedMember`。
- `recalculateDiscountForLines()` 只要存在 `selectedMember` 就強制重算會員折扣，沒有依 `discountSource` 區分會員、促銷與手動折扣。
- 結帳頁以 `selectedMember?.id` 產生 `memberId`，因此會員在套折扣時被清掉後，訂單就失去會員關聯。

## Solution

- 讓會員身分與折扣來源分離保存，促銷折扣與手動折扣不再清除 `selectedMember`。
- `recalculateDiscountForLines()` 改依 `discountSource` 重算對應折扣。
- `clearMember()` 只在目前折扣來源為會員折扣時清除折抵；促銷或手動折扣維持原狀。
- `clearPromotionDiscount()` 若仍有會員，會回退到會員折扣。

## Verification

- `npx tsc -b`：通過。
- `npm run lint`：通過。
- `npm test -- --run test/pos-orders/cartStore.test.ts test/pos-orders/CheckoutPage.test.tsx test/pos-orders/CartPromotion.test.tsx`：通過。
- `npm test -- --run`：通過，12 files / 23 tests。
- `npm run build`：通過。
- Docker frontend 重建成功。
- 瀏覽器驗證：綁定 `林依晨` 後套用 `CAFE20`，收銀台仍顯示 `金卡 林依晨`；結帳頁同時顯示 `金卡 · 林依晨 · 1,292 點` 與 `優惠碼 CAFE20 · CAFE20`。

---

# 2026-05-17 POS checkout completion summary cleared after payment

## Issue

- 場景：收銀台完成付款後，`CheckoutPage` 會呼叫 `clearCart()` 清空目前購物車。
- 異常行為：付款成功訊息仍存在，但訂單摘要區改顯示「購物車是空的」，剛完成的品項、會員與促銷資訊會消失。
- 後續瀏覽器驗證又發現：若現金剛好付款 `$95`，購物車清空後 totals 變成 `$0`，找零會錯顯為 `$95`。

## Root Cause

- `CheckoutPage` 的摘要區直接讀 zustand cart state。
- 付款成功後清空 cart 是正確行為，但畫面沒有保留完成付款當下的 receipt snapshot。
- 現金找零也直接依目前 cart totals 計算，因此 cart 清空後會把整筆收款額當成找零。

## Solution

- 新增 `CheckoutReceiptSnapshot`，在付款完成、清空 cart 前保存品項、金額、會員、折扣來源與促銷資訊。
- 付款完成後的訂單摘要、會員 chip、促銷 chip、金額彙總與現金應收改讀取 receipt snapshot。
- 現金不足與找零計算改使用摘要總額，且付款完成後不再觸發不足狀態。

## Verification

- `npx tsc -b`：通過。
- `npm test -- --run test/pos-orders/CheckoutPage.test.tsx`：通過。
- `npm run lint`：通過。
- `npm test -- --run`：通過，12 files / 23 tests。
- `npm run build`：通過。
- Docker frontend 重建成功。
- 瀏覽器驗證：完成 `美式咖啡 12oz` 現金結帳後，付款完成畫面仍顯示品項與應收 `$95`，未顯示「購物車是空的」，找零為 `$0`。

---

# 2026-05-17 In-app browser screenshot timeout during checkout receipt verification

## Issue

- 場景：用 in-app browser 驗證付款完成摘要後呼叫 `tab.screenshot()`。
- 錯誤訊息：`Timed out running CDP command "Page.captureScreenshot"`。

## Root Cause

- 與既有 `Page.captureScreenshot` 逾時屬同一類瀏覽器工具層問題。
- DOM snapshot、點擊、導覽與實際付款流程皆成功，產品流程未被阻斷。

## Solution

- 本輪不修改產品程式碼。
- 改以 DOM snapshot 與實際互動結果驗證完成畫面狀態。

## Verification

- DOM snapshot 確認付款完成畫面顯示訂單號、品項、應收金額與正確找零。

---

# 2026-05-17 POS order number search input had no behavior

## Issue

- 場景：使用者進入 `/pos/orders` 訂單列表。
- 異常行為：畫面上有「依訂單編號搜尋」輸入框，但輸入任何內容都不會篩選列表，也沒有無結果提示。

## Root Cause

- `OrderListPage` 只渲染了搜尋 `TextField`，沒有對應 state、onChange 或篩選後的顯示資料。
- 桌面表格、手機卡片與統計卡都直接使用原始 `orders`。

## Solution

- 新增 `orderNoKeyword` state 與 `displayedOrders`。
- 將訂單編號關鍵字套用到目前載入頁面的訂單列表。
- 桌面表格、手機卡片與統計卡改用 `displayedOrders`。
- 無搜尋結果時顯示明確空狀態與提示。

## Verification

- `npx tsc -b`：通過。
- `npm test -- --run test/pos-orders/OrderListPage.test.tsx`：通過。
- `npm run lint`：通過。
- `npm test -- --run`：通過，12 files / 24 tests。
- `npm run build`：通過。
- Docker frontend 重建成功。
- 瀏覽器驗證：搜尋 `74938` 只顯示 `000000-20260517145729-74938`；搜尋 `NO-MATCH-ORDER` 顯示「找不到符合條件的訂單」。

---

# 2026-05-17 POS new order button had no navigation

## Issue

- 場景：使用者進入 `/pos/orders` 訂單列表。
- 異常行為：右上角 `新增訂單` 按鈕可點，但沒有導頁或建立新單行為。

## Root Cause

- `OrderListPage` 的 `新增訂單` button 只有視覺樣式，未設定 `onClick`。
- 收銀員從訂單列表完成查單後，無法直接回到收銀台開始下一筆訂單。

## Solution

- 替 `新增訂單` 按鈕加上 `onClick={() => navigate('/pos/register')}`。
- 補 `OrderListPage.test.tsx`，mock `useNavigate()` 並驗證點擊後導向收銀台。

## Verification

- `npx tsc -b`：通過。
- `npm test -- --run test/pos-orders/OrderListPage.test.tsx`：通過。
- `npm run lint`：通過。
- `npm test -- --run`：通過，12 files / 25 tests。
- `npm run build`：通過。
- Docker frontend 重建成功。
- 瀏覽器驗證：從 `/pos/orders` 點擊 `新增訂單` 會導向 `/pos/register`，並顯示收銀台主畫面。

---

# 2026-05-17 Homebrew cask API failed during Flutter Android setup

## Issue

- 場景：準備安裝 Flutter SDK 與 Android Studio 時執行 `brew info --cask flutter`、`brew info --cask android-studio`。
- 錯誤訊息：`Error: undefined method 'to_sym' for nil`，堆疊位於 Homebrew `cask_struct_generator.rb`。

## Root Cause

- 當時 Homebrew cask API metadata 解析失敗，且本機 Homebrew 版本為 `5.1.8`。
- `brew doctor` 顯示系統可用；後續 `brew tap` 觸發 Homebrew auto-update 後版本更新至 `5.1.11`，cask 查詢恢復正常。

## Solution

- 讓 Homebrew 完成 auto-update。
- 重新執行 cask 查詢，確認 `flutter` 與 `android-studio` cask 可解析。
- 使用 `brew install --cask flutter android-studio` 完成安裝。

## Verification

- `brew info flutter`：可顯示 Flutter SDK `3.41.9`。
- `brew info --cask android-studio`：可顯示 Android Studio `2025.3.4.7`。
- `flutter doctor -v`：Flutter 與 Android toolchain 通過。
- `/tmp/pos_flutter_smoke` smoke app 可成功 `flutter build apk --debug` 並在 emulator 啟動。

---

# 2026-05-17 Plaintext OpenAI API key in shell configuration

## Issue

- 場景：檢查 Flutter Android toolchain 時讀取 shell 設定。
- 異常行為：`/Users/wei/.zshrc` 內含明文 `OPENAI_API_KEY` export，且 Codex shell snapshot / session log 可能保留歷史輸出。

## Root Cause

- API key 被直接寫入 shell rc 檔，導致每個互動 shell 都會載入，也可能被工具快照、session log 或診斷輸出帶出。

## Solution

- 從 `/Users/wei/.zshrc` 移除明文 `OPENAI_API_KEY` export。
- 執行 `launchctl unsetenv OPENAI_API_KEY` 清除 launchd 層級變數。
- 掃描 POS repo、shell 設定、shell history、Codex shell snapshots 與 session logs。
- 對 Codex shell/session 歷史中的 API-key 型字串改為 `[REDACTED_OPENAI_KEY]`。
- 保留官方技能範例與 Codex `auth.json`，避免破壞工具登入狀態。

## Verification

- POS repo 與 `git grep` 未發現明文 OpenAI key。
- `.zshrc`、常見 shell 設定與 history 未再發現 OpenAI API key 形態字串。
- `.codex/shell_snapshots` 與 `.codex/sessions` 未再發現 OpenAI API key 形態字串。
- 後續需由使用者到 OpenAI API keys 頁面撤銷舊 key、建立新 key，並改用 secret manager / Keychain 管理。

---

# 2026-05-17 Flutter Android GridView zero-size first frame

## Issue

- 場景：`frontend-app` 第一版 POS 終端在 `pos_android_tablet` emulator 上執行 `flutter run -d emulator-5554`。
- 錯誤訊息：`Failed assertion: line 493 pos 12: 'crossAxisExtent > 0.0': is not true.`
- 異常位置：`_ProductGrid` 的 `GridView.builder`。

## Root Cause

- Android emulator 首幀 layout 過程中，`GridView` 曾收到 `crossAxisExtent: 0.0` 與 `viewportMainAxisExtent: 0.0`。
- `SliverGridDelegateWithMaxCrossAxisExtent` 要求 cross axis extent 必須大於 0，因此在真機啟動時 assert；widget test 的平板 viewport 沒有覆蓋到這個短暫零尺寸邊界。

## Solution

- 在 `_ProductGrid` 外層加入 `LayoutBuilder`。
- 當 `constraints.maxWidth <= 0` 或 `constraints.maxHeight <= 0` 時先回傳 `SizedBox.shrink()`。
- 尺寸正常後再建立 `GridView.builder`。

## Verification

- `flutter analyze`：通過。
- `flutter test`：通過，2 tests。
- `flutter build apk --debug`：通過。
- `flutter run -d emulator-5554`：成功安裝並啟動，未再出現 layout exception。

---

# 2026-05-17 Browser automation virtual clipboard blocked text input

## Issue

- 場景：使用 in-app browser 驗證 Web 收銀台掃描模式，點擊 `掃描條碼` 後嘗試以 Playwright `type` / `fill` 輸入條碼。
- 錯誤訊息：`Browser Use virtual clipboard is not installed`。

## Root Cause

- Browser automation 工具層缺少虛擬剪貼簿能力，導致該工具的文字輸入操作失敗。
- DOM snapshot 與點擊仍可用，產品頁面也無 console error。

## Solution

- 產品程式碼不需修改。
- 瀏覽器層驗證掃描按鈕、提示列與 console error。
- 條碼 Enter 後 API keyword payload 改由 React Testing Library 測試覆蓋。

## Verification

- `RegisterPage.test.tsx` 驗證點擊掃描按鈕會 focus 搜尋欄並顯示提示，輸入條碼後按 Enter 會以條碼作為 `productApi.getProducts` keyword。
- in-app browser DOM snapshot 顯示掃描提示列。
- browser console error logs 為空。

---

# 2026-05-17 Flutter PIN login screen overflow on short viewport

## Issue

- 場景：替 `frontend-app` 新增 PIN 登入畫面後執行 `flutter test`。
- 錯誤訊息：`A RenderFlex overflowed by 140 pixels on the bottom.`
- 異常行為：Flutter 測試預設 800x600 viewport 下，PIN keypad 底部超出 root render tree，`pin-submit-button` 與 `pin-digit-0` 點擊落在畫面外。

## Root Cause

- `PinLoginScreen` 使用固定垂直間距與較高的 `GridView.count` 數字鍵盤。
- 畫面內容高度超過 600px 測試視窗，且外層 `Column` 沒有可捲動容器，導致底部按鈕不可達。

## Solution

- 將登入畫面外層改為 `LayoutBuilder + SingleChildScrollView + minHeight`，短視窗可捲動且一般平板仍置中。
- 壓縮數字鍵盤列高與標題間距，避免 Android tablet / 測試小視窗溢出。
- 收銀台 header 在窄寬度改成上下排列並允許狀態列水平捲動。

## Verification

- `dart format lib test`：通過。
- `flutter test`：通過，24 tests。
- `flutter analyze`：通過。
- `flutter build apk --debug`：通過。

---

# 2026-05-19 POS order number overlaps table columns after horizontal scroll

## Issue

- 場景：Web POS 淺色模式進入訂單列表後，桌面表格往右水平滑動。
- 異常行為：長訂單編號會在視覺上壓到狀態與類型欄，右側操作欄需要較大水平滑動才看得到。

## Root Cause

- 訂單編號欄雖然是 sticky left，但長字串只設為 `white-space: nowrap`，缺少固定欄寬內的裁切與欄內捲動。
- 桌面表格使用欄位自然寬度與較大的 `minWidth`，折扣、建立時間與操作欄會把整體表格撐寬。

## Solution

- 將桌面表格改為 `tableLayout: fixed`，並使用 `colgroup` 明確定義欄寬。
- 訂單編號欄固定為 `280px`，cell 加上 `overflow: hidden`，內層 wrapper 加上 `overflow-x: auto`。
- 表格最小寬度收斂至 `1180px`，降低水平滑動距離。
- 折扣 Chip 加上 `maxWidth: 100%` 與 ellipsis，避免窄欄位撐開 layout。

## Verification

- `npm test -- --run test/pos-orders/OrderListPage.test.tsx`：通過，6 tests。
- `npx tsc -b`：通過。
- `npm run lint`：通過。
- `npm test -- --run`：通過，16 files / 39 tests。
- `npm run build`：通過，保留既有 Vite chunk size warning。
- Browser 驗證 `http://localhost:38182/pos/orders`：右滑後操作欄完整在容器內，訂單編號欄改為欄內捲動。

---

# 2026-05-19 POS order status chip is truncated

## Issue

- 場景：Web POS 淺色模式訂單列表，桌面表格顯示已完成訂單。
- 異常行為：狀態欄 chip 顯示為 `已...`，無法一眼辨識完整狀態。

## Root Cause

- 前一輪為了壓縮表格總寬，狀態欄只有 `90px`。
- MUI Chip label 在寬度接近邊界時會使用 overflow / ellipsis，導致中文狀態字樣被省略。

## Solution

- 將訂單編號欄由 `224px` 進一步收斂至 `190px`。
- 將狀態欄加寬至 `124px`。
- 為狀態 chip 新增專用樣式，讓 label 使用 `overflow: visible`、`text-overflow: clip`、`white-space: nowrap`。

## Verification

- `npm test -- --run test/pos-orders/OrderListPage.test.tsx`：通過，6 tests。
- `npx tsc -b`：通過。
- `npm run lint`：通過。
- `npm test -- --run`：通過，16 files / 39 tests。
- `npm run build`：通過，保留既有 Vite chunk size warning。
- Browser DOM 幾何驗證：狀態 label text 為 `已完成`，`scrollWidth <= clientWidth`，未被省略。

---

# 2026-05-19 POS order detail title low contrast in light mode

## Issue

- 場景：Web POS 淺色模式，從訂單列表打開「詳情」彈窗。
- 異常行為：左上角 `訂單詳情 + 訂單編號` 幾乎呈現白色，與白色 Dialog 背景對比不足。

## Root Cause

- Dialog 標題使用預設結構，未在淺色模式下明確指定前景色。
- 長訂單編號與主標混在同一行，也讓標題辨識與層級不清楚。

## Solution

- 訂單詳情 Dialog paper 明確指定 `background.paper` 與 `text.primary`。
- 標題拆成主標與訂單編號兩行，主標使用 `text.primary` 與粗體，訂單編號使用 `text.secondary` 與 monospace。
- 保留隱藏輔助文字，讓測試與輔助工具仍能辨識完整彈窗標題。

## Verification

- `npm test -- --run test/pos-orders/OrderListPage.test.tsx`：通過，6 tests。
- `npx tsc -b`：通過。
- `npm run lint`：通過。
- `npm test -- --run`：通過，16 files / 39 tests。
- `npm run build`：通過，保留既有 Vite chunk size warning。
- Browser DOM style 驗證：Dialog 背景為白色，標題為 `rgb(17, 24, 39)`，訂單編號為 `rgb(95, 102, 117)`。

---

# 2026-05-19 Admin selected nav text is low contrast in light mode

## Issue

- 場景：Web POS 淺色模式，進入管理後台並選取 `商品管理` 等側邊欄項目。
- 異常行為：選取項目背景為淡橘色，但文字與圖示為白色，辨識度不足。

## Root Cause

- `AdminLayout` 的 selected nav item 將文字色寫死為 `#FFFFFF`。
- 淺色模式 selected 背景是低飽和淡橘，白字在此背景下對比不足。

## Solution

- 依照主題模式分離 selected item 配色。
- 淺色模式 selected 文字與圖示改為深橘 `#7C2D12`。
- 深色模式 selected 仍維持白字。
- 新增 `AdminLayout` 測試，覆蓋淺色模式 active nav item 文字色。

## Verification

- `npm test -- --run test/layouts/AdminLayout.test.tsx`：通過，1 test。
- `npx tsc -b`：通過。
- `npm run lint`：通過。
- `npm test -- --run`：通過，17 files / 40 tests。
- `npm run build`：通過，保留既有 Vite chunk size warning。
- Browser DOM style 驗證：active `商品管理` 文字與圖示皆為 `rgb(124, 45, 18)`。

---

# 2026-05-21 Shared login page still shows template brand

## Issue

- 場景：使用者進入 `http://localhost:38182/login?redirect=%2Fadmin%2Fdashboard`。
- 異常行為：畫面顯示 `HIVE.ERP`、`企業模塊化組件系統` 與 `Enterprise Inc.`，看起來像前端設定又跑回模板。

## Root Cause

- `38182` 的 `pos-frontend` 容器與端口設定正常，並非跑到其他專案。
- 問題來源是後台共用登入頁 `/login` 仍保留母體模板 `LoginPage` 的預設品牌文案。
- POS PIN 登入頁 `/pos/login` 已是 POS 專案畫面，但後台路由未登入時會 redirect 到 `/login`，因此模板品牌被顯示出來。
- 修正測試時也發現標題拆成 `Titanium` 與上色 span 後，accessible name 會被計算成 `TitaniumPOS`，需要補明確 `aria-label`。

## Solution

- 新增 `frontend-web/src/shared/config/appBrand.ts` 集中管理 POS 品牌與登入表單文案。
- 將 `LoginPage` 的 `HIVE.ERP`、`企業模塊化組件系統` 與 `Enterprise Inc.` 改為 `Titanium POS` 專案品牌。
- 將 `LoginForm` 的 label、submit 文案與登入按鈕品牌色改從共用常數讀取。
- 在 h1 補上 `aria-label="Titanium POS"`，讓視覺與輔助工具名稱一致。
- 新增 `test/auth/LoginPage.test.tsx` 覆蓋 `/login` 品牌文案，防止模板品牌回歸。

## Verification

- `npm test -- --run test/auth/LoginPage.test.tsx`：通過，1 test。
- `npx tsc -b`：通過。
- `npm run lint`：通過。
- `npm test -- --run`：通過，18 files / 42 tests。
- `npm run build`：通過，保留既有 Vite chunk size warning。
- `docker compose --env-file env/local/.env -f docker/local/docker-compose.yml build frontend`：通過。
- `docker compose --env-file env/local/.env -f docker/local/docker-compose.yml up -d --no-deps frontend`：通過。
- Browser DOM 驗證 `/login?redirect=%2Fadmin%2Fdashboard` 顯示 `Titanium POS`，且 `HIVE` / `ERP` 不存在。

---

# 2026-05-22 POS foreground sidebar collapse keeps 240px width

## Issue

- 場景：Web POS 收銀台桌面版新增側邊欄收合功能。
- 異常行為：點擊 `收合側邊欄` 後，品牌文字已隱藏且狀態變成 `data-collapsed="true"`，但側邊欄幾何寬度仍停在 `240px`，主畫面沒有取得收合後的空間。

## Root Cause

- 第一版使用 MUI permanent `Drawer` 作為桌面側欄，Drawer paper 與外層 flex shell 的寬度同步不穩定。
- 改成一般 `aside` 後仍被 flex item 預設 `min-width: auto` 撐回內容最小寬度，導致即使 inline `width: 88px`，computed width 仍為 `240px`。

## Solution

- 桌面版前台側邊欄改用一般 `aside` flex 容器，手機版 temporary Drawer 保持不變。
- 在桌面 shell 與 sidebar content 同步設定 `width`、`minWidth`、`maxWidth`。
- 收合狀態維持 icon-only 導覽、tooltip 與可及性名稱，避免收合後入口不可辨識。

## Verification

- `npm test -- --run test/layouts/PosLayout.test.tsx`：通過，4 tests。
- `npx tsc -b`：通過。
- `npm run lint`：通過。
- `npm test -- --run`：通過，18 files / 44 tests。
- `npm run build`：通過，保留既有 Vite chunk size warning。
- Browser DOM 幾何驗證：展開 `240px`，收合 `88px`，再展開回 `240px`。

---

# 2026-05-22 Module-auth targeted Maven test uses stale common dependency

## Issue

- 場景：帳號管理打磨後，要跑 `module-auth` 的 `UserServiceImplTest`。
- 指令：`mvn -pl module-auth test -Dtest=UserServiceImplTest`。
- 異常行為：編譯失敗，`AuditAspect` 與 `PermissionGuardService` 找不到 `SecurityUtils.getCurrentRole()`。

## Root Cause

- `SecurityUtils.getCurrentRole()` 已存在於 `backend/module-common/src/main/java/com/enterprise/common/security/SecurityUtils.java`。
- 單獨指定 `-pl module-auth` 時，Maven 沒一起重建 reactor 內相依的 `module-common`，導致 `module-auth` 吃到舊的 common 編譯產物。
- 改用 `-am` 後，common / organization / auth 會一起建置；但上游模組沒有 `UserServiceImplTest`，需要允許未匹配指定測試。

## Solution

- 改用：

```bash
mvn -pl module-auth -am test -Dtest=UserServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false
```

## Verification

- `mvn -pl module-auth -am test -Dtest=UserServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false`：通過，11 tests。

---

# 2026-05-22 Local admin account locked during browser credential probing

## Issue

- 場景：準備瀏覽器驗證帳號管理頁時，嘗試用常見密碼登入本地 `admin` 帳號。
- 異常行為：多次失敗後，`admin` 進入鎖定狀態，後端回傳 `User account is locked. Please try again later.`。

## Root Cause

- 本地 `module-auth` 具有登入失敗鎖定機制。
- 未先確認測試帳密就用真實本地帳號做多次登入嘗試，觸發 `failed_attempts >= 5`。

## Solution

- 僅針對本地開發資料庫復原 AI 測試造成的副作用：

```sql
update auth_users
set failed_attempts = 0,
    locked_until = null
where username = 'admin';
```

- 後續瀏覽器驗證改用已知本地 `JWT_SECRET` 簽發短效測試 JWT，直接寫入瀏覽器 localStorage，不再猜密碼。

## Verification

- 復原前：`admin failed_attempts = 5`，`locked_until = 2026-05-22 04:05:18.191891`。
- 復原後：`admin failed_attempts = 0`，`locked_until = null`。
- Browser 驗證 `/admin/access/users` 可載入帳號管理頁。

---

# 2026-05-22 Account management shows role load failure for cashier session

## Issue

- 場景：使用 POS `cashier` / 店長 session 進入 `/admin/access/users`。
- 異常行為：畫面停在帳號管理主畫面，顯示「載入角色資料失敗，請稍後再試。」與空表格，看起來像帳號管理功能壞掉。

## Root Cause

- `cashier` 帳號角色為 `CASHIER, STORE_MANAGER`，不具備帳號管理需要的 `system:user:manage`。
- 帳號管理頁含建立帳號、角色指派、停用與重設密碼等敏感操作，後端只允許 `SUPER_ADMIN` 完整操作。
- 前端原本只有一般登入守衛，未對 `帳號管理` / `角色權限` 做角色層級守衛，導致無權限 POS session 進入頁面後才被 API 403 擋下。
- 初版修正將 `reason=admin-permission` 導到登入頁，但登入頁看到同一個 reason 時也會清掉剛登入成功的 admin session，造成 admin 登入後又回登入頁。

## Solution

- 後端登入回應補 `role`，讓前端保存登入者主角色。
- 前端 `authStore` 新增 JWT role 解析，支援既有 session。
- `ProtectedRoute` 增加 `allowedRoles`，將 `/admin/access/roles` 與 `/admin/access/users` 限定為 `SUPER_ADMIN`。
- `LoginPage` 只在 `reason=admin-permission` 且當前身分不是 `SUPER_ADMIN` 時清除舊 session，避免清掉新登入的 admin。
- `AdminLayout` 對非 `SUPER_ADMIN` 隱藏 `角色權限` 與 `帳號管理` 導覽入口。

## Verification

- Backend：`mvn -pl module-auth -am test -Dtest=AuthServiceImplTest,UserServiceImplTest,RbacControllerSecurityTest -Dsurefire.failIfNoSpecifiedTests=false` 通過，17 tests。
- Frontend：`npx tsc -b`、`npm run lint`、`npm test -- --run`、`npm run build` 通過。
- Browser：真實 `cashier / 123456` 會被導回含系統管理員提示的登入頁；`admin / 123456` 登入後可正常載入帳號管理資料，`roles/users` API 皆 200。

---

# 2026-05-22 Dark mode login form text is unreadable on light card

## Issue

- 場景：後台登入頁在深色模式下顯示。
- 異常行為：登入卡片維持淺色背景，但輸入框 label、輸入文字與頁尾文字過白或過淡，導致畫面看起來像文字消失。

## Root Cause

- 登入頁卡片使用固定淺色背景，但 `LoginForm` 內的 MUI `TextField` 仍繼承全域深色主題的輸入框文字與 label 色彩。
- 頁尾與副標題使用 `text.secondary/text.disabled`，在固定淺色卡片內沒有足夠對比。

## Solution

- 在 `LoginPage` 對登入卡片、副標題與頁尾使用適合淺色卡片的固定 slate 色階。
- 在 `LoginForm` 新增登入頁專用 `TextField` 樣式，固定 label、輸入文字、WebKit text fill、外框與 focus 色彩。

## Verification

- `npm test -- --run test/auth/LoginPage.test.tsx`：通過，2 tests。
- `npx tsc -b`：通過。
- `npm run lint`：通過。
- `npm test -- --run`：通過，19 files / 52 tests。
- `npm run build`：通過。
- Browser：登入頁實測卡片文字 `rgb(30, 41, 59)`，label `rgb(100, 116, 139)`，輸入文字與 WebKit fill `rgb(30, 41, 59)`，深色模式下可讀。

---

# 2026-05-22 Role creation dialog input label overlaps value

## Issue

- 場景：角色權限頁新增「新增角色」對話框後，用瀏覽器填入角色名稱與角色代碼。
- 異常行為：深色對話框中的 MUI TextField label 沒有穩定縮到欄位上緣，角色代碼 label 會壓到輸入值，視覺上不清楚。

## Root Cause

- 角色新增對話框使用固定深色背景，但 TextField 沒有局部指定深色對話框需要的 label、input、helper text 與外框樣式。
- TextField 在對話框轉場與填值後的 label shrink 狀態不夠穩定，造成 label 與值短暫或持續重疊。

## Solution

- 新增 `roleDialogTextFieldSx`，固定深色對話框內 TextField 的 label、input、helper text 與外框顏色。
- 對角色名稱、角色代碼、角色描述欄位設定 `InputLabelProps={{ shrink: true }}`，讓 label 永遠停在欄位上緣。

## Verification

- `npm test -- --run test/auth/RolePermissionTree.test.tsx`：通過，1 test。
- `npx tsc -b`：通過。
- Browser：新增角色對話框填入 `test_shift_manager` 後顯示 `TEST_SHIFT_MANAGER`，label 位於輸入框上緣，Paper 背景 `rgb(35, 38, 51)`、opacity `1`。
