cmake_minimum_required(VERSION 3.10)
project(edgeapp-native LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Default location for OpenCV Android SDK relative to project root.
if(NOT DEFINED OPENCV_ANDROID_SDK)
    set(OPENCV_ANDROID_SDK "${CMAKE_SOURCE_DIR}/../opencv/opencv-android-sdk")
endif()

message(STATUS "Using OpenCV SDK at: ${OPENCV_ANDROID_SDK}")

# Try to detect NDK path if not provided by Gradle (Gradle usually defines ANDROID_NDK)
if(NOT DEFINED ANDROID_NDK)
    if(DEFINED ENV{ANDROID_NDK_HOME})
        set(ANDROID_NDK $ENV{ANDROID_NDK_HOME})
    elseif(DEFINED ENV{ANDROID_NDK_ROOT})
        set(ANDROID_NDK $ENV{ANDROID_NDK_ROOT})
    elseif(DEFINED ENV{NDK_HOME})
        set(ANDROID_NDK $ENV{NDK_HOME})
    endif()
endif()

message(STATUS "ANDROID_NDK (initial): ${ANDROID_NDK}")
message(STATUS "ANDROID_ABI: ${ANDROID_ABI}")
message(STATUS "ANDROID_NATIVE_API_LEVEL: ${ANDROID_NATIVE_API_LEVEL}")

# Add NDK sysroot include explicitly if found
if(DEFINED ANDROID_NDK AND ANDROID_NDK)
    set(NDK_SYSROOT_INCLUDE "${ANDROID_NDK}/sysroot/usr/include")
    if(EXISTS "${NDK_SYSROOT_INCLUDE}")
        message(STATUS "Adding NDK sysroot include: ${NDK_SYSROOT_INCLUDE}")
        include_directories("${NDK_SYSROOT_INCLUDE}")
    else()
        # Alternate path for recent NDKs
        get_filename_component(HOST_OS ${CMAKE_HOST_SYSTEM_NAME} NAME)
        set(NDK_ALT_INCLUDE "${ANDROID_NDK}/toolchains/llvm/prebuilt/${CMAKE_HOST_SYSTEM_NAME}-${CMAKE_HOST_SYSTEM_PROCESSOR}/sysroot/usr/include")
        if(EXISTS "${NDK_ALT_INCLUDE}")
            message(STATUS "Adding alternate NDK include: ${NDK_ALT_INCLUDE}")
            include_directories("${NDK_ALT_INCLUDE}")
        else()
            message(WARNING "NDK include path not found automatically; ensure ndk is installed and Gradle provides ANDROID_NDK.")
        endif()
    endif()
else()
    message(WARNING "ANDROID_NDK is not defined. Ensure NDK is installed and ndkVersion in build.gradle matches an installed NDK.")
endif()

# OpenCV include
set(OPENCV_INCLUDE_DIR "${OPENCV_ANDROID_SDK}/sdk/native/jni/include")
if(EXISTS "${OPENCV_INCLUDE_DIR}")
    message(STATUS "Adding OpenCV include: ${OPENCV_INCLUDE_DIR}")
    include_directories(${OPENCV_INCLUDE_DIR})
else()
    message(WARNING "OpenCV include dir not found at ${OPENCV_INCLUDE_DIR}")
endif()

# Import OpenCV prebuilt lib for the ABI (if present)
if(EXISTS "${OPENCV_ANDROID_SDK}/sdk/native/libs/${ANDROID_ABI}/libopencv_java4.so")
    add_library(opencv_java4 SHARED IMPORTED)
    set_target_properties(opencv_java4 PROPERTIES IMPORTED_LOCATION "${OPENCV_ANDROID_SDK}/sdk/native/libs/${ANDROID_ABI}/libopencv_java4.so")
    message(STATUS "Found OpenCV prebuilt for ABI ${ANDROID_ABI}")
else()
    message(WARNING "OpenCV prebuilt lib not found for ABI ${ANDROID_ABI}")
endif()

# Source file(s)
set(SRC_FILES src/main/cpp/native-lib.cpp)

# Add library
add_library(native-lib SHARED ${SRC_FILES})

# Ensure compile features and include dirs are properly set
target_include_directories(native-lib PRIVATE ${OPENCV_INCLUDE_DIR} ${NDK_SYSROOT_INCLUDE})
target_compile_features(native-lib PRIVATE cxx_std_17)

find_library(log-lib log)

# Link with OpenCV if available
if(TARGET opencv_java4)
    target_link_libraries(native-lib PRIVATE opencv_java4 ${log-lib})
else()
    target_link_libraries(native-lib PRIVATE ${log-lib})
endif()

# Helpful verbose output for debugging (you can remove when fixed)
set(CMAKE_VERBOSE_MAKEFILE ON)