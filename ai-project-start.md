# AI 專案啟動入口 / AI Project Start

本文件是 POS 專案的 AI 啟動入口。使用者目前的工作流是：先由 Claude 補齊需求與規劃，再由 Codex 依規劃實作、測試與提交。

## 1. 初始化必做

請先確認模塊化組件 reference 是否存在：

```text
reference/模塊化組件/ai-handoff.md
```

若不存在，請執行：

```bash
bash scripts/setup-module-reference.sh
```

完成後，請先閱讀：

```text
reference/模塊化組件/ai-handoff.md
```

再依照該文件規劃是否需要導入或回寫模塊。

## 2. Reference 定位

```text
reference/模塊化組件/
```

只作為 AI 規劃參考與匯出工具來源，不是 POS 正式源碼。正式導入後的 portable bundle 會放在：

```text
module/
```

## 3. 標準啟動順序

1. 閱讀 `AGENTS.md`
2. 閱讀 `README.md`
3. 閱讀 `docs/project-map.md`
4. 閱讀 `需求/README.md`
5. 閱讀 `需求/AI協作交接規範.md`
6. 檢查原本本地端口是否被佔用：Backend `38080`、Frontend `38082`、PostgreSQL `5432`、Redis `6379`
7. 執行或確認 `scripts/setup-module-reference.sh`
8. 閱讀 `reference/模塊化組件/ai-handoff.md`
9. 依目前角色執行：
   - Claude：補齊需求、系統規格、開發流程規劃、功能檢驗流程書
   - Codex：檢查 Claude 文件後實作、測試、提交

## 4. Claude 規劃階段產物

Claude 需要補齊：

```text
需求/需求文檔.md
需求/系統規格表.md
需求/開發流程規劃.md
需求/功能檢驗流程書.md
```

Claude 應回答「做什麼、順序、驗收」，不要規定 Codex 每個檔案怎麼寫。

## 5. Codex 執行階段檢查

Codex 接手前應確認：

- `需求/需求文檔.md` 已描述功能範圍與業務規則
- `需求/系統規格表.md` 已描述技術棧、架構、環境與模塊策略
- `需求/開發流程規劃.md` 已描述優先級、依賴、階段與風險
- `需求/功能檢驗流程書.md` 已描述驗收標準與測試案例
- `reference/模塊化組件/ai-handoff.md` 已存在
- `docs/project-map.md` 已反映目前實際資料夾結構

## 6. 給 AI 的最小提示詞

```text
請先閱讀 AGENTS.md、ai-project-start.md 與 docs/project-map.md。
如果 reference/模塊化組件/ai-handoff.md 不存在，請執行 bash scripts/setup-module-reference.sh 自動從遠端抓取模塊化組件母體。
啟動 Docker、前端、後端或瀏覽器測試前，請先檢查 POS 原本端口 38080、38082、5432、6379 是否被佔用；若被其他專案佔用，先回報，不要自行改臨時端口繞過。
若你是 Claude，請先補齊 需求/需求文檔.md、需求/系統規格表.md、需求/開發流程規劃.md、需求/功能檢驗流程書.md。
若你是 Codex，請先檢查 Claude 產物是否完整，再自行設計實作方案並執行。
```
