package com.example.data.remote

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException

class AudioRecordManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentOutputFile: File? = null
    private var recordingJob: Job? = null

    val isRecording: Boolean
        get() = mediaRecorder != null

    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying == true

    /**
     * Start recording real audio into a universal format (.m4a / .3gp compatible with MMS)
     */
    fun startRecording(
        onAmplitudeSample: (Float) -> Unit = {}
    ): Boolean {
        stopPlayback()
        val audioDir = File(context.cacheDir, "voice_notes").apply { mkdirs() }
        val outputFile = File(audioDir, "voice_${System.currentTimeMillis()}.m4a")
        currentOutputFile = outputFile

        return try {
            @Suppress("DEPRECATION")
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(64000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder

            // Coroutine to sample amplitude
            recordingJob = CoroutineScope(Dispatchers.Main).launch {
                while (mediaRecorder != null) {
                    delay(100)
                    try {
                        val maxAmp = mediaRecorder?.maxAmplitude ?: 0
                        val normalized = (maxAmp / 32767f).coerceIn(0.1f, 1.0f)
                        onAmplitudeSample(normalized)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e("AudioRecordManager", "Error starting recording: ${e.message}")
            mediaRecorder?.release()
            mediaRecorder = null
            false
        }
    }

    /**
     * Stop recording and return output audio file and duration
     */
    fun stopRecording(): File? {
        recordingJob?.cancel()
        recordingJob = null

        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            currentOutputFile
        } catch (e: Exception) {
            Log.e("AudioRecordManager", "Error stopping recording: ${e.message}")
            mediaRecorder?.release()
            mediaRecorder = null
            currentOutputFile
        }
    }

    fun cancelRecording() {
        recordingJob?.cancel()
        recordingJob = null
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            // ignore
        }
        mediaRecorder = null
        currentOutputFile?.delete()
        currentOutputFile = null
    }

    /**
     * Play an audio file or sample with progress updates
     */
    fun startPlayback(
        filePath: String?,
        onProgress: (Float) -> Unit,
        onCompletion: () -> Unit
    ) {
        stopPlayback()
        val player = MediaPlayer()
        mediaPlayer = player

        try {
            if (!filePath.isNullOrBlank() && File(filePath).exists()) {
                player.setDataSource(filePath)
            } else {
                // Play a brief tone if file is virtual
                onCompletion()
                return
            }

            player.prepare()
            player.start()

            CoroutineScope(Dispatchers.Main).launch {
                while (player.isPlaying) {
                    val current = player.currentPosition
                    val total = player.duration.coerceAtLeast(1)
                    onProgress(current.toFloat() / total.toFloat())
                    delay(100)
                }
                onProgress(1.0f)
                onCompletion()
            }

            player.setOnCompletionListener {
                onCompletion()
                stopPlayback()
            }
        } catch (e: Exception) {
            Log.e("AudioRecordManager", "Error playing audio: ${e.message}")
            onCompletion()
            stopPlayback()
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            // ignore
        }
        mediaPlayer = null
    }
}
