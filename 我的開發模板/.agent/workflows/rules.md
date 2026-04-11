---
description: 專案核心開發規範 (包含全域 mainrule 與專案 projectrule)
---

// turbo-all

> [!IMPORTANT]
> 本文件為專案特定規範的快速入口。全域通用規範請參見 `.cursor/rules/mainrule.mdc`，本文件僅定義**當前專案獨有**的設定。

## 一、 語言與通訊規範 (Language & Communication)

<!-- TODO: 依據專案需求填寫，以下為範例 -->

1.  **溝通語言**：[繁體中文 / 簡體中文 / English]
2.  **代碼註釋**：
    - 以模塊、函式為單位撰寫註釋。
    - 必須包含 **[繁體中文 - English]** 雙語對照。
3.  **Git 提交訊息**：採取 `type(scope): subject` 格式。

## 二、 技術與架構規範 (Technical Specs)

<!-- TODO: 依據專案需求填寫 -->

1.  **技術棧**：
    - 後端：[例：Spring Boot 3.x (Java 21), PostgreSQL, Redis]
    - 前端 (Web)：[例：React + TypeScript, Material-UI, Vite]
    - 前端 (App)：[例：Flutter]
2.  **RESTful API**：資源導向、統一響應格式、版本控制 (v1)。

## 三、 部署配置 (Deployment)

<!-- TODO: 依據專案需求填寫 -->

1.  **部署方式**：[Docker / 雲端 / 其他]
2.  **測試伺服器**：[IP / URL]
3.  **是否使用開發網關**：[是 / 否]

---
*全域規範請參見 `.cursor/rules/mainrule.mdc`，此文件僅為專案特定設定。*
