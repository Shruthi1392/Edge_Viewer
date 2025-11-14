#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "native-lib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Minimal test function to verify includes and build
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_edgeapp_MainActivity_stringFromJNI(JNIEnv* env, jobject /* this */) {
    LOGI("stringFromJNI invoked"); // logs to logcat
    std::string hello = "Hello from native C++ (NDK)";
    return env->NewStringUTF(hello.c_str());
}