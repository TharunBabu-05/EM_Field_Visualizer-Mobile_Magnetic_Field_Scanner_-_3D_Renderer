@echo off
echo ========================================
echo    Rebuilding Fixed APK
echo ========================================
echo.

set JAVA_HOME=C:\Users\tharu\Downloads\jdk-17.0.12_windows-x64_bin\jdk-17.0.12
set ANDROID_HOME=C:\Users\tharu\AppData\Local\Android\Sdk

cd EMFieldVisualizer

echo Building APK...
call gradlew.bat assembleDebug

if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo.
    echo ========================================
    echo    BUILD SUCCESS!
    echo ========================================
    echo.
    echo Installing on device...
    "%ANDROID_HOME%\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"
    echo.
    echo Done! Try opening the app now!
) else (
    echo Build failed!
)

pause
