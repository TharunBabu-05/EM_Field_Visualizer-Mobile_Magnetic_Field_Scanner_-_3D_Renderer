# EM Field Visualizer - Android Project Setup Script

# Create directory structure
$dirs = @(
    "EMFieldVisualizer",
    "EMFieldVisualizer\app",
    "EMFieldVisualizer\app\src\main",
    "EMFieldVisualizer\app\src\main\java\com\emfield\visualizer\data",
    "EMFieldVisualizer\app\src\main\java\com\emfield\visualizer\sensors",
    "EMFieldVisualizer\app\src\main\java\com\emfield\visualizer\rendering",
    "EMFieldVisualizer\app\src\main\java\com\emfield\visualizer\ui",
    "EMFieldVisualizer\app\src\main\java\com\emfield\visualizer\utils",
    "EMFieldVisualizer\app\src\main\cpp",
    "EMFieldVisualizer\app\src\main\res\layout",
    "EMFieldVisualizer\app\src\main\res\values",
    "EMFieldVisualizer\app\src\main\res\raw",
    "EMFieldVisualizer\app\src\main\res\mipmap-mdpi",
    "EMFieldVisualizer\app\src\main\res\mipmap-hdpi",
    "EMFieldVisualizer\app\src\main\res\mipmap-xhdpi",
    "EMFieldVisualizer\app\src\main\res\mipmap-xxhdpi",
    "EMFieldVisualizer\app\src\main\res\mipmap-xxxhdpi",
    "EMFieldVisualizer\gradle\wrapper"
)

foreach ($dir in $dirs) {
    New-Item -ItemType Directory -Path $dir -Force | Out-Null
}

Write-Host "✓ Directory structure created successfully!" -ForegroundColor Green
