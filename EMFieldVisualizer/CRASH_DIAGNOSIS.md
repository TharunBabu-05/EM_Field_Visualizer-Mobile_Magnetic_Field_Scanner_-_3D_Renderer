# App Crash Diagnosis Guide

## 🔍 Getting Crash Logs

The app is crashing on startup. Let's get the error logs to see why.

### Method 1: Using ADB (Recommended)

**1. Connect your phone via USB**

**2. Enable USB Debugging on phone:**
   - Settings → About Phone → Tap "Build Number" 7 times
   - Settings → Developer Options → Enable "USB Debugging"

**3. Run this command in PowerShell:**
```powershell
$env:ANDROID_HOME = "C:\Users\tharu\AppData\Local\Android\Sdk"
cd $env:ANDROID_HOME\platform-tools
.\adb.exe logcat -c  # Clear old logs
.\adb.exe logcat | Select-String "AndroidRuntime|FATAL|Exception"
```

**4. Open the app on your phone**

**5. You'll see the crash error** - copy it and send it to me

---

### Method 2: Simpler ADB Command

```powershell
cd "C:\Users\tharu\AppData\Local\Android\Sdk\platform-tools"
.\adb.exe logcat -d | Select-String "com.emfield.visualizer" -Context 20
```

This shows the last 20 lines around our app's package name.

---

### Method 3: Filter for Crashes Only

```powershell
cd "C:\Users\tharu\AppData\Local\Android\Sdk\platform-tools"
.\adb.exe logcat -d "*:E"  # Show only errors
```

---

## 🎯 Common Crash Causes & Quick Fixes

### 1. Sensor Not Available
**Symptom:** Crash immediately on launch
**Fix:** Check if your phone has a magnetometer

**Test:**
```powershell
.\adb.exe shell pm list features | Select-String "sensor"
```

Look for: `android.hardware.sensor.compass`

**If missing:** Need to make sensors optional in manifest

---

### 2. Missing Permissions
**Symptom:** Crash when accessing sensors
**Fix:** Grant permissions manually

**Grant all permissions:**
```powershell
cd "C:\Users\tharu\AppData\Local\Android\Sdk\platform-tools"
.\adb.exe shell pm grant com.emfield.visualizer android.permission.BODY_SENSORS
.\adb.exe shell pm grant com.emfield.visualizer android.permission.HIGH_SAMPLING_RATE_SENSORS
```

---

### 3. API Level Mismatch
**Check Android version:**
```powershell
.\adb.exe shell getprop ro.build.version.sdk
```

Our app requires: **API 26+ (Android 8.0+)**

If your phone is older, the app won't work.

---

### 4. Native Library Missing
**Symptom:** UnsatisfiedLinkError
**Check if native libs are in APK:**
```powershell
cd "C:\Users\tharu\new project\EMFieldVisualizer"
Add-Type -AssemblyName System.IO.Compression.FileSystem
$apk = [System.IO.Compression.ZipFile]::OpenRead("app\build\outputs\apk\debug\app-debug.apk")
$apk.Entries | Where-Object { $_.FullName -like "*/lib/*" }
$apk.Dispose()
```

Should show:
- lib/armeabi-v7a/libsensor_native.so
- lib/arm64-v8a/libsensor_native.so
- etc.

---

## 🚀 Quick Test Commands

**Install app:**
```powershell
cd "C:\Users\tharu\AppData\Local\Android\Sdk\platform-tools"
.\adb.exe install -r "C:\Users\tharu\new project\EMFieldVisualizer\app\build\outputs\apk\debug\app-debug.apk"
```

**Clear app data (fresh start):**
```powershell
.\adb.exe shell pm clear com.emfield.visualizer
```

**Launch app and capture crash:**
```powershell
# Clear logs
.\adb.exe logcat -c

# Launch app
.\adb.exe shell am start -n com.emfield.visualizer/.ui.MainActivity

# Get crash log
.\adb.exe logcat -d | Select-String "FATAL" -Context 50
```

---

## 📱 What I Need From You

**Run this and send me the output:**
```powershell
cd "C:\Users\tharu\AppData\Local\Android\Sdk\platform-tools"

# Clear old logs
.\adb.exe logcat -c

# Open the app on your phone now!
# (Wait for it to crash)

# Then run this:
.\adb.exe logcat -d | Select-String "AndroidRuntime" -Context 30
```

Send me the crash log and I'll tell you exactly what's wrong and how to fix it!

---

## 🔧 Likely Issues to Check

Based on our app, most likely causes:
1. **Phone doesn't have magnetometer** (required sensor)
2. **Android version too old** (need 8.0+)
3. **Permissions not granted** (sensors)
4. **Native library not loaded** (C++ code)

The crash log will tell us exactly which one it is!
