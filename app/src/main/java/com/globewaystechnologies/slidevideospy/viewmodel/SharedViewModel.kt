package com.globewaystechnologies.slidevideospy.viewmodel

import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.globewaystechnologies.slidevideospy.services.PinkService
import com.globewaystechnologies.slidevideospy.utils.isMyServiceRunning
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File


class SharedViewModel(application: Application) : AndroidViewModel(application){

    private val sharedPreferences = application.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // This line seems problematic as `sharedViewModel` is not defined here.
    // It should likely be `this.getMediaRecorderAudioSource()` or similar,
    // and `mediaRecorderAudioSources` needs to be defined.
    // For now, I'll assume a default value or you'll adjust it.

    // TODO: Define mediaRecorderAudioSources, perhaps as a list of pairs (DisplayName, Value)
    // val mediaRecorderAudioSources = listOf("Default" to "0", "Mic" to "1", ...)

    // Initialize with the stored value or a default. The display name part (Int) is not stored directly.
    // You might need a way to map the stored string value back to the Pair<Int, String> if the Int part is crucial for display.
    // For simplicity, I'm assuming the String part is sufficient for now.
    var selectedMediaRecorderAudioSource by mutableStateOf(getMediaRecorderAudioSource() ?: "Default")
    fun getMediaRecorderAudioSource(): Pair<Int, String>? {
        val value = sharedPreferences.getString("media_recorder_audio_source_value", null)
        return value?.let { Pair(0, it) } // Replace `0` with the appropriate default Int value
    }
    fun setMediaRecorderAudioSource(sourcePair: Pair<Int, String>) {
        sharedPreferences.edit().putString("media_recorder_audio_source_value", sourcePair.second).apply()
        selectedMediaRecorderAudioSource = sourcePair.second // Update with the string value for consistency
    }
    fun getAudioSource(): String? {
        return sharedPreferences.getString("audio_source", null)
    }

    fun setAudioSource(source: String) {
        sharedPreferences.edit().putString("audio_source", source).apply()
    }

    fun getAudioBitrate(): String? {
        return sharedPreferences.getString("audio_bitrate", null)
    }

    fun setAudioBitrate(bitrate: String) {
        sharedPreferences.edit().putString("audio_bitrate", bitrate).apply()
        // Optionally, update a StateFlow if you need to observe this value
    }

    // This line seems problematic as `sharedViewModel` is not defined here.
    // It should likely be `this.getAudioBitrate()` or similar.
    // var selectedAudioBitrate by remember { mutableStateOf(getAudioBitrate() ?: audioBitrates.first()) }

    private val _text = MutableStateFlow("Initial Text")
    open val text: StateFlow<String> = _text

    private val _isServiceRunning = MutableStateFlow(isMyServiceRunning(application, PinkService::class.java))
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    fun updateText(newValue: String) {
        _text.value = newValue
    }

    fun updateServiceRunning(value: Boolean) {

        _isServiceRunning.update {
            value
        }
    }

    val publicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "MediaSync")
    val videoFiles = mutableStateListOf<File>()
    var isGrid by mutableStateOf(false)

    // Resolution and Bitrate logic

    private val _isAudioEnabled = MutableStateFlow(isAudioEnabledInternal())
    val isAudioEnabled: StateFlow<Boolean> = _isAudioEnabled.asStateFlow()

    private val _videoZoom = MutableStateFlow(getVideoZoomInternal())
    val videoZoom: StateFlow<Float> = _videoZoom.asStateFlow()

    private fun getVideoZoomInternal(): Float {
        return sharedPreferences.getFloat("video_zoom", 1.0f)
    }

    fun setVideoZoom(zoom: Float) {
        sharedPreferences.edit().putFloat("video_zoom", zoom).apply()
        _videoZoom.value = zoom
    }
    fun getVideoZoom(): Float {
        return sharedPreferences.getFloat("video_zoom", 1.0f)
    }

    private fun isAudioEnabledInternal(): Boolean {
        return sharedPreferences.getBoolean("audio_enabled", true)
    }
    fun setAudioEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("audio_enabled", enabled).apply()
        _isAudioEnabled.value = enabled
    }
    private val _resolution = MutableStateFlow(getResolution())
    val resolution: StateFlow<String> = _resolution.asStateFlow()

    private val _bitrate = MutableStateFlow(getBitrate())
    val bitrate: StateFlow<String> = _bitrate.asStateFlow()

    fun getResolution(): String {
        return sharedPreferences.getString("resolution", "720p") ?: "720p"
    }

    fun setResolution(newResolution: String) {
        sharedPreferences.edit().putString("resolution", newResolution).apply()
        _resolution.value = newResolution
    }

    fun getBitrate(): String {
        return sharedPreferences.getString("bitrate", "4 Mbps") ?: "4 Mbps"
    }

    fun setBitrate(newBitrate: String) {
        sharedPreferences.edit().putString("bitrate", newBitrate).apply()
        _bitrate.value = newBitrate
    }
}
