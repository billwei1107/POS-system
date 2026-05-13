# Reference 資料夾

此資料夾用於存放 AI 規劃與開發時需要閱讀的外部參考資料。

## 模塊化組件

初始化或接手 POS 專案時，AI 應執行：

```bash
bash scripts/setup-module-reference.sh
```

腳本會從遠端抓取模塊化組件母體到：

```text
reference/模塊化組件/
```

抓取完成後，AI 必須先閱讀：

```text
reference/模塊化組件/ai-handoff.md
```

注意：`reference/模塊化組件/` 只作為參考與匯出工具來源，不是 POS 正式源碼。
