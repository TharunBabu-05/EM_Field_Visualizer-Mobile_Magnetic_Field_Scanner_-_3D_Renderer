# EM Field Visualizer - Android

An advanced Android application that visualizes electromagnetic fields in real-time 3D using direct hardware sensor access.

## 🎯 Features

- **Direct Hardware Access**: Raw magnetometer data at 100-200 Hz
- **3D Visualization**: OpenGL ES 3.0 rendering with multiple modes:
  - Vector field arrows
  - Heat map overlay
  - Particle flow animation
- **Spatial Tracking**: 6DOF position tracking using IMU sensor fusion
- **Dual Scanning Modes**:
  - Real-time: Move phone through space to build 3D map
  - Manual: Place individual measurement points
- **Calibration System**: Figure-8 motion calibration for accurate readings
- **Data Export**: CSV, JSON, and 3D OBJ formats

## 📋 Requirements

- **Android Studio**: Arctic Fox (2020.3.1) or later
- **Android SDK**: 34
- **NDK**: 25.1 or later
- **Minimum Android**: 8.0 (API 26)
- **Target Android**: 14.0 (API 34)
- **Physical Device Required**: Must have:
  - Magnetometer (compass)
  - Accelerometer
  - Gyroscope
  - OpenGL ES 3.0 support

## 🚀 Getting Started

### 1. Open in Android Studio

```bash
File > Open > Select "EMFieldVisualizer" folder
```

### 2. Sync Gradle

Android Studio will automatically prompt to sync. Click "Sync Now".

### 3. Build the Project

```bash
Build > Make Project (Ctrl+F9)
```

### 4. Connect Physical Device

**Important**: This app requires a physical Android device. Emulators do not have magnetometer sensors.

- Enable USB Debugging on your device
- Connect via USB
- Accept the debugging authorization

### 5. Run

```bash
Run > Run 'app' (Shift+F10)
```

## 📂 Project Structure

```
EMFieldVisualizer/
├── app/
│   ├── src/main/
│   │   ├── java/com/emfield/visualizer/
│   │   │   ├── data/           # Data models and repository
│   │   │   ├── sensors/        # Sensor access and fusion (to be implemented)
│   │   │   ├── rendering/      # OpenGL rendering (to be implemented)
│   │   │   ├── ui/             # Activities and fragments
│   │   │   └── utils/          # Utilities (to be implemented)
│   │   ├── cpp/                # Native C++ code
│   │   │   ├── sensor_native.cpp      # High-speed sensor buffer
│   │   │   ├── kalman_filter.cpp      # Kalman filtering
│   │   │   └── CMakeLists.txt
│   │   └── res/
│   │       ├── layout/         # UI layouts
│   │       ├── raw/            # OpenGL shaders
│   │       └── values/         # Strings, colors, themes
│   └── build.gradle.kts
└── build.gradle.kts
```

## 🔧 How It Works

### 1. Sensor Data Collection

- Accesses `TYPE_MAGNETIC_FIELD_UNCALIBRATED` at highest sampling rate
- Raw data buffered in native C++ for performance
- Calibration removes device-specific biases

### 2. Position Tracking

- Fuses accelerometer + gyroscope data
- Extended Kalman Filter in C++ for smooth tracking
- Transforms device coordinates to world space

### 3. 3D Rendering

- OpenGL ES 3.0 for GPU-accelerated graphics
- Custom shaders for vector field rendering
- Particle system follows magnetic field gradients

### 4. Data Export

- CSV: Timestamp, Position (X,Y,Z), Field (Bx,By,Bz)
- JSON: Structured data with metadata
- OBJ: 3D model for visualization in Blender/Maya

## 🎮 Usage

### Calibration (First Time)

1. Tap "Calibrate"
2. Move your device in a figure-8 pattern
3. Continue until calibration reaches 100%
4. Calibration data is saved automatically

### Scanning

**Real-time Mode:**
1. Toggle "Real-time Mode" ON
2. Tap "Start Scanning"
3. Slowly move your phone through the space
4. Data points are collected continuously

**Manual Mode:**
1. Toggle "Real-time Mode" OFF
2. Tap "Start Scanning"
3. Position phone, tap screen to capture point
4. Move to next position and repeat

### Visualization

1. Switch to "Visualize" tab
2. Select visualization mode:
   - **Vectors**: Shows field direction as 3D arrows
   - **Heatmap**: Color-coded field strength
   - **Particles**: Animated flow lines
3. Touch to rotate camera
4. Pinch to zoom

### Export Data

1. In Visualize tab, tap "Export"
2. Choose format (CSV/JSON/OBJ)
3. Share or save to device

## 🛠️ Development Status

### ✅ Phase 1: Project Setup (Complete)
- Android project structure
- Gradle configuration with NDK
- Resource files (layouts, strings, colors)
- Data models and interfaces
- Native C++ foundation
- OpenGL shaders

### 🚧 Phase 2: Sensor Implementation (Next)
- MagnetometerManager.kt
- CalibrationManager.kt
- IMUFusion.kt
- SpatialMapper.kt

### 📋 Phase 3: Rendering (Planned)
- GLRenderer.kt
- VectorFieldRenderer.kt
- Particle system
- Camera controls

### 📋 Phase 4: UI & Export (Planned)
- Fragment implementations
- Data export manager
- Settings and preferences

## 🐛 Troubleshooting

### "No magnetometer found"
- Ensure device has magnetometer (check specifications)
- Some budget devices lack this sensor

### "OpenGL initialization failed"
- Device must support OpenGL ES 3.0
- Check: `adb shell dumpsys | grep GLES`

### Build errors
- Ensure NDK is installed: Tools > SDK Manager > SDK Tools > NDK
- Sync Gradle files
- Clean and rebuild: Build > Clean Project, then Build > Rebuild Project

## 📖 Technical Details

### Sensor Specifications
- **Sampling Rate**: 100-200 Hz (device dependent)
- **Magnetometer Range**: ±1200 μT typical
- **Position Accuracy**: ±5cm (accumulative drift)
- **Field Resolution**: 0.1 μT

### Performance Targets
- 60 FPS rendering
- < 10ms sensor latency
- 10,000+ simultaneous data points
- < 200MB memory usage

## 🤝 Contributing

This is a learning/demonstration project. Feel free to:
- Report bugs
- Suggest features
- Submit pull requests
- Use as reference for your own projects

## 📄 License

MIT License - Use freely with attribution

## 🙏 Acknowledgments

- Android Sensor Framework documentation
- OpenGL ES tutorials
- Kalman Filter implementations from academic sources

## 📬 Contact

For questions or feedback about this project, please open an issue on GitHub.

---

**Made with ❤️ for hardware-level Android development**
