import os

# Project base path
base = r"c:\Users\tharu\new project"

# Directory structure
dirs = [
    "EMFieldVisualizer",
    "EMFieldVisualizer/app",
    "EMFieldVisualizer/app/src/main",
    "EMFieldVisualizer/app/src/main/java/com/emfield/visualizer/data",
    "EMFieldVisualizer/app/src/main/java/com/emfield/visualizer/sensors",
    "EMFieldVisualizer/app/src/main/java/com/emfield/visualizer/rendering",
    "EMFieldVisualizer/app/src/main/java/com/emfield/visualizer/ui",
    "EMFieldVisualizer/app/src/main/java/com/emfield/visualizer/utils",
    "EMFieldVisualizer/app/src/main/cpp",
    "EMFieldVisualizer/app/src/main/res/layout",
    "EMFieldVisualizer/app/src/main/res/values",
    "EMFieldVisualizer/app/src/main/res/raw",
    "EMFieldVisualizer/app/src/main/res/mipmap-mdpi",
    "EMFieldVisualizer/app/src/main/res/mipmap-hdpi",
    "EMFieldVisualizer/app/src/main/res/mipmap-xhdpi",
    "EMFieldVisualizer/app/src/main/res/mipmap-xxhdpi",
    "EMFieldVisualizer/app/src/main/res/mipmap-xxxhdpi",
    "EMFieldVisualizer/gradle/wrapper"
]

for d in dirs:
    path = os.path.join(base, d)
    os.makedirs(path, exist_ok=True)

print("✓ Directory structure created successfully!")
