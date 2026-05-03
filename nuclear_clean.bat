@echo off
echo ========================================
echo    NUCLEAR CLEAN - Complete Cache Reset
echo ========================================
echo.
echo This will delete ALL Gradle caches and rebuild from scratch.
echo This may take 5-10 minutes but will fix all cache issues.
echo.
pause

echo.
echo [1/5] Deleting entire Gradle cache...
if exist "%USERPROFILE%\.gradle\caches" (
    echo Please wait, this may take a moment...
    rmdir /s /q "%USERPROFILE%\.gradle\caches"
    echo Gradle cache deleted!
) else (
    echo No cache to delete.
)

echo.
echo [2/5] Cleaning project .gradle...
cd EMFieldVisualizer
if exist ".gradle" (
    rmdir /s /q ".gradle"
    echo Project .gradle cleaned!
)

echo.
echo [3/5] Cleaning build folders...
if exist "build" rmdir /s /q "build"
if exist "app\build" rmdir /s /q "app\build"
echo Build folders cleaned!

echo.
echo [4/5] Running Gradle clean...
call gradlew.bat clean

echo.
echo [5/5] Building with fresh dependencies...
echo This will download ~200MB of dependencies...
call gradlew.bat assembleDebug --no-daemon --refresh-dependencies

echo.
echo ========================================
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo    BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo Your APK is ready:
    echo %CD%\app\build\outputs\apk\debug\app-debug.apk
    echo.
) else (
    echo    BUILD FAILED
    echo ========================================
    echo.
    echo Check errors above.
)

pause
