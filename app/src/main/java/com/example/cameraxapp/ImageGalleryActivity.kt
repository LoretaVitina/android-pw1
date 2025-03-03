package com.example.cameraxapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import java.io.File

class ImageGalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_gallery)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val images = getSavedImages()
        val adapter = ImageAdapter(this, images)
        viewPager.adapter = adapter
    }

    private fun getSavedImages(): List<File> {
        val directory = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name))
        }
        return directory?.listFiles()?.filter { it.extension in listOf("jpg", "png") } ?: emptyList()
    }
}
