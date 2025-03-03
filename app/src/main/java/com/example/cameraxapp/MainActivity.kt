package com.example.cameraxapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var outputDirectory: File
    private lateinit var imageCapture: ImageCapture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewFinder = findViewById<PreviewView>(R.id.viewFinder)
        val btnTakePicture = findViewById<Button>(R.id.image_capture_button)
        val btnAudio = findViewById<Button>(R.id.btn_audio)
        val btnShowImages = findViewById<Button>(R.id.btn_show_images)
        val btnDeleteImages = findViewById<Button>(R.id.btn_delete_images)

        outputDirectory = getOutputDirectory()

        startCamera(viewFinder)

        btnTakePicture.setOnClickListener { takePhoto() }
        btnAudio.setOnClickListener {
            val intent = Intent(this, AudioActivity::class.java)
            startActivity(intent)
        }
        btnShowImages.setOnClickListener {
            val intent = Intent(this, ImageGalleryActivity::class.java)
            startActivity(intent)
        }
        btnDeleteImages.setOnClickListener { deleteAllImages() }
    }

    private fun startCamera(viewFinder: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val fileName = "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        val file = File(outputDirectory, fileName)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    println("Saved: ${file.absolutePath}")
                }
                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                }
            })
    }

    private fun deleteAllImages() {
        outputDirectory.listFiles()?.forEach { it.delete() }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return mediaDir ?: filesDir
    }
}
