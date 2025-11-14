cmake_minimum_required(VERSION 3.10)
project(edgeapp-native)

set(CMAKE_CXX_STANDARD 17)

# You need to point OPENCV_ANDROID_SDK to the path where OpenCV Android SDK is unpacked.
# Example: set(OPENCV_ANDROID_SDK ${CMAKE_SOURCE_DIR}/../opencv)
if(NOT DEFINED OPENCV_ANDROID_SDK)
    message(WARNING "OPENCV_ANDROID_SDK not set. Configure it in CMakeLists or via -DOPENCV_ANDROID_SDK=path")
endif()

# If OpenCV SDK is available, include its cmake config
if(EXISTS "${OPENCV_ANDROID_SDK}/sdk/native/jni/include")
    include_directories("${OPENCV_ANDROID_SDK}/sdk/native/jni/include")
    add_library(opencv_java4 SHARED IMPORTED)
    set_target_properties(opencv_java4 PROPERTIES IMPORTED_LOCATION "${OPENCV_ANDROID_SDK}/sdk/native/libs/${ANDROID_ABI}/libopencv_java4.so")
    add_library(native-lib SHARED src/main/cpp/native-lib.cpp)
    find_library(log-lib log)
    target_link_libraries(native-lib opencv_java4 ${log-lib})
else()
    # Fallback: compile without OpenCV (will fail if you rely on OpenCV headers)
    add_library(native-lib SHARED src/main/cpp/native-lib.cpp)
    find_library(log-lib log)
    target_link_libraries(native-lib ${log-lib})
endif()