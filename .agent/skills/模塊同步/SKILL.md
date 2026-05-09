---
name: 模塊同步
description: 在專案與母體模塊化組件之間執行同步。包含：修復 bug 後回寫母體、新功能抽離至母體、從母體拉取更新三個方向。
---

# 模塊同步 Skill

## 概述

本 Skill 處理「企業模塊化組件系統」專案與母體倉庫之間的雙向同步流程。

**正式母體倉庫位置**：`~/Desktop/code/模塊化組件/`（Git remote: `billwei1107/module`）

**專案內參考位置**：`reference/模塊化組件/`。此資料夾只作為 AI 規劃參考與匯出工具來源，不可直接當作正式源碼修改。

---

## 通用模組 vs 專案專屬模組

執行任何同步前，必須先判斷目標是否屬於通用模組：

| ✓ 通用模組（需同步） | ✗ 專案專屬（不同步） |
|-------------------|--------------------|
| `module-common`, `module-auth`, `module-organization`, `module-workflow`, `module-notification`, `module-attendance` | 客戶專屬 seeder、品牌文案、一次性整合設定 |
| 可被多專案複用的 `module-pos-*` 業態模板模組 | 只服務單一 POS 客戶流程的客製化分支 |
| 前端 `shared/api`, `shared/store`, `shared/types`, 通用元件與 hook | 前端品牌主題、客戶專屬頁面文案 |

---

## 第一步：確認同步方向

詢問用戶：

1. **修復回寫**：在專案中修復了通用模組的 bug，需要寫回母體
2. **新功能抽離**：開發了可複用的新功能，需要加入母體
3. **從母體拉取**：母體有更新，需要同步到當前專案

---

## 方向 A：修復回寫（專案 → 母體）

**使用時機**：修復了通用模組的 bug，且已在專案中驗證無誤。

### 執行步驟

1. **確認修復範圍**：列出修改的檔案，確認都屬於通用模組（非專案專屬）
2. **執行 rsync 回寫**：

   後端模組（擇一）：
   ```bash
   rsync -av --delete \
     ~/Desktop/code/[專案]/backend/[module-name]/ \
     ~/Desktop/code/模塊化組件/module/backend/[module-name]/
   ```

   前端 feature：
   ```bash
   rsync -av --delete \
     ~/Desktop/code/[專案]/frontend-web/src/features/[feature-name]/ \
     ~/Desktop/code/模塊化組件/module/frontend-web/src/features/[feature-name]/
   ```

   前端 shared：
   ```bash
   rsync -av --delete \
     ~/Desktop/code/[專案]/frontend-web/src/shared/[dir]/ \
     ~/Desktop/code/模塊化組件/module/frontend-web/src/shared/[dir]/
   ```

3. **在母體 commit**：
   ```bash
   cd ~/Desktop/code/模塊化組件
   git add .
   git commit -m "fix(module-[name]): [說明修復內容]"
   git push origin feature/module-leave
   ```

4. **回報完成**：告知用戶已回寫並 push 到 `billwei1107/module`

---

## 方向 B：新功能抽離（專案 → 母體）

**使用時機**：在專案中開發了通用邏輯，評估可供其他客戶專案複用。

### 判斷標準

- ✓ 適合抽離：通用 CRUD 邏輯、業務無關的工具函數、任何企業系統都可能用到的功能
- ✗ 不適合抽離：硬編碼了業態相關邏輯、包含特定客戶資料、與 POS 收銀流程耦合

### 執行步驟

1. **清除硬編碼**：移除所有專案特定的路由名稱、常數、客戶資訊
2. **執行 rsync 複製**（同方向 A 的指令）
3. **確認母體可獨立運作**：確認複製過去的代碼不依賴專案專屬的檔案
4. **在母體 commit**：
   ```bash
   git commit -m "feat(module-[name]): [說明新增功能]"
   git push origin feature/module-leave
   ```
5. **更新母體文檔**（若需要）：更新 `企畫書.md` 或相關說明

---

## 方向 C：從母體拉取更新（母體 → 專案）

**使用時機**：母體有新功能或修復，需要同步到當前專案。

### 執行步驟

1. **確認要拉取的模組**，避免覆蓋專案已有的客製化修改
2. **執行 rsync 拉取**：

   ```bash
   rsync -av --delete \
     ~/Desktop/code/模塊化組件/module/backend/[module-name]/ \
     ~/Desktop/code/[專案]/backend/[module-name]/
   ```

3. **執行 TypeScript 編譯驗證**（前端）：
   ```bash
   cd [專案]/frontend-web && node_modules/.bin/tsc -b
   ```

4. **執行 build 驗證**：
   ```bash
   npm run build
   ```

5. **確認無誤後 commit 到專案**

---

## 注意事項

> [!CAUTION]
> - **禁止**把 `reference/模塊化組件/` 當作正式母體直接修改
> - **禁止**同步客戶專屬資料、品牌文案、一次性流程到母體
> - `module-pos-*` 若是可重用的 POS 業態模板，應回寫母體；若是單一客戶客製化，留在 POS 專案
> - 回寫前**必須**確認修復已在專案中驗證無誤
> - 新功能抽離前**必須**清除所有硬編碼
