# AGENTS.md

本文件是 AI Agent 進入 POS 專案時的第一入口。全局規則仍以 `~/ai-kb/AGENTS.md`、`~/ai-kb/memory/MEMORY.md` 與 `/Users/wei/.claude/CLAUDE.md` 為準；本文件補充 POS 專案自己的啟動、Claude 規劃與 Codex 執行流程。

## 語言

- 對使用者溝通、任務清單、進度回報與最終回覆一律使用繁體中文。
- 程式註釋與文件語言依本專案規則與既有檔案風格執行。

## 必讀順序

首次接觸本專案時，請依序閱讀：

1. `~/ai-kb/AGENTS.md`
2. `~/ai-kb/memory/MEMORY.md`
3. `ai-project-start.md`
4. `README.md`
5. `docs/project-map.md`
6. `需求/README.md`
7. `需求/AI協作交接規範.md`
8. `CLAUDE.md`（若由 Claude 接手）
9. `.agent/workflows/rules.md` 與 `.cursor/rules/projectrule.mdc`（需要本地工作流或 Cursor 規則時）
10. `reference/模塊化組件/ai-handoff.md`（若不存在，先執行初始化腳本）

POS 專案根目錄固定為 `/Users/wei/Desktop/code/POS/`。舊模板資料夾 的有效內容已整理併入 POS 根目錄、`.agent/`、`.cursor/`、`需求/` 與 `devlog/`；後續不得再把模板資料夾當作根目錄或需求來源。

## 分支與 Git

- 本專案開發分支為 `feature`。
- 開發、提交、推送前必須確認不是 `main` / `master`。
- 開始工作前執行：

```bash
git status --short --branch
git pull origin feature
```

## 模塊化組件 Reference

初始化時必須確認：

```text
reference/模塊化組件/ai-handoff.md
```

若不存在，請執行：

```bash
bash scripts/setup-module-reference.sh
```

注意：

- `reference/模塊化組件/` 只作為 AI 規劃參考與匯出工具來源。
- 正式導入後的 portable bundle 會放在 `module/`。
- 不可直接修改 `reference/模塊化組件/` 並當作 POS 正式源碼。
- 通用模塊 bug 或可重用能力，必須依 `reference/模塊化組件/ai-handoff.md` 回寫真正母體 `/Users/wei/Desktop/code/模塊化組件/`。

## Claude → Codex 分工

- Claude 階段：補齊 `需求/需求文檔.md`、`需求/系統規格表.md`、`需求/開發流程規劃.md`、`需求/功能檢驗流程書.md`。
- Claude 應描述「做什麼、為什麼、驗收標準、風險與待確認問題」。
- Claude 不應指定過細的檔案修改、函式名稱、CSS class、migration 版本號或逐行施工方式。
- Codex 階段：根據 Claude 文件自行設計實作方案、導入模塊、編碼、測試、提交並更新 devlog。
- Codex 若發現規劃與實際架構衝突，應先說明調整方案與風險，再執行。

## 開發規則

- 開發前先確認需求文檔、系統規格表、開發流程規劃與功能檢驗流程書。
- 若 Claude 規劃文件缺漏，先補齊或列出缺口，不要直接開始開發。
- 不確定需求時先向使用者確認。
- 完成功能後必須測試並更新 devlog。
- 遇到問題必須記錄在 `devlog/troubleshooting.md`；若解決通用問題，依知識庫規則寫入 Obsidian raw errors。

## 端口與服務啟動規則

- POS 本地服務必須優先使用 `需求/系統規格表.md` 定義的原本端口：Backend `38080`、Frontend `38082`、PostgreSQL `5432`、Redis `6379`。
- 每次啟動 Docker、Vite、Spring Boot、Playwright/瀏覽器測試服務，或修改任何端口設定前，必須先檢查目標端口是否已被佔用：

```bash
lsof -nP -iTCP:38080 -sTCP:LISTEN
lsof -nP -iTCP:38082 -sTCP:LISTEN
docker ps --format 'table {{.Names}}\t{{.Ports}}\t{{.Status}}'
```

- 若原本端口被其他專案或未知服務佔用，不得任意改用臨時端口繞過，也不得直接停止無關容器；必須先回報佔用者、影響範圍與建議處理方式。
- 只有在使用者明確同意臨時替代端口時，才可使用替代端口；替代端口必須記錄在當日 devlog，任務完成後清理臨時服務。
