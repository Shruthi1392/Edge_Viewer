```markdown
# EdgeApp — Android OpenCV C++ Edge Viewer (Minimal)

Implementation summary
- Android app captures camera frames using a TextureView (Camera2).
- Frames are copied as ARGB_8888 pixel buffers and passed to native C++ via JNI.
- Native C++ uses OpenCV (C++) to convert to grayscale, blur and run Canny edge detection; it returns processed RGBA bytes.
- GLSurfaceView / OpenGL ES 2.0 renderer uploads the processed bitmap as a texture and draws a textured quad to display the result.
- A small TypeScript web viewer displays a static processed frame (base64 image) and shows simple frame stats.

Key implementation files
- app/src/main/java/com/example/edgeapp/MainActivity.kt — camera capture loop, JNI calls, UI controls.
- app/src/main/java/com/example/edgeapp/CameraPreview.kt — Camera2 preview to TextureView.
- app/src/main/java/com/example/edgeapp/gl/GLRenderer.kt — OpenGL ES 2.0 renderer, texture upload and draw.
- app/src/main/cpp/native-lib.cpp — native processing (OpenCV C++ / JNI).
- app/CMakeLists.txt — native build configuration (links libopencv_java4.so from the OpenCV Android SDK).
- app/build.gradle — module build settings and passing OPENCV_ANDROID_SDK to CMake.
- web/src/* — TypeScript + HTML sample viewer (main.ts, index.html, styles.css).

Prerequisites
- Android Studio (recommended) or command-line Gradle.
- Android SDK with API level 33 (compileSdkVersion 33).
- Android NDK installed (match ndkVersion in app/build.gradle). Either install via SDK Manager or set ndk.dir in local.properties.
- OpenCV Android SDK 4.12 downloaded and placed at:
  <project-root>/opencv/opencv-android-sdk
  The expected layout:
  - opencv/opencv-android-sdk/sdk/native/jni/include
  - opencv/opencv-android-sdk/sdk/native/libs/<ABI>/libopencv_java4.so

Configure (if needed)
- If Android Studio does not auto-detect the NDK, add or edit local.properties at the project root:
  sdk.dir=/full/path/to/Android/sdk
  ndk.dir=/full/path/to/Android/sdk/ndk/<version>
- Ensure the OpenCV SDK folder exists at the path above. If you placed OpenCV somewhere else, update the CMake argument in app/build.gradle:
  arguments "-DOPENCV_ANDROID_SDK=/full/path/to/opencv-android-sdk"

Commands — Android build & run (from project root)
- Clean and build:
  ./gradlew clean assembleDebug
- Install and run on a connected device (debug):
  ./gradlew installDebug
- If you prefer Android Studio:
  1. Open the project.
  2. Sync Gradle / allow it to download dependencies.
  3. Run on a connected device (grant CAMERA permission when prompted).

Notes for native build
- Gradle calls CMake and supplies the Android toolchain. Do not run CMake manually outside Gradle for normal builds.
- If you see errors about missing jni.h or android/log.h:
  - Confirm NDK is installed and ndk.dir in local.properties points to the correct NDK path, or set ndkVersion in app/build.gradle to an installed NDK.
  - Clean and rebuild: ./gradlew clean assembleDebug
  - The project CMakeLists attempts to include the NDK sysroot include path; verify the Gradle/CMake configuration output in the Build window for messages about the NDK include.

Commands — Web viewer
- Move to web folder, install dependencies, compile TypeScript:
  cd web
  npm install
  npm run build
- Serve the web folder (example with http-server):
  npx http-server ./web -c-1 -p 8080
- Open http://localhost:8080/src/index.html in a browser. Replace the sample base64 image in web/src/main.ts with a real processed image exported from the Android device if desired.

Getting a sample processed frame from device
- While the app runs on-device, capture a screenshot or add a small helper in MainActivity to save the processed bitmap to external storage (not included here). Convert the saved PNG to base64 and replace the sampleBase64 string in web/src/main.ts.
