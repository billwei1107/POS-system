---
name: 寫devlog
description: 建立或更新當日開發日誌，包含開發規劃、專案結構、進度記錄，以及 git commit 提交記錄的標準格式寫入。
---

# 寫 Devlog Skill

## 概述

本 Skill 定義開發日誌的建立與更新流程。每次開發任務結束、或有 git commit/push 後，應執行本 Skill 確保日誌記錄完整。

**日誌存放位置**：專案根目錄下的 `devlog/` 資料夾

---

## 第一步：確認當前日期與日誌檔案

```bash
date
```

確認今日日期（YYYY-MM-DD），找到或建立對應的日誌檔案：`devlog/YYYY-MM-DD-devlog.md`

---

## 第二步：判斷操作類型

1. **建立新日誌**：今日尚無日誌檔案
2. **更新日誌**：補充進度或 git 提交記錄
3. **記錄問題**：遇到 bug 或卡關，寫入 `devlog/troubleshooting.md`

---

## 方向 A：建立新日誌

使用以下模板建立 `devlog/YYYY-MM-DD-devlog.md`：

```markdown
# YYYY-MM-DD 開發日誌

## 開發規劃 (Planning)

### 今日目標
- [ ] [任務 1]
- [ ] [任務 2]

### 待辦事項
- [ ] [待辦 1]

---

## 專案結構 (Project Structure)

```
[專案根目錄]/
├── [目錄 1]/    # [說明]
├── [目錄 2]/    # [說明]
└── ...
```

### 關鍵模組說明
| 模組/目錄 | 說明 |
|---------|------|
| [模組名] | [功能描述] |

---

## 進度 (Progress)

### 已完成
- [x] [完成項目 1]

### 進行中
- [ ] [進行中項目]

---

## Git 提交記錄

（每次 commit/push 後補充至此）
```

---

## 方向 B：補充 Git 提交記錄

每次 `git commit` 或 `git push` 後，在當日 devlog 的「## Git 提交記錄」區塊補充：

```markdown
- **提交時間**: HH:MM
- **提交 Hash**: `xxxxxxx`
- **提交訊息**: `type(scope): description`
  - 變更摘要 1
  - 變更摘要 2
- **變更統計**: X 個檔案 (+N/-M)
- **推送**: `origin/[branch]` ✓（若有 push）
```

取得提交 Hash 的指令：
```bash
git log --oneline -3
```

---

## 方向 C：記錄問題至 troubleshooting.md

當遇到 Error、Bug 或開發卡關時，寫入 `devlog/troubleshooting.md`：

```markdown
---

## [YYYY-MM-DD] [問題標題]

### 問題描述
- **錯誤訊息**：[貼上 Error 或行為描述]
- **發生情境**：[做什麼操作時出現]

### 解決方案
（解決後補充）
- **原因**：[根本原因分析]
- **修復方式**：[具體修改了什麼]
```

> 解決問題後，若屬於通用技術問題（非專案特定），還需同步寫入知識庫：
> `~/Desktop/obsidian/raw/coding/errors/YYYY-MM-DD-[問題簡述].md`

---

## 注意事項

> [!CAUTION]
> - 建立日誌前**必須**執行 `date` 確認系統時間，不得憑記憶填寫日期
> - **禁止**跳過「專案結構」區塊，每次建立新日誌都必須更新專案目錄樹
> - Git 提交記錄**必須**在 commit 後即時補充，不得事後補記
