# Xinyi POS App

Flutter Android-first POS terminal for Xinyi Flagship Store.

## Current Scope

- Android tablet cashier shell.
- Demo product catalog before API sync is wired.
- Product grid, category filter, cart summary, discount preview and cash checkout CTA.
- Widget tests for first-screen rendering and add-to-cart behavior.

## Verification

```bash
flutter analyze
flutter test
flutter build apk --debug
```

Run on the local Android tablet emulator:

```bash
flutter emulators --launch pos_android_tablet
flutter run -d emulator-5554
```

## Next Steps

- Wire POS PIN login and terminal context.
- Add API client for products, categories and order creation.
- Add SQLite or SQLCipher local cache.
- Add offline queue and sync heartbeat.
