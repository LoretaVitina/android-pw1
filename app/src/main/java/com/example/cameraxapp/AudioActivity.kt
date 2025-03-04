package com.example.cameraxapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import android.widget.Button
import com.google.firebase.analytics.FirebaseAnalytics
//import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class AudioActivity : AppCompatActivity() {
    private lateinit var recordButton: Button
    private lateinit var listView: ListView
    private lateinit var audioAdapter: ArrayAdapter<String>
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isRecording = false
    private lateinit var outputDirectory: File
    private val audioFiles = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)

        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // Log screen view
        logScreenEvent("AudioActivity")

        val btnRecord = findViewById<Button>(R.id.btnRecord)
        btnRecord.setOnClickListener {
            logEvent("record_button_clicked")
            startRecording()
        }

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))

        // Initialize UI elements
        recordButton = findViewById(R.id.btnRecord)
        listView = findViewById(R.id.audioListView)

        // Get the directory for audio storage
        outputDirectory = getAudioDirectory()
        loadAudioFiles()

        // Set up ListView adapter
        audioAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, audioFiles)
        listView.adapter = audioAdapter

        // Start/Stop recording on button click
        recordButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }

        // Play audio when clicking on an item in the ListView
        listView.setOnItemClickListener { _, _, position, _ ->
            val fileName = audioFiles[position]
            playAudio(File(outputDirectory, fileName))
        }

//        // Request permissions
        requestPermissions()
    }

    private fun startRecording() {
        val audioFile = File(outputDirectory, "recording_${System.currentTimeMillis()}.mp3")

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile.absolutePath)
            try {
                prepare()
                start()
                isRecording = true
                recordButton.text = "Stop Recording"
            } catch (e: IOException) {
                Toast.makeText(this@AudioActivity, "Recording failed", Toast.LENGTH_SHORT).show()
            }
        }
        logEvent("audio_saved")
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false
        recordButton.text = "Record"

        loadAudioFiles()
        audioAdapter.notifyDataSetChanged()
    }

    private fun getAudioDirectory(): File {
        val dir = File(externalCacheDir, "AudioRecordings")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun loadAudioFiles() {
        audioFiles.clear()
        outputDirectory.listFiles()?.forEach { file ->
            audioFiles.add(file.name)
        }
    }

    private fun playAudio(file: File) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(file.absolutePath)
                prepare()
                start()
            } catch (e: IOException) {
                Toast.makeText(this@AudioActivity, "Playback failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteAllRecordings() {
        outputDirectory.listFiles()?.forEach { it.delete() }
        audioFiles.clear()
        audioAdapter.notifyDataSetChanged()
        Toast.makeText(this, "All recordings deleted", Toast.LENGTH_SHORT).show()
        logEvent("audio_deleted")
    }

//    // Request audio permissions
    private fun requestPermissions() {
        val permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions, 0)
        }
    }

    // Handle toolbar menu actions
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.audio_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_image -> {
                startActivity(Intent(this, MainActivity::class.java))
                true
            }
            R.id.action_delete_audio -> {
                deleteAllRecordings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaRecorder?.release()
    }
}
