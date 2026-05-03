@echo off
echo ========================================
echo    EM Field Visualizer - Get Crash Log
echo ========================================
echo.

set ADB_PATH=C:\Users\tharu\AppData\Local\Android\Sdk\platform-tools\adb.exe

if not exist "%ADB_PATH%" (
    echo ADB not found at: %ADB_PATH%
    echo.
    echo Please check your Android SDK path.
    pause
    exit /b 1
)

echo Checking if device is connected...
"%ADB_PATH%" devices
echo.

echo Clearing old logs...
"%ADB_PATH%" logcat -c
echo.

echo ========================================
echo    NOW OPEN THE APP ON YOUR PHONE
echo ========================================
echo.
echo Press any key after the app crashes...
pause >nul

echo.
echo Fetching crash log...
echo.
echo ========================================
"%ADB_PATH%" logcat -d | findstr /C:"FATAL" /C:"AndroidRuntime" /C:"Exception" /C:"emfield"
echo ========================================
echo.

echo Saving full log to crash_log.txt...
"%ADB_PATH%" logcat -d > crash_log.txt
echo Done! Check crash_log.txt for full details.
echo.

pause
