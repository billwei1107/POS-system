---
name: 頁面結構圖
description: 使用 Mermaid Flowchart 語法產出頁面結構樹狀圖，含標準配色（Level 1-4）、節點格式與完整範例。
---

# 頁面結構圖 Skill

## 概述

本 Skill 定義使用 Mermaid Flowchart 語法繪製前台/後台頁面結構圖的標準流程與格式規範。

**文件存放位置**：`需求/頁面結構/` 目錄，如 `需求/頁面結構/前台頁面結構.md`

---

## 第一步：收集資訊

詢問用戶：
1. 要繪製的是**前台**還是**後台**（或兩者）？
2. 主要的一級頁面有哪些？
3. 各頁面下有哪些子功能或子頁面？

---

## 第二步：套用標準格式

### 圖表規範

| 規範項目 | 設定 |
|---------|------|
| 語法 | Mermaid `graph LR`（左到右） |
| 節點層級標記 | `:::levelN`（N = 1~4） |
| 存放位置 | `需求/頁面結構/[名稱].md` |

### 層級配色

| 層級 | 說明 | 顏色 |
|------|------|------|
| Level 1 | 主節點（系統/平台名稱） | 橙橘 `#f97316` |
| Level 2 | 主功能區塊 | 青綠 `#0d9488` |
| Level 3 | 子頁面/功能 | 紫色 `#8b5cf6` |
| Level 4 | 詳細功能/操作 | 粉色 `#ec4899` |

---

## 第三步：套用模板

```markdown
> 本圖使用 Mermaid 語法繪製，請使用支援 Mermaid 預覽的編輯器（如 VS Code + Mermaid 插件）開啟。

graph LR
    %% ==================== Level 1：主節點 ====================
    Main[系統名稱]:::level1

    %% ==================== Level 2：主功能區塊 ====================
    Main --> A[功能區塊 A]:::level2
    Main --> B[功能區塊 B]:::level2

    %% ==================== Level 3：子頁面 ====================
    A --> A1[子頁面 A-1]:::level3
    A --> A2[子頁面 A-2]:::level3
    B --> B1[子頁面 B-1]:::level3

    %% ==================== Level 4：詳細功能 ====================
    A1 --> A1a[操作 A-1-a]:::level4
    A1 --> A1b[操作 A-1-b]:::level4

    %% ==================== 樣式定義 ====================
    classDef level1 fill:#f97316,stroke:none,color:white;
    classDef level2 fill:#0d9488,stroke:none,color:white;
    classDef level3 fill:#8b5cf6,stroke:none,color:white;
    classDef level4 fill:#ec4899,stroke:none,color:white;
```

---

## 完整範例：POS 後台頁面結構

```markdown
> 本圖使用 Mermaid 語法繪製，請使用支援 Mermaid 預覽的編輯器開啟。

graph LR
    Main[POS 後台管理系統]:::level1

    Main --> Auth[認證管理]:::level2
    Main --> Org[組織架構]:::level2
    Main --> POS[收銀管理]:::level2
    Main --> Inv[庫存管理]:::level2

    Auth --> AuthLogin[登入頁]:::level3
    Auth --> AuthUsers[用戶列表]:::level3
    Auth --> AuthRoles[角色權限]:::level3

    Org --> OrgCompany[公司設定]:::level3
    Org --> OrgDept[部門管理]:::level3
    Org --> OrgEmployee[員工管理]:::level3

    OrgEmployee --> OrgEmpList[員工列表]:::level4
    OrgEmployee --> OrgEmpForm[新增/編輯員工]:::level4

    POS --> POSRegister[收銀台]:::level3
    POS --> POSOrders[訂單記錄]:::level3
    POS --> POSCheckout[結帳頁面]:::level3

    Inv --> InvList[庫存列表]:::level3
    Inv --> InvAlert[低庫存警示]:::level3

    classDef level1 fill:#f97316,stroke:none,color:white;
    classDef level2 fill:#0d9488,stroke:none,color:white;
    classDef level3 fill:#8b5cf6,stroke:none,color:white;
    classDef level4 fill:#ec4899,stroke:none,color:white;
```

---

## 注意事項

> [!CAUTION]
> - Mermaid 的 `classDef` **不支援** `font-family` 和 `font-weight`，禁止加入字型屬性
> - 節點 ID 使用英文（如 `Auth`, `OrgEmployee`），節點顯示文字可用中文
> - 圖表方向固定為 `graph LR`（左到右），不得改為 `TD`（上到下）
> - 每個節點**必須**添加對應層級的 `:::levelN` 標記
> - 文件開頭**必須**加上 Mermaid 預覽提示說明
