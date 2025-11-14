package com.example.edgeapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.TextureView
import java.util.*

class CameraPreview(private val context: Context, private val textureView: TextureView) {
    private var cameraDevice: CameraDevice? = null
    private var session: CameraCaptureSession? = null
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var backgroundHandler: Handler
    private var backgroundThread: HandlerThread

    init {
        backgroundThread = HandlerThread("CameraBackground")
        backgroundThread.start()
        backgroundHandler = Handler(backgroundThread.looper)
    }

    @SuppressLint("MissingPermission")
    fun start() {
        // Find a camera (pick the first back-facing)
        val camId = cameraManager.cameraIdList.firstOrNull { id ->
            val cs = cameraManager.getCameraCharacteristics(id)
            val facing = cs.get(CameraCharacteristics.LENS_FACING)
            facing != CameraCharacteristics.LENS_FACING_FRONT
        } ?: cameraManager.cameraIdList.first()

        cameraManager.openCamera(camId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                startPreviewSession()
            }
            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
                cameraDevice = null
            }
            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
                cameraDevice = null
            }
        }, backgroundHandler)
    }

    private fun startPreviewSession() {
        val tex = textureView.surfaceTexture ?: return
        // Choose preview size (texture's size)
        val width = textureView.width.takeIf { it>0 } ?: 640
        val height = textureView.height.takeIf { it>0 } ?: 480
        tex.setDefaultBufferSize(width, height)
        val surface = Surface(tex)
        try {
            val request = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            request?.addTarget(surface)
            cameraDevice?.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    this@CameraPreview.session = session
                    request?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                    session.setRepeatingRequest(request!!.build(), null, backgroundHandler)
                }
                override fun onConfigureFailed(session: CameraCaptureSession) {}
            }, backgroundHandler)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            session?.close()
            cameraDevice?.close()
            backgroundThread.quitSafely()
        } catch (e: Exception) { }
    }
}