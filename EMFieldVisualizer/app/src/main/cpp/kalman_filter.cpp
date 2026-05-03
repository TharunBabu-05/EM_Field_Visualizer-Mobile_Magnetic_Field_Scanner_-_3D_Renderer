#include <jni.h>
#include <cmath>
#include <mutex>
#include <android/log.h>

#define LOG_TAG "KalmanFilter"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Extended Kalman Filter for 6DOF IMU sensor fusion
struct KalmanState {
    float pos[3];        // Position estimate (x, y, z)
    float vel[3];        // Velocity estimate (vx, vy, vz)
    float orient[4];     // Orientation quaternion (w, x, y, z)
    float P[16];         // 4x4 covariance matrix for position/velocity
    float Q_pos;         // Process noise for position
    float Q_vel;         // Process noise for velocity
    float R_pos;         // Measurement noise for position
    float R_orient;      // Measurement noise for orientation
};

static KalmanState g_state = {
    {0, 0, 0},           // pos
    {0, 0, 0},           // vel
    {1, 0, 0, 0},       // orient (identity quaternion)
    {1, 0, 0, 0,        // P (identity matrix)
     0, 1, 0, 0,
     0, 0, 1, 0,
     0, 0, 0, 1},
    0.001f,              // Q_pos
    0.01f,               // Q_vel
    0.1f,                // R_pos
    0.05f                // R_orient
};

static std::mutex g_stateMutex;

void quaternionMultiply(float q1[4], float q2[4], float result[4]) {
    result[0] = q1[0] * q2[0] - q1[1] * q2[1] - q1[2] * q2[2] - q1[3] * q2[3];
    result[1] = q1[0] * q2[1] + q1[1] * q2[0] + q1[2] * q2[3] - q1[3] * q2[2];
    result[2] = q1[0] * q2[2] - q1[1] * q2[3] + q1[2] * q2[0] + q1[3] * q2[1];
    result[3] = q1[0] * q2[3] + q1[1] * q2[2] - q1[2] * q2[1] + q1[3] * q2[0];
}

void quaternionNormalize(float q[4]) {
    float norm = sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
    if (norm > 0.001f) {
        q[0] /= norm;
        q[1] /= norm;
        q[2] /= norm;
        q[3] /= norm;
    }
}

void matrixMultiply(float A[16], float B[16], float C[16]) {
    for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
            C[i * 4 + j] = 0;
            for (int k = 0; k < 4; k++) {
                C[i * 4 + j] += A[i * 4 + k] * B[k * 4 + j];
            }
        }
    }
}

void kalmanPredict(KalmanState& state, float dt, float accel[3], float gyro[3]) {
    std::lock_guard<std::mutex> lock(g_stateMutex);
    
    // Predict position: pos = pos + vel * dt + 0.5 * accel * dt^2
    for (int i = 0; i < 3; i++) {
        state.pos[i] += state.vel[i] * dt + 0.5f * accel[i] * dt * dt;
        state.vel[i] += accel[i] * dt;
    }
    
    // Update orientation with gyroscope data
    float angle = sqrt(gyro[0] * gyro[0] + gyro[1] * gyro[1] + gyro[2] * gyro[2]) * dt;
    if (angle > 0.001f) {
        float axis[3] = {gyro[0] / angle, gyro[1] / angle, gyro[2] / angle};
        float halfAngle = angle * 0.5f;
        float sinHalf = sin(halfAngle);
        float cosHalf = cos(halfAngle);
        
        float deltaQ[4] = {cosHalf, axis[0] * sinHalf, axis[1] * sinHalf, axis[2] * sinHalf};
        quaternionMultiply(state.orient, deltaQ, state.orient);
        quaternionNormalize(state.orient);
    }
    
    // Predict covariance (simplified)
    for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
            state.P[i * 4 + j] += dt * (state.Q_pos + state.Q_vel);
        }
    }
}

void kalmanUpdate(KalmanState& state, float measuredPos[3], float measuredOrient[4]) {
    std::lock_guard<std::mutex> lock(g_stateMutex);
    
    // Position update
    for (int i = 0; i < 3; i++) {
        float innovation = measuredPos[i] - state.pos[i];
        float S = state.P[i * 4 + i] + state.R_pos;
        
        if (S > 0.001f) {
            float K = state.P[i * 4 + i] / S;
            state.pos[i] += K * innovation;
            state.vel[i] += K * innovation * 0.1f; // Small velocity correction
            state.P[i * 4 + i] *= (1 - K);
        }
    }
    
    // Orientation update
    float orientInnovation[4];
    float measuredConj[4] = {measuredOrient[0], -measuredOrient[1], -measuredOrient[2], -measuredOrient[3]};
    quaternionMultiply(measuredConj, state.orient, orientInnovation);
    
    // Apply orientation correction with gain
    float orientGain = 0.1f;
    for (int i = 0; i < 4; i++) {
        state.orient[i] += orientGain * orientInnovation[i];
    }
    quaternionNormalize(state.orient);
}

extern "C" {

JNIEXPORT jfloatArray JNICALL
Java_com_emfield_visualizer_sensors_IMUFusion_nativeKalmanFilter(
        JNIEnv* env,
        jobject /* this */,
        jfloat accelX,
        jfloat accelY,
        jfloat accelZ,
        jfloat gyroX,
        jfloat gyroY,
        jfloat gyroZ,
        jfloat dt) {
    
    float accel[3] = {accelX, accelY, accelZ};
    float gyro[3] = {gyroX, gyroY, gyroZ};
    
    // Predict step
    kalmanPredict(g_state, dt, accel, gyro);
    
    // Return filtered position and orientation
    jfloatArray result = env->NewFloatArray(7); // 3 pos + 4 orient
    if (result == nullptr) return nullptr;
    
    jfloat values[7];
    for (int i = 0; i < 3; i++) {
        values[i] = g_state.pos[i];
    }
    for (int i = 0; i < 4; i++) {
        values[3 + i] = g_state.orient[i];
    }
    
    env->SetFloatArrayRegion(result, 0, 7, values);
    return result;
}

JNIEXPORT jfloatArray JNICALL
Java_com_emfield_visualizer_sensors_IMUFusion_nativeGetPosition(
        JNIEnv* env,
        jobject /* this */) {
    std::lock_guard<std::mutex> lock(g_stateMutex);
    
    jfloatArray result = env->NewFloatArray(3);
    if (result == nullptr) return nullptr;
    
    env->SetFloatArrayRegion(result, 0, 3, g_state.pos);
    return result;
}

JNIEXPORT jfloatArray JNICALL
Java_com_emfield_visualizer_sensors_IMUFusion_nativeGetOrientation(
        JNIEnv* env,
        jobject /* this */) {
    std::lock_guard<std::mutex> lock(g_stateMutex);
    
    jfloatArray result = env->NewFloatArray(4);
    if (result == nullptr) return nullptr;
    
    env->SetFloatArrayRegion(result, 0, 4, g_state.orient);
    return result;
}

JNIEXPORT void JNICALL
Java_com_emfield_visualizer_sensors_IMUFusion_nativeResetFilter(
        JNIEnv* env,
        jobject /* this */) {
    std::lock_guard<std::mutex> lock(g_stateMutex);
    
    // Reset state
    for (int i = 0; i < 3; i++) {
        g_state.pos[i] = 0;
        g_state.vel[i] = 0;
    }
    g_state.orient[0] = 1;
    g_state.orient[1] = 0;
    g_state.orient[2] = 0;
    g_state.orient[3] = 0;
    
    // Reset covariance
    for (int i = 0; i < 16; i++) {
        g_state.P[i] = 0;
    }
    for (int i = 0; i < 4; i++) {
        g_state.P[i * 4 + i] = 1;
    }
    
    LOGI("Kalman filter reset");
}

} // extern "C"
