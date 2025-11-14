#include <jni.h>
#include <vector>
#include <opencv2/imgproc.hpp>
#include <opencv2/core.hpp>

using namespace cv;

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_edgeapp_MainActivity_nativeProcessFrameRGBA(JNIEnv *env, jclass /*clazz*/,
                                                             jbyteArray input, jint width, jint height) {
    if (input == nullptr) return nullptr;
    jsize len = env->GetArrayLength(input);
    std::vector<uchar> inBytes(len);
    env->GetByteArrayRegion(input, 0, len, reinterpret_cast<jbyte*>(inBytes.data()));

    // ARGB_8888 in Android: each pixel is 4 bytes in order A,R,G,B for copyPixelsToBuffer/copyPixelsFromBuffer
    // We'll convert to BGRA for OpenCV processing (or to grayscale)
    Mat img(height, width, CV_8UC4, inBytes.data());
    // Convert from ARGB to BGRA: shift channels (A R G B) -> (B G R A)
    Mat bgra;
    cvtColor(img, bgra, COLOR_RGBA2BGRA); // treat input as RGBA-like ordering

    Mat gray;
    cvtColor(bgra, gray, COLOR_BGR2GRAY);
    Mat edges;
    GaussianBlur(gray, gray, Size(5,5), 1.5);
    Canny(gray, edges, 80, 160);

    // Create 4-channel output (BGRA) with edges in all color channels and full alpha
    Mat out;
    Mat channels[4];
    channels[0] = edges; // B
    channels[1] = edges; // G
    channels[2] = edges; // R
    channels[3] = Mat::ones(edges.size(), CV_8UC1) * 255; // A
    merge(channels, 4, out);

    // Convert back to ARGB ordering expected by Android (A,R,G,B)
    Mat outRGBA;
    cvtColor(out, outRGBA, COLOR_BGRA2RGBA);

    // Copy data to jbyteArray
    size_t outSize = outRGBA.total() * outRGBA.elemSize();
    jbyteArray outArr = env->NewByteArray((jsize)outSize);
    env->SetByteArrayRegion(outArr, 0, (jsize)outSize, reinterpret_cast<const jbyte*>(outRGBA.data));
    return outArr;
}