---
description: (僅適用於 Flutter 專案) 執行 Flutter 打包 APK，並依據版本號自動命名與移動至 root/apk 目錄，最後安裝至手機
---

<!--
  TODO: 使用此模板前請填寫以下變數：
  - APP_DIR: Flutter App 目錄的相對路徑 (例：frontend/app)
  - APK_NAME_PREFIX: APK 檔案命名前綴 (例：my-app)
-->

1. **自動更新版本號**
   // turbo
   cd [APP_DIR] && VER_LINE=$(grep '^version: ' pubspec.yaml) && OLD_VER=$(echo $VER_LINE | sed 's/version: //; s/+.*//') && BUILD_NUM=$(echo $VER_LINE | sed 's/.*+//') && IFS='.' read -r MAJOR MINOR PATCH <<< "$OLD_VER" && NEW_PATCH=$((PATCH + 1)) && NEW_BUILD=$((BUILD_NUM + 1)) && NEW_VER="${MAJOR}.${MINOR}.${NEW_PATCH}+${NEW_BUILD}" && sed -i '' "s/^version: .*/version: $NEW_VER/" pubspec.yaml && echo "Bumped version: $OLD_VER+$BUILD_NUM -> $NEW_VER"

2. **編譯與搬運**
   // turbo
   cd [APP_DIR] && VERSION=$(grep '^version: ' pubspec.yaml | sed 's/version: //; s/+.*//') && flutter build apk --release && cd ../.. && mkdir -p apk && mv [APP_DIR]/build/app/outputs/flutter-apk/app-release.apk apk/[APK_NAME_PREFIX]-$VERSION.apk

3. **確認檔案**
   // turbo
   ls -lh apk/ | tail -n 5

4. **安裝至手機**
   // turbo
   VERSION=$(grep '^version: ' [APP_DIR]/pubspec.yaml | sed 's/version: //; s/+.*//') && adb install -r apk/[APK_NAME_PREFIX]-$VERSION.apk
