# Quick Fix: Download Gradle Wrapper JAR

Write-Host "Downloading gradle-wrapper.jar..." -ForegroundColor Yellow

# Create directory if needed
New-Item -ItemType Directory -Path "gradle\wrapper" -Force | Out-Null

# Download the wrapper jar
$url = "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar"
$output = "gradle\wrapper\gradle-wrapper.jar"

try {
    Invoke-WebRequest -Uri $url -OutFile $output -UseBasicParsing
    Write-Host "Downloaded successfully!" -ForegroundColor Green
    
    # Verify file size
    $size = (Get-Item $output).Length
    Write-Host "File size: $size bytes" -ForegroundColor Cyan
    
    if ($size -lt 50000) {
        Write-Host "Warning: File seems too small. May need to download Gradle distribution instead." -ForegroundColor Yellow
    }
}
catch {
    Write-Host "Download failed: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Alternative: Run this command manually:" -ForegroundColor Yellow
    Write-Host 'Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-8.2-bin.zip" -OutFile "$env:TEMP\gradle.zip"' -ForegroundColor Gray
}

Write-Host ""
Write-Host "Done! Now you can run: .\gradlew.bat assembleDebug" -ForegroundColor Green
