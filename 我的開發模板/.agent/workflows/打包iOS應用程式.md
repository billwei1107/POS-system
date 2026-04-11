---
description: (僅適用於 Flutter 專案) 執行 iOS 應用程式的深度清理與 Release 版本打包安裝流程 (解決 Resource Fork 與簽名問題)
---

<!--
  TODO: 使用此模板前請確認：
  - APP_DIR: Flutter App 目錄的相對路徑 (例：frontend/app)
-->

# iOS 應用程式打包與安裝流程 (Release Mode)

本流程旨在解決常見的 `resource fork`、`Permission denied` 與 Xcode 索引錯誤，確保能成功將 Release 版 App 安裝至實體 iPhone。

## 1. 深度清理 (Deep Clean)

當遇到簽名錯誤或編譯失敗時，首先執行此步驟以確保環境乾淨。

1. 開啟終端機 (Terminal)。
2. 切換至 App 目錄：
   ```bash
   cd [APP_DIR]
   ```
3. 執行核彈級清理指令 (一次複製貼上執行)：
   ```bash
   # 清除 Xcode DerivedData 快取
   rm -rf ~/Library/Developer/Xcode/DerivedData
   
   # 清除 Flutter 建置檔
   flutter clean
   
   # 重新下載 Flutter 套件
   flutter pub get
   
   # 重置 iOS Pods
   cd ios
   rm -rf Pods Podfile.lock
   pod install
   
   # 修復權限並清除隱藏屬性 (關鍵修復步驟)
   chmod -R u+rw,go+r .
   xattr -rc .
   
   # 回到 app 根目錄
   cd ..
   ```

## 2. 編譯與安裝 (使用 Xcode)

雖然終端機 (`flutter build ios --release`) 可以編譯，但使用 Xcode 能自動處理大部分的簽名與設定問題，成功率最高。

1. **開啟 Xcode 專案**：
   在終端機執行：
   ```bash
   open ios/Runner.xcworkspace
   ```

2. **清理建置資料夾 (Clean Build Folder)**：
   *   點選上方選單 **Product** > **Clean Build Folder**。
   *   或者是按下快捷鍵 `Cmd` + `Shift` + `K`。
   *   *注意：這能解決 `No such file or directory` 等索引錯誤。*

3. **設定 Release 模式**：
   *   點選上方選單 **Product** > **Scheme** > **Edit Scheme...**。
   *   在左側列表選擇 **Run**。
   *   將右側的 **Build Configuration** 下拉選單改為 **Release**。
   *   點擊 **Close** 關閉視窗。

4. **連接並選擇裝置**：
   *   使用傳輸線將 iPhone 連接至電腦。
   *   在 Xcode 左上角的裝置選單中，選擇您的實體 iPhone。

5. **開始安裝**：
   *   點擊左上角的 **Play (三角形)** 按鈕，或按下 `Cmd` + `R`。
   *   Xcode 會開始編譯、簽名並安裝 App 至手機。
   *   *注意：如果手機上出現「不受信任的開發者」，請至手機的「設定 > 一般 > VPN 與裝置管理」中信任該憑證。*

## 常見問題排除

*   **錯誤：`resource fork, Finder information... not allowed`**
    *   解法：這代表檔案屬性髒了。請務必重新執行 **步驟 1 的深度清理**，特別是 `xattr -rc .` 這行指令。

*   **錯誤：`Permission denied`**
    *   解法：執行 `chmod -R u+rw,go+r ios/Pods` 來修復權限。

*   **錯誤：`No such file or directory` (針對某些 Pod)**
    *   解法：這是因為 Xcode 索引亂掉。請務必執行 **步驟 2 的「Clean Build Folder」**。
