package com.example.edgeapp

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.TextureView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.opengl.GLSurfaceView
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
        // Native stub - implemented in native-lib.cpp
        @JvmStatic external fun nativeProcessFrame(input: ByteArray, width: Int, height: Int): ByteArray
    }

    private lateinit var textureView: TextureView
    private lateinit var cameraPreview: CameraPreview
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var btnToggle: Button
    private lateinit var tvFps: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val running = AtomicBoolean(false)
    private var processEnabled = true

    private var lastTime = System.currentTimeMillis()
    private var frames = 0

    private lateinit var renderer: gl.GLRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textureView = findViewById(R.id.textureView)
        glSurfaceView = findViewById(R.id.glSurfaceView)
        btnToggle = findViewById(R.id.btnToggleProcess)
        tvFps = findViewById(R.id.tvFps)

        // Setup camera when texture is ready
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {
                cameraPreview = CameraPreview(this@MainActivity, textureView)
                cameraPreview.start()
            }
            override fun onSurfaceTextureSizeChanged(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureDestroyed(surface: android.graphics.SurfaceTexture): Boolean {
                cameraPreview.stop()
                return true
            }
            override fun onSurfaceTextureUpdated(surface: android.graphics.SurfaceTexture) {}
        }

        // Setup GLSurfaceView and renderer
        glSurfaceView.setEGLContextClientVersion(2)
        renderer = gl.GLRenderer(this)
        glSurfaceView.setRenderer(renderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        btnToggle.setOnClickListener {
            processEnabled = !processEnabled
            btnToggle.text = if (processEnabled) "Process: ON" else "Process: OFF"
        }
    }

    override fun onResume() {
        super.onResume()
        running.set(true)
        startCaptureLoop()
    }

    override fun onPause() {
        running.set(false)
        super.onPause()
    }

    private fun startCaptureLoop() {
        handler.post(object : Runnable {
            override fun run() {
                if (!running.get()) return
                if (textureView.isAvailable) {
                    val bmp = textureView.bitmap ?: null
                    bmp?.let { bitmap ->
                        // convert to RGBA byte array (PNG used for simplicity in stub; later we'll use raw data)
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos)
                        val bytes = baos.toByteArray()

                        // call native stub which currently returns the same bytes
                        val processed = if (processEnabled) {
                            try {
                                nativeProcessFrame(bytes, bitmap.width, bitmap.height)
                            } catch (e: Throwable) {
                                bytes
                            }
                        } else {
                            bytes
                        }

                        // For phase 1: just update renderer with the original bitmap (we'll decode processed bytes back)
                        val decoded = android.graphics.BitmapFactory.decodeByteArray(processed, 0, processed.size)
                        decoded?.let {
                            // send to GL renderer thread
                            glSurfaceView.queueEvent {
                                renderer.updateBitmap(it)
                            }
                            glSurfaceView.requestRender()
                        }
                        // FPS calc
                        frames++
                        val now = System.currentTimeMillis()
                        if (now - lastTime >= 1000) {
                            val fps = frames * 1000 / (now - lastTime)
                            runOnUiThread { tvFps.text = "FPS: $fps" }
                            frames = 0
                            lastTime = now
                        }
                    }
                }
                handler.postDelayed(this, 100) // ~10 FPS capture loop (simple)
            }
        })
    }
}