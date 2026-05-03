@echo off
echo ========================================
echo    Cleaning Gradle Cache
echo ========================================
echo.

echo Cleaning transforms cache...
if exist "%USERPROFILE%\.gradle\caches\transforms-3" (
    rmdir /s /q "%USERPROFILE%\.gradle\caches\transforms-3"
    echo Transforms cache cleaned.
) else (
    echo Transforms cache doesn't exist.
)

echo.
echo Cleaning modules cache...
if exist "%USERPROFILE%\.gradle\caches\modules-2" (
    rmdir /s /q "%USERPROFILE%\.gradle\caches\modules-2"
    echo Modules cache cleaned.
) else (
    echo Modules cache doesn't exist.
)

echo.
echo ========================================
echo    Gradle Cache Cleaned!
echo ========================================
echo.
echo Now building APK...
echo.

cd EMFieldVisualizer
call gradlew.bat clean assembleDebug

if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo.
    echo ========================================
    echo    SUCCESS! APK Built!
    echo ========================================
    echo.
    echo Your APK: %CD%\app\build\outputs\apk\debug\app-debug.apk
    echo.
) else (
    echo.
    echo Build failed. Check errors above.
    echo.
)

pause
