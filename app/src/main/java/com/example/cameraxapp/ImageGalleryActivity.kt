package com.example.cameraxapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import java.io.File
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class ImageGalleryActivity : AppCompatActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_gallery)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        logScreenEvent("ImageGalleryActivity")

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val btnBackToCapture = findViewById<Button>(R.id.btnBackToCapture)
        btnBackToCapture.setOnClickListener {
            logEvent("back_to_capture_clicked")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        val images = getSavedImages()
        val adapter = ImageAdapter(this, images)
        viewPager.adapter = adapter

        // Navigate back to Image Capture Activity
        btnBackToCapture.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Close gallery activity
        }
    }

    private fun getSavedImages(): List<File> {
        val directory = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name))
        }
        return directory?.listFiles()?.filter { it.extension in listOf("jpg", "png") } ?: emptyList()
    }

    private fun logEvent(eventName: String) {
        val bundle = Bundle()
        bundle.putString("event_name", eventName)
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    private fun logScreenEvent(screenName: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}
