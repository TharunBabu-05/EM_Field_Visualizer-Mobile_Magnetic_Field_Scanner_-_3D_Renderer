# Gradle Wrapper Missing - QUICK FIX

## Error
```
Could not find or load main class org.gradle.wrapper.GradleWrapperMain
```

## Cause
The `gradle/wrapper/gradle-wrapper.jar` file is missing!

---

## QUICK FIX (Run in PowerShell)

### Option 1: Run Fix Script (Easiest)
```powershell
cd "C:\Users\tharu\new project\EMFieldVisualizer"
.\FixWrapper.ps1
```

### Option 2: Direct Download Command
```powershell
cd "C:\Users\tharu\new project\EMFieldVisualizer"
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar" -OutFile "gradle\wrapper\gradle-wrapper.jar"
```

### Option 3: Manual Download
1. Go to: https://services.gradle.org/distributions/gradle-8.2-bin.zip
2. Download the file
3. Extract it
4. Copy `gradle-8.2/lib/gradle-wrapper.jar` to `EMFieldVisualizer/gradle/wrapper/`

---

## After Downloading Wrapper

Then build normally:
```powershell
$env:JAVA_HOME = "C:\Users\tharu\Downloads\jdk-17.0.12_windows-x64_bin\jdk-17.0.12"
$env:ANDROID_HOME = "C:\Users\tharu\AppData\Local\Android\Sdk"
.\gradlew.bat clean
.\gradlew.bat assembleDebug
```

---

## Why This Happened

The Gradle wrapper JAR was not included when the project was created. This file is needed to bootstrap Gradle. Once downloaded, it will work perfectly.

---

## Files Created
- ✅ `FixWrapper.ps1` - Quick download script
- ✅ `DownloadWrapper.ps1` - Comprehensive download + build script

---

**Run FixWrapper.ps1 now to download the missing file!**
