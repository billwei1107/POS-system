# 需求資料結構說明

本目錄是 Claude 規劃與 Codex 執行之間的正式交接區。所有規劃文件必須集中於此，不要散落在專案根目錄或其他資料夾。

進入需求區前，請先閱讀：

```text
AGENTS.md
ai-project-start.md
docs/project-map.md
```

## 文件地圖

```text
需求/
├── README.md              # 本文件，說明需求區資料結構
├── AI協作交接規範.md       # Claude 規劃與 Codex 執行的分工規範
├── 需求文檔.md             # Claude 補全：需求、角色、功能範圍、業務規則
├── 系統規格表.md           # Claude 補全：技術棧、架構、環境、資料庫、部署規格
├── 技術規格.md             # POS 詳細工程規格：資料表、Feature Toggle、API、離線與安全策略
├── 開發流程規劃.md         # Claude 補全：功能優先級、模塊選型、階段與依賴
├── 功能檢驗流程書.md       # Claude 補全：驗收標準、測試案例、邊界情境
├── UIUX-設計提示詞.md      # POS UI/UX 設計參考與外部 AI 生成提示詞
├── 流程圖/                 # Claude 可產出：功能流程、業務流程、狀態流程
└── 頁面結構/               # Claude 可產出：頁面結構圖與導航層級
```

## 基本原則

- Claude 階段負責補齊需求、規格、規劃與檢驗標準。
- Claude 不應指定過細的程式檔案、函式名稱、CSS class、migration 版本號或逐行施工方式。
- Codex 階段負責根據上述文件自行設計實作方案、導入模塊、編碼、測試、提交與回寫 devlog。
- 如果 Codex 發現 Claude 規劃與現有架構、模塊限制或驗證結果衝突，應先在 devlog 記錄並向使用者說明調整方案。
- 舊模板資料夾 的有效內容已併入本目錄與專案根目錄；後續 AI 不應再把模板資料夾視為專案根目錄或需求來源。

## 文件責任

| 文件 | 責任 | 更新時機 |
|------|------|----------|
| `需求文檔.md` | 描述產品需求、使用者角色、功能範圍與業務規則 | 需求新增、刪改、驗收口徑變更 |
| `系統規格表.md` | 描述技術棧、部署、模塊策略、資料安全與非功能規格 | 技術選型、部署、環境或模塊策略變更 |
| `技術規格.md` | 描述資料表、Feature Toggle、API、離線與安全細節 | 後端資料模型、API contract、migration 規劃變更 |
| `開發流程規劃.md` | 描述階段、優先級、依賴、風險與 DoD | Sprint 規劃或開發順序調整 |
| `功能檢驗流程書.md` | 描述驗收標準、測試案例與回歸流程 | 功能完成、測試策略或驗收條件變更 |
| `UIUX-設計提示詞.md` | 提供 UI/UX 外部設計參考 | POS 終端或管理後台視覺規劃更新 |

## 目前已知系統範圍

- 後端已存在 `module-common`、`module-auth`、`module-organization`、`module-workflow`、`module-notification`、`module-attendance`、`module-leave` 與 POS 專屬 `module-pos-*` 核心模組。
- 前端已存在 auth、organization、workflow、notification、attendance、leave 與 POS 商品、訂單、支付、稅務、庫存、員工等 feature。
- Flutter POS 終端尚未建立，仍屬後續階段。
- `reference/模塊化組件/` 僅作為參考；正式回寫母體請使用 `/Users/wei/Desktop/code/模塊化組件/`。
