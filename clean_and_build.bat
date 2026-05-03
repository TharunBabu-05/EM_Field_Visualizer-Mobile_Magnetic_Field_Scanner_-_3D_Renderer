@echo off
echo ========================================
echo    EM Field Visualizer - Clean Build
echo ========================================
echo.

cd EMFieldVisualizer

echo [1/2] Cleaning previous build...
call gradlew.bat clean

echo.
echo [2/2] Building APK...
call gradlew.bat assembleDebug

echo.
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo ========================================
    echo    BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo APK Location:
    echo %CD%\app\build\outputs\apk\debug\app-debug.apk
    echo.
    dir app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo You can now install this APK on your Android device!
    echo.
) else (
    echo ========================================
    echo    BUILD FAILED
    echo ========================================
    echo.
    echo Check the build output above for errors.
    echo.
)

pause
