// (Replace or update the previous MainActivity; only showing modified capture part and native signature)
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
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity() {

    companion object {
        init { System.loadLibrary("native-lib") }
        @JvmStatic external fun nativeProcessFrameRGBA(input: ByteArray, width: Int, height: Int): ByteArray
    }

    // ... (the rest is similar) ...

    private fun startCaptureLoop() {
        handler.post(object : Runnable {
            override fun run() {
                if (!running.get()) return
                if (textureView.isAvailable) {
                    val bmp = textureView.bitmap?.copy(Bitmap.Config.ARGB_8888, false)
                    bmp?.let { bitmap ->
                        val width = bitmap.width
                        val height = bitmap.height
                        val buffer = ByteBuffer.allocate(bitmap.byteCount)
                        bitmap.copyPixelsToBuffer(buffer)
                        val bytes = buffer.array() // ARGB_8888 raw bytes

                        val processed = if (processEnabled) {
                            try {
                                nativeProcessFrameRGBA(bytes, width, height)
                            } catch (e: Throwable) {
                                bytes
                            }
                        } else bytes

                        // decode processed bytes back to Bitmap (we assume same ARGB_8888 layout)
                        val outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val outBuffer = ByteBuffer.wrap(processed)
                        outBitmap.copyPixelsFromBuffer(outBuffer)

                        glSurfaceView.queueEvent {
                            renderer.updateBitmap(outBitmap)
                        }
                        glSurfaceView.requestRender()
                        // fps calc omitted here for brevity (same as before)
                    }
                }
                handler.postDelayed(this, 100)
            }
        })
    }
}