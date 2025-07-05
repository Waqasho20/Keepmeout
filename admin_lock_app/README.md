# Admin Lock App

ایک مکمل Flutter-based Android ایپلیکیشن جو ایڈمن کنٹرول کے ساتھ ڈیوائس کو محفوظ طریقے سے لاک کرنے کی سہولت فراہم کرتی ہے۔

## خصوصیات (Features)

### بنیادی خصوصیات
- **ایڈمن کنٹرول**: مکمل ایڈمن سطح کا کنٹرول
- **ٹائمر بیسڈ لاک**: 1 سے 60 منٹ تک کا لاک ٹائم سیٹ کریں
- **محفوظ پاس ورڈ**: ایڈمن پاس ورڈ کے ذریعے جلدی انلاک
- **فل اسکرین لاک**: مکمل اسکرین لاک موڈ
- **سسٹم لیول سیکیورٹی**: DevicePolicyManager استعمال

### سیکیورٹی فیچرز
- **بیک/ہوم بٹن بلاک**: تمام نیویگیشن بٹن غیر فعال
- **ایپ سوئچنگ روکنا**: ٹاسک سوئچنگ مکمل طور پر بند
- **اسکرین پننگ**: Lock Task Mode کا استعمال
- **فورگرانڈ سروس**: مستقل لاک برقراری
- **ری بوٹ پرسسٹنس**: ڈیوائس ری سٹارٹ کے بعد بھی لاک برقرار

## تکنیکی تفصیلات

### استعمال شدہ ٹیکنالوجیز
- **Flutter**: UI اور کراس پلیٹفارم ڈیولپمنٹ
- **Android Native (Kotlin)**: سسٹم لیول فیچرز
- **DevicePolicyManager**: ڈیوائس ایڈمن کنٹرول
- **MethodChannel**: Flutter-Android کمیونیکیشن
- **Foreground Service**: بیک گرانڈ پروسیسنگ

### آرکیٹیکچر
```
┌─────────────────┐
│   Flutter UI    │
│  (Dart/Widget)  │
├─────────────────┤
│  MethodChannel  │
│  Communication  │
├─────────────────┤
│ Android Native  │
│    (Kotlin)     │
├─────────────────┤
│ System Services │
│ DeviceAdmin/etc │
└─────────────────┘
```

## انسٹالیشن گائیڈ

### پیش شرائط
1. **Flutter SDK** (3.8.1 یا اوپر)
2. **Android SDK** (API Level 34)
3. **Java/OpenJDK** (17 یا اوپر)
4. **Android Studio** (اختیاری لیکن بہتر)

### قدم بہ قدم انسٹالیشن

#### 1. Flutter SDK سیٹ اپ
```bash
# Flutter SDK ڈاؤن لوڈ کریں
git clone -b stable https://github.com/flutter/flutter.git
export PATH="$PATH:`pwd`/flutter/bin"

# Flutter doctor چلائیں
flutter doctor
```

#### 2. Android SDK سیٹ اپ
```bash
# Android command-line tools ڈاؤن لوڈ کریں
wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip

# SDK directory بنائیں
mkdir -p ~/Android/sdk/cmdline-tools/latest
unzip commandlinetools-linux-*.zip -d ~/Android/sdk/cmdline-tools/latest

# Environment variables سیٹ کریں
export ANDROID_HOME=~/Android/sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools

# Required packages انسٹال کریں
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

#### 3. Java/OpenJDK انسٹال کریں
```bash
# Ubuntu/Debian پر
sudo apt update
sudo apt install openjdk-17-jdk

# Environment variable سیٹ کریں
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

#### 4. پروجیکٹ بلڈ کریں
```bash
# پروجیکٹ directory میں جائیں
cd admin_lock_app

# Dependencies انسٹال کریں
flutter pub get

# APK بلڈ کریں
flutter build apk

# یا debug version کے لیے
flutter build apk --debug
```

## استعمال کی گائیڈ

### پہلی بار سیٹ اپ

#### 1. ایپ انسٹال کریں
```bash
# APK انسٹال کریں
adb install build/app/outputs/flutter-apk/app-release.apk

# یا debug version
adb install build/app/outputs/flutter-apk/app-debug.apk
```

#### 2. Device Admin Permissions
- ایپ کھولنے پر Device Admin permission کی درخواست آئے گی
- **"Activate"** پر کلک کریں
- یہ permission ضروری ہے سسٹم لیول لاک کے لیے

#### 3. Additional Permissions
ایپ کو مندرجہ ذیل permissions کی ضرورت ہو سکتی ہے:
- **System Alert Window**: اوور لے کے لیے
- **Notification Access**: نوٹیفیکیشن کے لیے
- **Accessibility Service**: (اختیاری) بہتر کنٹرول کے لیے

### بنیادی استعمال

#### 1. ایڈمن پاس ورڈ سیٹ کریں
- **"Set Admin Password"** فیلڈ میں محفوظ پاس ورڈ داخل کریں
- کم از کم 4 کریکٹر ضروری ہیں
- یہ پاس ورڈ جلدی انلاک کے لیے استعمال ہوگا

#### 2. لاک ٹائم سیٹ کریں
- **Slider** استعمال کرکے 1-60 منٹ سیٹ کریں
- یا **Quick buttons** (5m, 10m, 15m, 30m) استعمال کریں
- ٹائم کی تصدیق کریں

#### 3. لاک شروع کریں
- **"START LOCK"** بٹن دبائیں
- ایپ فوری طور پر لاک موڈ میں چلا جائے گا
- اسکرین مکمل طور پر لاک ہو جائے گی

### لاک اسکرین کا استعمال

#### ٹائمر ڈسپلے
- **بقیہ وقت** بڑے نمبرز میں دکھایا جاتا ہے
- فارمیٹ: `MM:SS` یا `HH:MM:SS`
- ریئل ٹائم اپڈیٹ

#### ایڈمن انلاک
1. **"Admin Unlock"** بٹن دبائیں
2. **Number pad** استعمال کرکے پاس ورڈ داخل کریں
3. **"Unlock"** بٹن دبائیں
4. غلط پاس ورڈ کی صورت میں فیلڈ صاف ہو جائے گا

#### خودکار انلاک
- ٹائمر مکمل ہونے پر خودکار طور پر انلاک
- ایڈمن اسکرین پر واپسی

## فائل ڈھانچہ (File Structure)

```
admin_lock_app/
├── lib/
│   ├── main.dart                 # Main app entry point
│   ├── screens/
│   │   ├── admin_screen.dart     # Admin control interface
│   │   └── lock_screen.dart      # Lock screen UI
│   └── services/
│       └── lock_service.dart     # Flutter-Android communication
├── android/
│   └── app/src/main/
│       ├── AndroidManifest.xml   # App permissions & components
│       ├── res/xml/
│       │   └── device_admin.xml  # Device admin policies
│       └── kotlin/com/example/admin_lock_app/
│           ├── MainActivity.kt    # Main activity with MethodChannel
│           ├── AdminReceiver.kt   # Device admin receiver
│           ├── LockService.kt     # Foreground service
│           └── BootReceiver.kt    # Boot event handler
├── test/
│   └── widget_test.dart          # Unit tests
├── pubspec.yaml                  # Flutter dependencies
└── README.md                     # Documentation
```

## تکنیکی تفصیلات

### Android Manifest Permissions
```xml
<!-- Device Admin -->
<uses-permission android:name="android.permission.BIND_DEVICE_ADMIN" />

<!-- System Control -->
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.DISABLE_KEYGUARD" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

<!-- Service & Notifications -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Boot & Task Management -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.REORDER_TASKS" />
```

### Device Admin Policies
```xml
<uses-policies>
    <force-lock />           <!-- Device locking -->
    <disable-keyguard />     <!-- Keyguard control -->
    <watch-login />          <!-- Login monitoring -->
    <wipe-data />           <!-- Emergency wipe -->
</uses-policies>
```

### MethodChannel Communication
```dart
// Flutter to Android
await _channel.invokeMethod('startLock', {
  'duration': durationMinutes,
  'password': password,
});

// Android to Flutter
MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
  .setMethodCallHandler { call, result ->
    when (call.method) {
      "startLock" -> { /* Handle lock */ }
    }
  }
```

## سیکیورٹی کی تفصیلات

### محفوظیت کے طریقے

#### 1. Device Admin Integration
- **DevicePolicyManager** کا استعمال
- سسٹم لیول device locking
- Admin privileges کی ضرورت

#### 2. Lock Task Mode
- **startLockTask()** method
- Screen pinning functionality
- App switching prevention

#### 3. Key Event Blocking
```kotlin
override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    if (isLocked) {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_HOME,
            KeyEvent.KEYCODE_MENU -> return true // Block
        }
    }
    return super.onKeyDown(keyCode, event)
}
```

#### 4. Foreground Service
- **Persistent background operation**
- Wake lock maintenance
- Timer management
- Notification display

#### 5. Boot Persistence
- **BootReceiver** for restart handling
- SharedPreferences for state storage
- Automatic lock restoration

### ممکنہ کمزوریاں اور حل

#### 1. Root Access
**مسئلہ**: Root users bypass restrictions
**حل**: Root detection اور warning

#### 2. Safe Mode
**مسئلہ**: Safe mode میں app disable
**حل**: System app installation (requires root)

#### 3. ADB Commands
**مسئلہ**: Developer options سے bypass
**حل**: Developer options detection

#### 4. Force Stop
**مسئلہ**: Settings سے force stop
**حل**: System app status اور protection

## ٹربل شوٹنگ

### عام مسائل اور حل

#### 1. Device Admin Permission نہیں مل رہی
**علامات**: "Device admin privileges required" error
**حل**:
```bash
# Manual activation
adb shell dpm set-device-admin com.example.admin_lock_app/.AdminReceiver
```

#### 2. App Build نہیں ہو رہا
**علامات**: Gradle build failures
**حل**:
```bash
# Clean build
flutter clean
flutter pub get
flutter build apk --verbose
```

#### 3. Lock Screen سے باہر نکل جانا
**علامات**: Back/Home button working
**حل**:
- Device Admin permission check کریں
- Lock Task Mode status verify کریں
- Foreground service running check کریں

#### 4. Timer Reset ہو جانا
**علامات**: Timer not persisting
**حل**:
- SharedPreferences data check کریں
- BootReceiver registration verify کریں
- Service restart mechanism check کریں

### Debug Commands
```bash
# Check device admin status
adb shell dpm list-owners

# Check running services
adb shell dumpsys activity services

# Check app permissions
adb shell dumpsys package com.example.admin_lock_app

# Force stop (for testing)
adb shell am force-stop com.example.admin_lock_app
```

## کارکردگی کی بہتری

### بیٹری کی بچت
- **Efficient timer implementation**
- **Minimal background processing**
- **Smart wake lock usage**

### میموری کا استعمال
- **Proper resource cleanup**
- **Efficient UI rendering**
- **Memory leak prevention**

### CPU کا استعمال
- **Optimized native code**
- **Minimal polling**
- **Event-driven architecture**

## مستقبل کی بہتریاں

### منصوبہ بند فیچرز
1. **Biometric unlock** - فنگر پرنٹ/فیس انلاک
2. **Remote control** - دور سے کنٹرول
3. **Multiple profiles** - مختلف یوزر پروفائلز
4. **Usage analytics** - استعمال کی رپورٹس
5. **Parental controls** - والدین کا کنٹرول

### تکنیکی بہتریاں
1. **Kotlin Multiplatform** - iOS support
2. **Jetpack Compose** - Modern Android UI
3. **Room Database** - Local data storage
4. **WorkManager** - Background task management
5. **Firebase Integration** - Cloud features

## لائسنس اور قانونی معلومات

### استعمال کی شرائط
- یہ ایپ تعلیمی اور ذاتی استعمال کے لیے ہے
- تجارتی استعمال سے پہلے اجازت لیں
- غلط استعمال کی ذمہ داری صارف کی ہے

### ذمہ داری سے بری
- ڈیولپر کسی نقصان کا ذمہ دار نہیں
- اپنے خطرے پر استعمال کریں
- بیک اپ اور احتیاط ضروری ہے

### رابطہ معلومات
- **Developer**: Manus AI Assistant
- **Support**: GitHub Issues
- **Documentation**: README.md

---

**نوٹ**: یہ ایپ محفوظ اور ذمہ دارانہ استعمال کے لیے بنائی گئی ہے۔ کسی بھی غیر قانونی یا نقصان دہ مقصد کے لیے استعمال نہ کریں۔

