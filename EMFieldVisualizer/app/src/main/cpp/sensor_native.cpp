#include <jni.h>
#include <android/log.h>
#include <cmath>
#include <vector>
#include <algorithm>
#include <mutex>

#define LOG_TAG "EMFieldNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Circular buffer for high-frequency sensor data
class SensorBuffer {
private:
    std::vector<float> bufferX;
    std::vector<float> bufferY;
    std::vector<float> bufferZ;
    std::vector<long> timestamps;
    size_t capacity;
    size_t writeIndex;
    size_t count;
    mutable std::mutex bufferMutex;

public:
    SensorBuffer(size_t size) : capacity(size), writeIndex(0), count(0) {
        bufferX.resize(size);
        bufferY.resize(size);
        bufferZ.resize(size);
        timestamps.resize(size);
    }

    void addSample(float x, float y, float z, long timestamp) {
        std::lock_guard<std::mutex> lock(bufferMutex);
        bufferX[writeIndex] = x;
        bufferY[writeIndex] = y;
        bufferZ[writeIndex] = z;
        timestamps[writeIndex] = timestamp;
        
        writeIndex = (writeIndex + 1) % capacity;
        if (count < capacity) count++;
    }

    size_t getCount() const { 
        std::lock_guard<std::mutex> lock(bufferMutex);
        return count; 
    }
    
    void getLatest(float& x, float& y, float& z) {
        std::lock_guard<std::mutex> lock(bufferMutex);
        if (count == 0) {
            x = y = z = 0.0f;
            return;
        }
        size_t idx = (writeIndex + capacity - 1) % capacity;
        x = bufferX[idx];
        y = bufferY[idx];
        z = bufferZ[idx];
    }
    
    void getBatch(float* output, size_t batchSize) {
        std::lock_guard<std::mutex> lock(bufferMutex);
        size_t actualBatch = std::min(batchSize, count);
        
        for (size_t i = 0; i < actualBatch; i++) {
            size_t idx = (writeIndex + capacity - actualBatch + i) % capacity;
            output[i * 3] = bufferX[idx];
            output[i * 3 + 1] = bufferY[idx];
            output[i * 3 + 2] = bufferZ[idx];
        }
    }
    
    float getAverageMagnitude() {
        std::lock_guard<std::mutex> lock(bufferMutex);
        if (count == 0) return 0.0f;
        
        float sum = 0.0f;
        for (size_t i = 0; i < count; i++) {
            float mag = sqrt(bufferX[i] * bufferX[i] + bufferY[i] * bufferY[i] + bufferZ[i] * bufferZ[i]);
            sum += mag;
        }
        return sum / count;
    }
};

// Global sensor buffer
static SensorBuffer* g_sensorBuffer = nullptr;

extern "C" {

JNIEXPORT void JNICALL
Java_com_emfield_visualizer_sensors_MagnetometerManager_nativeInit(
        JNIEnv* env,
        jobject /* this */,
        jint bufferSize) {
    if (g_sensorBuffer != nullptr) {
        delete g_sensorBuffer;
    }
    g_sensorBuffer = new SensorBuffer(bufferSize);
    LOGI("Native sensor buffer initialized with size %d", bufferSize);
}

JNIEXPORT void JNICALL
Java_com_emfield_visualizer_sensors_MagnetometerManager_nativeAddSample(
        JNIEnv* env,
        jobject /* this */,
        jfloat x,
        jfloat y,
        jfloat z,
        jlong timestamp) {
    if (g_sensorBuffer != nullptr) {
        g_sensorBuffer->addSample(x, y, z, timestamp);
    }
}

JNIEXPORT jfloatArray JNICALL
Java_com_emfield_visualizer_sensors_MagnetometerManager_nativeGetLatest(
        JNIEnv* env,
        jobject /* this */) {
    float x, y, z;
    if (g_sensorBuffer != nullptr) {
        g_sensorBuffer->getLatest(x, y, z);
    } else {
        x = y = z = 0.0f;
    }
    
    jfloatArray result = env->NewFloatArray(3);
    if (result == nullptr) return nullptr;
    
    jfloat values[3] = {x, y, z};
    env->SetFloatArrayRegion(result, 0, 3, values);
    
    return result;
}

JNIEXPORT jfloatArray JNICALL
Java_com_emfield_visualizer_sensors_MagnetometerManager_nativeGetBatch(
        JNIEnv* env,
        jobject /* this */,
        jint batchSize) {
    if (g_sensorBuffer == nullptr) {
        return nullptr;
    }
    
    jfloatArray result = env->NewFloatArray(batchSize * 3);
    if (result == nullptr) return nullptr;
    
    std::vector<float> batchData(batchSize * 3);
    g_sensorBuffer->getBatch(batchData.data(), batchSize);
    
    env->SetFloatArrayRegion(result, 0, batchSize * 3, batchData.data());
    return result;
}

JNIEXPORT jfloat JNICALL
Java_com_emfield_visualizer_sensors_MagnetometerManager_nativeGetAverageMagnitude(
        JNIEnv* env,
        jobject /* this */) {
    if (g_sensorBuffer == nullptr) {
        return 0.0f;
    }
    return g_sensorBuffer->getAverageMagnitude();
}

JNIEXPORT void JNICALL
Java_com_emfield_visualizer_sensors_MagnetometerManager_nativeCleanup(
        JNIEnv* env,
        jobject /* this */) {
    if (g_sensorBuffer != nullptr) {
        delete g_sensorBuffer;
        g_sensorBuffer = nullptr;
        LOGI("Native sensor buffer cleaned up");
    }
}

} // extern "C"
