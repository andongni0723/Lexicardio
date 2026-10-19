# Changelog

## v0.15.3
更新內容:
- 修正重跑發佈流程時因 GitHub Release 已存在而中斷的問題，改為更新既有 APK 附件。

Updates:
- Fixed release retries failing on an existing GitHub Release by updating its APK asset.

## v0.15.2
更新內容:
- 修正 Google Play 發佈設定，使用 PLAY_SERVICE_ACCOUNT_JSON 指定的服務帳戶，不再限制固定 Email。

Updates:
- Fixed Google Play publishing to use the service account configured in PLAY_SERVICE_ACCOUNT_JSON without requiring a hard-coded account email.

## v0.15.1
更新內容:
- 新增 Google Play 內部測試自動發佈，沿用 v0.15.0 的卡組刪除與測驗版面修正。
- 發佈時同時建立簽署 APK 與 AAB，並保留 R8 對照檔。

Updates:
- Added automatic Google Play internal testing releases, including the card set deletion and quiz layout fixes from v0.15.0.
- Release builds now produce signed APKs and App Bundles and retain the R8 mapping.

## v0.15.0
更新內容:
- 新增卡組刪除功能與刪除確認對話框，並同步更新卡組列表及最近學習紀錄。
- 修復考試與學習模式長文字選項被裁切的問題；題目與選項可捲動，Next 按鈕固定顯示於底部。

Updates:
- Added card set deletion with confirmation and updates to card set lists and recent learning history.
- Fixed clipped long answers in test and learn modes; questions and answers scroll while the Next button stays at the bottom.

## v0.14.0
更新內容:
- 新增「最近學習」功能。
- 新增初次開啟教學設定
- 重構部份程式碼

Updates:
- Added "Recent Learned" feature.
- Added new first-time teaching settings
- Refactor some code

## v0.13.1
更新內容:
- 新增「編輯卡組」功能。
- 修復直接下載鏈接錯誤

Updates:
- Add an “Edit Card Set” feature.
- Fix the direct download button with the wrong link.

## v0.13.0
更新內容:
- 新增「編輯卡組」功能。

Updates:
- Add an “Edit Card Set” feature.

## v0.12.0
更新內容:
- 重構「發現新版本」面板。
- 新增「直接下載」按鈕。
- Release 改成使用 arm64 APK 檔案。

Updates:
- Refactor "New Version" panel
- Add a direct download button.
- Change the release to use an arm64 APK file.

## v0.11.0
- Add today learned module.
- Add daily learning goal.
- Fix translation.

## v0.10.5
- Add translation of new update panel.
- Add Change log.

## v0.10.4
- Fix home page crush when open app without connect internet.

## v0.10.3
- New Update Available Panel.

## v0.10.2
- Fix learn mode crush.