# Gradle Wrapper Download and Setup Script

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Downloading Gradle Wrapper" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$wrapperDir = "gradle\wrapper"
$wrapperJar = "$wrapperDir\gradle-wrapper.jar"
$wrapperUrl = "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar"

Write-Host "Checking wrapper directory..." -ForegroundColor Yellow

if (!(Test-Path $wrapperDir)) {
    Write-Host "Creating wrapper directory..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
}

Write-Host "Downloading gradle-wrapper.jar..." -ForegroundColor Yellow
Write-Host "From: $wrapperUrl" -ForegroundColor Gray

try {
    # Download using .NET WebClient (more reliable than Invoke-WebRequest)
    $webClient = New-Object System.Net.WebClient
    $webClient.DownloadFile($wrapperUrl, $wrapperJar)
    
    if (Test-Path $wrapperJar) {
        $size = (Get-Item $wrapperJar).Length
        Write-Host "Downloaded successfully! Size: $size bytes" -ForegroundColor Green
    }
}
catch {
    Write-Host "Failed to download from GitHub. Trying alternative..." -ForegroundColor Red
    
    # Alternative: Download from services.gradle.org
    $altUrl = "https://services.gradle.org/distributions/gradle-8.2-bin.zip"
    Write-Host "Downloading Gradle distribution..." -ForegroundColor Yellow
    
    $tempZip = "$env:TEMP\gradle-8.2-bin.zip"
    $webClient = New-Object System.Net.WebClient
    $webClient.DownloadFile($altUrl, $tempZip)
    
    # Extract just the wrapper jar
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [System.IO.Compression.ZipFile]::OpenRead($tempZip)
    $entry = $zip.Entries | Where-Object { $_.FullName -like "*/gradle-wrapper.jar" } | Select-Object -First 1
    
    if ($entry) {
        [System.IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $wrapperJar, $true)
        Write-Host "Extracted gradle-wrapper.jar from distribution!" -ForegroundColor Green
    }
    
    $zip.Dispose()
    Remove-Item $tempZip -Force
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Building APK" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set environment variables
$env:JAVA_HOME = "C:\Users\tharu\Downloads\jdk-17.0.12_windows-x64_bin\jdk-17.0.12"
$env:ANDROID_HOME = "C:\Users\tharu\AppData\Local\Android\Sdk"

Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Gray
Write-Host "ANDROID_HOME: $env:ANDROID_HOME" -ForegroundColor Gray
Write-Host ""

# Clean and build
Write-Host "Cleaning project..." -ForegroundColor Yellow
.\gradlew.bat clean

Write-Host ""
Write-Host "Building APK..." -ForegroundColor Yellow
.\gradlew.bat assembleDebug

Write-Host ""
if (Test-Path "app\build\outputs\apk\debug\app-debug.apk") {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "   SUCCESS! APK Built!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "APK: $(Get-Location)\app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Cyan
    $apkSize = (Get-Item "app\build\outputs\apk\debug\app-debug.apk").Length
    Write-Host "Size: $apkSize bytes" -ForegroundColor Cyan
}
else {
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "   BUILD FAILED" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
}

Read-Host "`nPress Enter to exit"
