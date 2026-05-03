@echo off
echo ========================================
echo    ULTIMATE CACHE FIX
echo ========================================
echo.
echo Running PowerShell script to force-delete cache...
echo.

powershell.exe -ExecutionPolicy Bypass -File "%~dp0ForceClean.ps1"

if errorlevel 1 (
    echo.
    echo PowerShell script failed. Trying manual method...
    echo.
    
    echo Manually deleting cache...
    if exist "%USERPROFILE%\.gradle\caches" (
        echo This may take a moment...
        rd /s /q "%USERPROFILE%\.gradle\caches"
    )
    
    cd EMFieldVisualizer
    if exist ".gradle" rd /s /q ".gradle"
    if exist "build" rd /s /q "build"
    if exist "app\build" rd /s /q "app\build"
    
    echo.
    echo Running build...
    call gradlew.bat clean
    call gradlew.bat assembleDebug --no-daemon --refresh-dependencies
)

pause
