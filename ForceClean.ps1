# Force delete Gradle cache using PowerShell
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   FORCE DELETING Gradle Cache" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$gradleCache = "$env:USERPROFILE\.gradle\caches"

if (Test-Path $gradleCache) {
    Write-Host "Forcing deletion of: $gradleCache" -ForegroundColor Yellow
    Write-Host "This may take a minute..." -ForegroundColor Yellow
    
    # Use Remove-Item with Force to handle locked files
    try {
        Remove-Item -Path $gradleCache -Recurse -Force -ErrorAction Stop
        Write-Host "Cache deleted successfully!" -ForegroundColor Green
    }
    catch {
        Write-Host "Error deleting cache: $_" -ForegroundColor Red
        Write-Host "Trying alternative method..." -ForegroundColor Yellow
        
        # Alternative: Use cmd rmdir
        cmd /c "rmdir /s /q `"$gradleCache`""
        Write-Host "Alternative deletion attempted." -ForegroundColor Yellow
    }
}
else {
    Write-Host "Cache doesn't exist - already clean!" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Cleaning Project" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Set-Location "EMFieldVisualizer"

# Clean project .gradle
if (Test-Path ".gradle") {
    Remove-Item -Path ".gradle" -Recurse -Force
    Write-Host "Project .gradle cleaned!" -ForegroundColor Green
}

# Clean build folders
if (Test-Path "build") {
    Remove-Item -Path "build" -Recurse -Force
    Write-Host "Root build cleaned!" -ForegroundColor Green
}
if (Test-Path "app\build") {
    Remove-Item -Path "app\build" -Recurse -Force
    Write-Host "App build cleaned!" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Building APK" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Build with fresh dependencies
.\gradlew.bat clean assembleDebug --no-daemon --refresh-dependencies

Write-Host ""
if (Test-Path "app\build\outputs\apk\debug\app-debug.apk") {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "   SUCCESS! APK Built!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "APK: $(Get-Location)\app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Cyan
}
else {
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "   BUILD FAILED" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
}

Read-Host "`nPress Enter to exit"
