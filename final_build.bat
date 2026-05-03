@echo off
echo ========================================
echo    FINAL BUILD - All Errors Fixed
echo ========================================
echo.

cd EMFieldVisualizer

echo [1/2] Building APK (this may take a few minutes)...
call gradlew.bat assembleDebug

echo.
echo ========================================
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo    SUCCESS! APK Built!
    echo ========================================
    echo.
    echo Your APK is ready at:
    echo %CD%\app\build\outputs\apk\debug\app-debug.apk
    echo.
    for %%A in ("app\build\outputs\apk\debug\app-debug.apk") do (
        echo File size: %%~zA bytes
    )
    echo.
    echo You can now:
    echo 1. Install on Android device via USB
    echo 2. Share the APK file
    echo 3. Upload to device for installation
    echo.
) else (
    echo    BUILD FAILED
    echo ========================================
    echo.
    echo Please check the error messages above.
    echo.
)

pause
