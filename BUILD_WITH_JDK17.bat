@echo off
echo ========================================
echo    EM Field Visualizer - DIRECT BUILD
echo ========================================
echo.
echo Setting up Java environment...
echo.

REM Set JAVA_HOME to your JDK 17
set JAVA_HOME=C:\Users\tharu\Downloads\jdk-17.0.12_windows-x64_bin\jdk-17.0.12

REM Set Android SDK location
set ANDROID_HOME=C:\Users\tharu\AppData\Local\Android\Sdk

REM Add Java to PATH
set PATH=%JAVA_HOME%\bin;%PATH%

echo JAVA_HOME: %JAVA_HOME%
echo ANDROID_HOME: %ANDROID_HOME%
echo.

REM Clean Gradle cache
echo Cleaning Gradle cache...
if exist "%USERPROFILE%\.gradle\caches" (
    echo Deleting cache...
    rd /s /q "%USERPROFILE%\.gradle\caches"
)

echo.
echo Navigating to project...
cd EMFieldVisualizer

echo.
echo Cleaning project...
if exist ".gradle" rd /s /q ".gradle"
if exist "build" rd /s /q "build"
if exist "app\build" rd /s /q "app\build"

echo.
echo Building APK...
echo This may take a few minutes...
echo.

call gradlew.bat clean
call gradlew.bat assembleDebug

echo.
echo ========================================
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo    BUILD SUCCESS!
    echo ========================================
    echo.
    echo APK Location:
    echo %CD%\app\build\outputs\apk\debug\app-debug.apk
    echo.
    for %%A in ("app\build\outputs\apk\debug\app-debug.apk") do (
        echo Size: %%~zA bytes
    )
    echo.
) else (
    echo    BUILD FAILED
    echo ========================================
    echo.
    echo Check errors above.
)

pause
