# Building the EM Field Visualizer App

## Prerequisites

1. **Android Studio** (Arctic Fox or later)
   - Download from: https://developer.android.com/studio

2. **Android SDK**
   - SDK Platform 34 (Android 14)
   - SDK Build-Tools 34.0.0+
   - NDK 25.1+

3. **Java Development Kit (JDK)**
   - JDK 17 required

## Building from Android Studio

### Step 1: Open the Project

1. Launch Android Studio
2. Click "Open" or "File > Open"
3. Navigate to `EMFieldVisualizer` folder
4. Click "OK"
5. Wait for Gradle sync to complete (~2-5 minutes first time)

### Step 2: Configure NDK (if needed)

1. Go to `Tools > SDK Manager`
2. Click "SDK Tools" tab
3. Check "NDK (Side by side)"
4. Click "Apply" and wait for download

### Step 3: Build the APK

**Option A: Debug APK (for testing)**

1. In Android Studio menu: `Build > Build Bundle(s) / APK(s) > Build APK(s)`
2. Wait for build to complete (~5-10 minutes first time)
3. Click "locate" in the notification popup
4. APK location: `app/build/outputs/apk/debug/app-debug.apk`

**Option B: Release APK (smaller, optimized)**

1. In Android Studio menu: `Build > Generate Signed Bundle / APK`
2. Select "APK" and click "Next"
3. Create a new keystore or use existing:
   - Keystore path: Choose location
   - Password: Create secure password
   - Key alias: e.g., "emfield-key"
   - Key password: Create secure password
   - Validity: 25 years
   - First/Last Name: Your name
   - Organization: Your org or "Personal"
4. Click "Next"
5. Select "release" build variant
6. Check "V2 (Full APK Signature)"
7. Click "Finish"
8. APK location: `app/release/app-release.apk`

### Step 4: Install on Device

**Via Android Studio:**
1. Connect Android device via USB
2. Enable "USB Debugging" on device
3. Click "Run" (green play button) in Android Studio
4. Select your device
5. App installs and launches automatically

**Via ADB (Command Line):**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Via File Transfer:**
1. Copy APK to phone
2. Open file on phone
3. Allow installation from unknown sources (if prompted)
4. Tap "Install"

## Building from Command Line

### Windows:

```cmd
cd EMFieldVisualizer
gradlew assembleDebug
```

### Linux/Mac:

```bash
cd EMFieldVisualizer
./gradlew assembleDebug
```

**Output location:**
- Debug APK: `app\build\outputs\apk\debug\app-debug.apk`
- Release APK: `app\build\outputs\apk\release\app-release.apk` (after signing)

## Build Variants

### Debug Build
- **Size**: ~25-30 MB
- **Performance**: Good (includes debug symbols)
- **Use case**: Testing, development
- **Command**: `gradlew assembleDebug`

### Release Build
- **Size**: ~15-20 MB (after ProGuard)
- **Performance**: Optimized
- **Use case**: Distribution, production
- **Command**: `gradlew assembleRelease` (requires signing)

## Troubleshooting

### "NDK not configured"
```
Tools > SDK Manager > SDK Tools > Install NDK
```

### "Gradle sync failed"
```
File > Invalidate Caches and Restart
```

### "SDK not found"
```
File > Project Structure > SDK Location > Set Android SDK location
```

### "Out of memory"
In `gradle.properties`, increase:
```
org.gradle.jvmargs=-Xmx4096m
```

### "Native library not found"
1. Check NDK is installed
2. Clean and rebuild: `Build > Clean Project`, then `Build > Rebuild Project`

## APK Size Optimization

Current optimizations in place:
- ProGuard enabled for release builds
- Resource shrinking enabled
- Native libraries for 4 ABIs (can reduce to 2 for smaller APK)

To reduce size further, edit `app/build.gradle.kts`:
```kotlin
ndk {
    abiFilters += listOf("arm64-v8a", "armeabi-v7a")  // Remove x86 variants
}
```

## Verification

After building, verify the APK:

```bash
# Check APK contents
unzip -l app-debug.apk

# Check APK size
ls -lh app-debug.apk

# Analyze APK (Android Studio)
Build > Analyze APK... > Select your APK
```

## Distribution

### Google Play Store
1. Create release APK (signed)
2. Go to https://play.google.com/console
3. Create app listing
4. Upload APK
5. Fill in required information
6. Submit for review

### Direct Distribution
1. Host APK on website
2. Provide download link
3. Users must enable "Install from unknown sources"

## Development Tips

- Use debug build for development (faster builds)
- Test on real device (emulator doesn't have magnetometer)
- Enable "Instant Run" for faster iteration
- Use logcat for debugging: `View > Tool Windows > Logcat`

---

**Build time estimates:**
- First build: 5-10 minutes (downloads dependencies)
- Incremental builds: 30-60 seconds
- Clean rebuild: 2-3 minutes
