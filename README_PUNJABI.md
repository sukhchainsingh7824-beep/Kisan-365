# KISAN 365 — Kotlin Android Starter

ਇਹ ਮੌਜੂਦਾ ਮਨਜ਼ੂਰਸ਼ੁਦਾ KISAN 365 ਸਕ੍ਰੀਨ ਡਿਜ਼ਾਈਨ ਦਾ Native Android Kotlin + Jetpack Compose starter ਹੈ।

## Android Studio ਵਿੱਚ ਖੋਲ੍ਹਣਾ
1. ZIP extract ਕਰੋ।
2. Android Studio → Open → `KISAN365_Kotlin_Android` ਫੋਲਡਰ ਚੁਣੋ।
3. Gradle Sync ਕਰੋ।
4. SDK 35 ਅਤੇ JDK 17 ਉਪਲਬਧ ਹੋਣੇ ਚਾਹੀਦੇ ਹਨ।

## Firebase ਜੋੜਨਾ
1. Firebase Console ਵਿੱਚ Android app ਬਣਾਓ; package name `in.kisan365.app` ਹੀ ਰੱਖੋ।
2. ਆਪਣੀ `google-services.json` ਫਾਈਲ `app/` ਫੋਲਡਰ ਵਿੱਚ ਰੱਖੋ।
3. Root `build.gradle.kts` ਦੇ plugins ਵਿੱਚ Google Services plugin ਜੋੜੋ:
   `id("com.google.gms.google-services") version "4.4.2" apply false`
4. `app/build.gradle.kts` ਦੇ plugins ਵਿੱਚ ਇਹ ਲਾਈਨ uncomment/add ਕਰੋ:
   `id("com.google.gms.google-services")`
5. Firebase Console → Authentication ਵਿੱਚ Email/Password ਅਤੇ Google providers enable ਕਰੋ।
6. Firestore security rules ਅਤੇ Admin custom claims ਸਰਵਰ ਤੋਂ configure ਕਰੋ।

## ਬਹੁਤ ਜ਼ਰੂਰੀ ਸੁਰੱਖਿਆ
- Admin password ਇਸ starter ਵਿੱਚ production ਲਈ implement ਨਹੀਂ ਹੈ। Dialog ਸਿਰਫ਼ UI placeholder ਹੈ।
- Production Admin authorization Firebase Auth custom claims + Firestore Security Rules / Cloud Functions ਨਾਲ ਹੋਣੀ ਚਾਹੀਦੀ ਹੈ।
- Customer password Firebase Authentication ਸੰਭਾਲੇ; app ਵਿੱਚ plain-text password ਕਦੇ save ਨਾ ਕਰੋ।
- `google-services.json` Firebase client config ਹੈ। `service-account.json` / private keys ਨੂੰ Android app ਵਿੱਚ ਕਦੇ ਨਾ ਰੱਖੋ।
- Dashboard readings sample ਹਨ; motor ON/OFF buttons hardware ਨੂੰ control ਨਹੀਂ ਕਰਦੇ।
- ਅਸਲੀ motor control ਲਈ controller, authenticated command channel, acknowledgement ਅਤੇ independent electrical protections ਲਾਜ਼ਮੀ ਹਨ।
- Timer UI demo ਹੈ; real OFF action controller/backend ਨਾਲ implement ਕਰਨਾ ਬਾਕੀ ਹੈ।

## ਕੀ ਸ਼ਾਮਲ ਹੈ
- Home Power Dashboard, R/Y/B phase cards, motor cards (ਅਧਿਕਤਮ 7)
- Timer, Alerts, Settings
- Customer account UI
- Admin-only Firebase / Google Sign-In / FCM settings layout
- Green/white theme ਅਤੇ bottom navigation
