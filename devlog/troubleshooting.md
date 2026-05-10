# 問題追蹤日誌 (Troubleshooting Log)

> 本文件專門記錄開發過程中遇到的問題與解決方案。
> 遇到新問題時，請先搜尋本文件是否有類似問題的紀錄。

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
