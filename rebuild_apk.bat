@echo off
echo ========================================
echo    EM Field Visualizer - Rebuild
echo ========================================
echo.

cd EMFieldVisualizer

echo [1/3] Cleaning previous build...
call gradlew.bat clean

echo.
echo [2/3] Building project...
call gradlew.bat assembleDebug

echo.
echo [3/3] Checking for APK...
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo.
    echo ========================================
    echo    BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo APK Location:
    echo app\build\outputs\apk\debug\app-debug.apk
    echo.
    dir app\build\outputs\apk\debug\app-debug.apk
    echo.
) else (
    echo.
    echo APK not found. Check build output above.
)

pause
