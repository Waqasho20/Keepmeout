#!/bin/bash

# Admin Lock App Build Script
# یہ script Flutter app کو build کرنے کے لیے استعمال کریں

echo "🚀 Admin Lock App Build Script شروع ہو رہا ہے..."

# Check if Flutter is installed
if ! command -v flutter &> /dev/null; then
    echo "❌ Flutter SDK نہیں ملا۔ پہلے Flutter install کریں۔"
    exit 1
fi

# Check if Android SDK is set up
if [ -z "$ANDROID_HOME" ]; then
    echo "⚠️  ANDROID_HOME environment variable set نہیں ہے۔"
    echo "Android SDK path set کریں:"
    echo "export ANDROID_HOME=/path/to/android/sdk"
    exit 1
fi

# Check Java installation
if ! command -v java &> /dev/null; then
    echo "❌ Java نہیں ملا۔ OpenJDK 17 install کریں۔"
    exit 1
fi

echo "✅ Prerequisites check مکمل"

# Clean previous builds
echo "🧹 پرانے builds صاف کر رہے ہیں..."
flutter clean

# Get dependencies
echo "📦 Dependencies install کر رہے ہیں..."
flutter pub get

# Run code analysis
echo "🔍 Code analysis چل رہا ہے..."
flutter analyze
if [ $? -ne 0 ]; then
    echo "❌ Code analysis میں errors ہیں۔ پہلے fix کریں۔"
    exit 1
fi

# Run tests
echo "🧪 Tests چل رہے ہیں..."
flutter test
if [ $? -ne 0 ]; then
    echo "⚠️  Tests fail ہوئے، لیکن build جاری رکھ رہے ہیں..."
fi

# Build APK
echo "🔨 APK build کر رہے ہیں..."
flutter build apk --release

if [ $? -eq 0 ]; then
    echo "✅ Build کامیاب!"
    echo "📱 APK location: build/app/outputs/flutter-apk/app-release.apk"
    
    # Show APK info
    APK_PATH="build/app/outputs/flutter-apk/app-release.apk"
    if [ -f "$APK_PATH" ]; then
        APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
        echo "📊 APK size: $APK_SIZE"
        
        # Install instructions
        echo ""
        echo "📲 Install کرنے کے لیے:"
        echo "adb install $APK_PATH"
        echo ""
        echo "یا device پر manually transfer کرکے install کریں۔"
    fi
else
    echo "❌ Build fail ہوا!"
    echo "🔧 Troubleshooting:"
    echo "1. Android SDK properly configured ہے؟"
    echo "2. Java version 17+ ہے؟"
    echo "3. Device admin permissions available ہیں؟"
    exit 1
fi

echo "🎉 Build process مکمل!"

