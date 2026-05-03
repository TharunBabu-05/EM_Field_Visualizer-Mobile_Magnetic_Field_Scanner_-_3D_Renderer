# Add project specific ProGuard rules here.
# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep sensor and rendering classes
-keep class com.emfield.visualizer.sensors.** { *; }
-keep class com.emfield.visualizer.rendering.** { *; }
