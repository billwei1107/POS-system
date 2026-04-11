---
description: (僅適用於 Flutter 專案) iOS 應用程式打包與 TestFlight 分發流程
---

# iOS 應用程式打包與 TestFlight 內測分發流程

本流程說明如何將 Flutter iOS 應用程式打包並上傳至 App Store Connect 進行 TestFlight 內部測試。

## 前置準備 (Apple Developer Account)

1.  **App ID**: 確保在 [Apple Developer Portal](https://developer.apple.com/account/resources/identifiers/list) 已建立對應的 App ID (Bundle ID)。
2.  **App Store Connect**: 在 [App Store Connect](https://appstoreconnect.apple.com/apps) 建立新的 App，Bundle ID 需與上述一致。

## 1. Xcode 專案設定

1.  使用 Xcode 開啟專案工作區：
    ```bash
    open [APP_DIR]/ios/Runner.xcworkspace
    ```
2.  **設定簽名 (Signing)**:
    - 點擊左側導航欄最上方的 `Runner` (專案根節點)。
    - 選擇 `TARGETS` -> `Runner`。
    - 進入 `Signing & Capabilities` 分頁。
    - 勾選 `Automatically manage signing` (推薦)。
    - **Team**: 選擇您的 Apple Developer Team。
    - **Bundle Identifier**: 確認與 App Store Connect 上設定的一致。
3.  **設定版本號**:
    - 在 `General` 分頁中，設定 `Version` (如 1.0.0) 和 `Build` (如 1)。
    - *注意：每次上傳 TestFlight，Build Number 必須遞增 (如 1 -> 2)。*

## 2. 打包與封存 (Archive)

1.  **選擇目標裝置**:
    - 在 Xcode 頂部工具列，目標設備選擇 **Any iOS Device (arm64)** (不可以選擇模擬器)。
2.  **執行封存**:
    - 點擊選單列 `Product` -> `Archive`。
    - 等待編譯完成 (可能需要幾分鐘)。

## 3. 上傳至 App Store Connect

1.  編譯完成後，會自動彈出 **Organizer** 視窗 (若未彈出，可由 `Window` -> `Organizer` 開啟)。
2.  選擇剛完成的 Archives 項目，點擊右側的 **Distribute App**。
3.  選擇 **App Store Connect** -> Next。
4.  選擇 **Upload** -> Next。
5.  保持勾選所有預設選項 (Upload symbols 等) -> Next。
6.  選擇 **Automatically manage signing** -> Next。
7.  檢查摘要資訊，點擊 **Upload**。
8.  等待上傳成功畫面。

## 4. TestFlight 內部測試設定

1.  登入 [App Store Connect](https://appstoreconnect.apple.com/)。
2.  進入您的 App，點擊上方 **TestFlight** 分頁。
3.  **等待處理**: 剛上傳的版本狀態會顯示為 "Processing" (處理中)，通常需等待 10-20 分鐘。
4.  **解決法規遵循 (Compliance)**:
    - 處理完成後，狀態可能變為 "Missing Compliance"。
    - 點擊 "Manage"，若無使用特殊加密，通常選擇 "None of the algorithms mentioned above" 即可。
5.  **新增內部測試人員**:
    - 左側選單選擇 **Internal Testing** (加上 `+` 號新增群組，如 "Dev Team")。
    - 在群組中點擊 `Testers` 旁的 `+` 號。
    - 選擇您自己 (Admin/Developer 帳號)。
6.  **分發**:
    - 測試人員的手機將收到 Email 邀請。
    - 在手機下載 **TestFlight** App。
    - 透過 Email 連結或 TestFlight App 內的 Redeem Code 安裝應用程式。
