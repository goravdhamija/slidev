package com.globewaystechnologies.slidevideospy.viewmodel

import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    private val sharedPreferences = application.getSharedPreferences("settings", Context.MODE_PRIVATE)

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
