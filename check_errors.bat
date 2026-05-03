@echo off
echo ========================================
echo    Checking Compilation Errors
echo ========================================
echo.

cd EMFieldVisualizer

echo [1/2] Cleaning project...
call gradlew.bat clean

echo.
echo [2/2] Compiling with detailed errors...
call gradlew.bat compileDebugKotlin --info > build_log.txt 2>&1

echo.
echo Build log saved to: EMFieldVisualizer\build_log.txt
echo.

echo Last 50 lines of build output:
echo ========================================
powershell -Command "Get-Content build_log.txt -Tail 50"

pause
