# [專案名稱]

> [簡短描述專案用途]

## 技術棧

| 層級 | 技術 |
|------|------|
| 後端 | [例：Spring Boot 3.x / Java 21] |
| 前端 (Web) | [例：React + TypeScript / Vite] |
| 前端 (App) | [例：Flutter] |
| 資料庫 | [例：PostgreSQL 16] |
| 快取 | [例：Redis 7] |
| 部署 | [例：Docker / Docker Compose] |

## 快速開始

### 環境需求
- [例：Node.js >= 20]
- [例：Java 21]
- [例：Docker & Docker Compose]

### 安裝步驟

```bash
# 1. 複製環境變數
cp .env.example .env

# 2. 安裝依賴
[npm install / flutter pub get / gradle build]

# 3. 啟動開發環境
[npm run dev / docker-compose up]
```

## 專案結構

```
[專案名稱]/
├── .agent/workflows/     # AI 助手工作流程
├── .cursor/rules/        # Cursor 開發規範
├── devlog/               # 開發日誌
├── 需求/                  # 需求文檔與流程圖
├── .env.example          # 環境變數範本
├── .gitignore            # Git 忽略規則
└── README.md             # 本文件
```

## 開發規範

- 詳見 `.cursor/rules/mainrule.mdc`（全域規範）
- 詳見 `.cursor/rules/projectrule.mdc`（專案規範）
- 詳見 `.agent/workflows/rules.md`（AI 工作流規範）

## 開發日誌

- 主日誌：`devlog/devlog.md`
- 每日日誌：`devlog/YYYY-MM-DD-devlog.md`
- 問題追蹤：`devlog/troubleshooting.md`
