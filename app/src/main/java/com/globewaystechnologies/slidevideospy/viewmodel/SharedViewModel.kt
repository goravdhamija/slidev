package com.globewaystechnologies.slidevideospy.viewmodel

import android.app.Application
import android.content.Context
import android.os.Environment
import android.util.Size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.globewaystechnologies.slidevideospy.data.PreferenceKeys
import com.globewaystechnologies.slidevideospy.data.SettingsRepository
import com.globewaystechnologies.slidevideospy.dataStore
import com.globewaystechnologies.slidevideospy.screens.VideoQuality
import com.globewaystechnologies.slidevideospy.utils.CameraUtils.getAvailableVideoQualities
import com.globewaystechnologies.slidevideospy.utils.isMyServiceRunning
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import com.globewaystechnologies.slidevideospy.services.PinkService


const val PREFERENCES_NAME = "omni_preferences"

class SharedViewModel(application: Application) : AndroidViewModel(application){

    private val sharedPreferences = application.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val settingsRepository = SettingsRepository(getApplication<Application>().dataStore)

    private val _isPaidUser = MutableStateFlow(sharedPreferences.getBoolean("is_paid_user", false))
    val isPaidUser: StateFlow<Boolean> = _isPaidUser.asStateFlow()

    private val _text = MutableStateFlow("Initial Text")
    open val text: StateFlow<String> = _text

    private val _videoSlotDurationMillis = MutableStateFlow(0L) // default value
    val videoSlotDurationMillis: StateFlow<Long> = _videoSlotDurationMillis

    private val _isServiceRunning = MutableStateFlow(isMyServiceRunning(application, PinkService::class.java))
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    val publicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "MediaSync")
    val videoFiles = mutableStateListOf<File>()
    var isGrid by mutableStateOf(false)

    private val _isAudioEnabled = MutableStateFlow(true)
    val isAudioEnabled: StateFlow<Boolean> = _isAudioEnabled.asStateFlow()

    private val _videoZoom = MutableStateFlow(getVideoZoomInternal())
    val videoZoom: StateFlow<Float> = _videoZoom.asStateFlow()

    private val _resolution = MutableStateFlow(getResolution())
    val resolution: StateFlow<String> = _resolution.asStateFlow()

    private val _bitrate = MutableStateFlow(getBitrate())
    val bitrate: StateFlow<String> = _bitrate.asStateFlow()

    var selectedStorageLocation by mutableStateOf(getStorageLocation())

    private val _selectedMediaRecorderAudioSource = MutableStateFlow("Default")
    var selectedMediaRecorderAudioSource: StateFlow<String> = _selectedMediaRecorderAudioSource.asStateFlow()

//    private val _audioSources = MutableStateFlow<AudioSource?>(null) // nullable initially
//    val audioSources: StateFlow<AudioSource?> = _audioSources.asStateFlow()


    private val _selectedAudioBitrate = MutableStateFlow("96kbps")
    var selectedAudioBitrate: StateFlow<String> = _selectedAudioBitrate.asStateFlow()

    private val _selectedVideoQuality = MutableStateFlow<VideoQuality?>(null)
    val selectedVideoQuality: StateFlow<VideoQuality?> = _selectedVideoQuality.asStateFlow()

    val availableVideoQualities: List<Size> = getAvailableVideoQualities(application)
    val videoQualities: List<VideoQuality>

    init {
        videoQualities = run {
            val predefinedQualities = listOf(
                VideoQuality("SD 480p", "640x480", "sd"),
                VideoQuality("SD 576p", "720x576", "sd"),
                VideoQuality("HD 720p", "1280x720", "hd"),
                VideoQuality("HD 1080p", "1920x1080", "hd"),
                VideoQuality("QHD 1440p", "2560x1440", "qhd"),
                VideoQuality("UHD 4K", "3840x2160", "uhd"),
                VideoQuality("UHD 5K", "5120x2880", "uhd"),
                VideoQuality("UHD 8K", "7680x4320", "uhd"),
                VideoQuality("LD 360p", "640x360", "ld"),
                VideoQuality("VLD 240p", "426x240", "vld")
            )

            predefinedQualities.filter { predefinedQuality ->
                val resolutionParts = predefinedQuality.resolution.split("x")
                if (resolutionParts.size == 2) {
                    try {
                        val width = resolutionParts[0].toInt()
                        val height = resolutionParts[1].toInt()
                        availableVideoQualities.any { availableSize ->
                            availableSize.width == width && availableSize.height == height
                        }
                    } catch (e: NumberFormatException) {
                        false // Invalid resolution format
                    }
                } else {
                    false // Invalid resolution format
                }
            }.ifEmpty {
                availableVideoQualities.minByOrNull { it.width * it.height }
                    ?.let { listOf(VideoQuality("Lowest Available", "${it.width}x${it.height}", "custom")) }
                    ?: listOf(VideoQuality("Default (No Cam)", "640x480", "sd"))
            }


        }



        viewModelScope.launch {
            settingsRepository.readSomeSetting(PreferenceKeys.SELECTED_SLOT_DURATION_MILLIS)
                .collect { value ->
                    _videoSlotDurationMillis.value = value.toLongOrNull() ?: 0L
                }
        }
        viewModelScope.launch {
            settingsRepository.readSomeSetting(PreferenceKeys.SELECTED_IS_AUDIO_ENABLED)
                .collect { value ->
                    _isAudioEnabled.value = value.toBooleanStrictOrNull() ?: true
                }
        }
        viewModelScope.launch {
            settingsRepository.readSomeSetting(PreferenceKeys.SELECTED_MEDIA_RECORDER_AUDIO_SOURCE)
                .collect { value ->
                    _selectedMediaRecorderAudioSource.value = value.toString().ifEmpty { "Default" }
                }
        }
        viewModelScope.launch {
            settingsRepository.readSomeSetting(PreferenceKeys.SELECTED_AUDIO_BITRATE)
                .collect { value ->
                    _selectedAudioBitrate.value = value.toString().ifEmpty { "96kbps" }
                }

        }



        viewModelScope.launch {
            settingsRepository.readSomeSetting(PreferenceKeys.SELECTED_VIDEO_RESOLUTION)
                .collect { value ->
                    val quality = videoQualities.find { it.resolution == value } ?: videoQualities.firstOrNull()
                    _selectedVideoQuality.value = quality
                  //  _selectedVideoResolution.value = value.toString().ifEmpty { "720p" } // Kept for reference if string is needed
                }

        }


    }

    // Function to set the video slot duration
    suspend fun setVideoSlotDurationMillis(durationMillis: Long) {
       // sharedPreferences.edit().putLong("video_slot_duration_millis", durationMillis).apply()
        _videoSlotDurationMillis.value = durationMillis

        settingsRepository.saveSomeSetting(
            key = PreferenceKeys.SELECTED_SLOT_DURATION_MILLIS,
            value = durationMillis.toString()
        )


    }
    suspend fun getVideoSlotDurationMillis(): Long {
        val flow = settingsRepository.readSomeSetting(PreferenceKeys.SELECTED_SLOT_DURATION_MILLIS)
        val value = flow.firstOrNull() ?: "0"
        return value.toLongOrNull() ?: 0L
    }


    suspend fun setAudioEnabled(value: Boolean) {
        _isAudioEnabled.value = value
        settingsRepository.saveSomeSetting(
            key = PreferenceKeys.SELECTED_IS_AUDIO_ENABLED,
            value = value.toString()
        )
    }

    suspend fun getAudioEnabled(): Boolean {
        val flow = settingsRepository.readSomeSetting(PreferenceKeys.SELECTED_IS_AUDIO_ENABLED)
        val value = flow.firstOrNull() ?: true
        return value.toString().toBooleanStrictOrNull() ?: true
    }

    suspend fun setSelectedMediaRecorderAudioSource(value: String) {
        _selectedMediaRecorderAudioSource.value = value
        settingsRepository.saveSomeSetting(
            key = PreferenceKeys.SELECTED_MEDIA_RECORDER_AUDIO_SOURCE,
            value = value.toString()
        )
    }

    suspend fun getSelectedMediaRecorderAudioSource(): String {
        val flow = settingsRepository.readSomeSetting(PreferenceKeys.SELECTED_MEDIA_RECORDER_AUDIO_SOURCE)
        val value = flow.firstOrNull() ?: "Default"
        return value.toString() ?: "Default"
    }


//    suspend fun setAudioSources(value: String) {
//        _audioSources.value = value
//        settingsRepository.saveSomeSetting(
//            key = PreferenceKeys.AUDIO_SOURCES,
//            value = value.toString()
//        )
//    }

    suspend fun getAudioSources(): String {
        val flow = settingsRepository.readSomeSetting(PreferenceKeys.AUDIO_SOURCES)
        val value = flow.firstOrNull() ?: "Default"
        return value.toString() ?: "Default"
    }


    suspend fun setSelectedAudioBitrate(value: String) {
        _selectedAudioBitrate.value = value
        settingsRepository.saveSomeSetting(
            key = PreferenceKeys.SELECTED_AUDIO_BITRATE,
            value = value.toString()
        )
    }

    suspend fun getSelectedAudioBitrate(): String {
        val flow = settingsRepository.readSomeSetting(PreferenceKeys.SELECTED_AUDIO_BITRATE)
        val value = flow.firstOrNull() ?: "Default"
        return value.toString() ?: "Default"
    }


    suspend fun setSelectedVideoQuality(videoQuality: String) {
        val quality = videoQualities.find { it.resolution == videoQuality } ?: videoQualities.firstOrNull()
        _selectedVideoQuality.value = quality

        settingsRepository.saveSomeSetting(
            key = PreferenceKeys.SELECTED_VIDEO_RESOLUTION,
            value = videoQuality // Store the resolution string
        )
    }

    suspend fun getSelectedVideoResolutionString(): String {
        val flow = settingsRepository.readSomeSetting(PreferenceKeys.SELECTED_VIDEO_RESOLUTION)
        val value = flow.firstOrNull() ?: "720p"
        return value.toString()
    }


    fun getVideoStoragePath(context: Context): String {
        val location = getStorageLocation()
        // Implement logic to return the actual path based on "Internal" or "External" (SD Card)
        return if (location == "Internal") context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.absolutePath ?: "" else "Path for SD Card" // Placeholder
    }


    // Storage Location
    fun getStorageLocation(): String {
        // Assuming "Internal" is the default or first option. Adjust as necessary.
        return sharedPreferences.getString("storage_location", "Internal") ?: "Internal"
    }

    fun setStorageLocation(location: String) {
        sharedPreferences.edit().putString("storage_location", location).apply()
    }

    // App Lock Pattern
    fun getAppLockPattern(): List<Int>? {
        val patternString = sharedPreferences.getString("app_lock_pattern", null)
        return patternString?.split(",")?.mapNotNull { it.toIntOrNull() }
    }

    fun setAppLockPattern(pattern: List<Int>) {
        sharedPreferences.edit().putString("app_lock_pattern", pattern.joinToString(",")).apply()
    }
    fun isAppLockEnabled(): Boolean {
        return sharedPreferences.getBoolean("app_lock_enabled", false)
    }

    fun setAppLockEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("app_lock_enabled", enabled).apply()
    }

    // This line seems problematic as `sharedViewModel` is not defined here.
    // It should likely be `this.isAppLockEnabled()` or similar.
    // and `mediaRecorderAudioSources` needs to be defined.
    // For now, I'll assume a default value or you'll adjust it.

    // TODO: Define mediaRecorderAudioSources, perhaps as a list of pairs (DisplayName, Value)
    // val mediaRecorderAudioSources = listOf("Default" to "0", "Mic" to "1", ...)

    // Initialize with the stored value or a default. The display name part (Int) is not stored directly.
    // You might need a way to map the stored string value back to the Pair<Int, String> if the Int part is crucial for display.
    // For simplicity, I'm assuming the String part is sufficient for now.
//    fun getMediaRecorderAudioSource(): Pair<Int, String>? {
//        val value = sharedPreferences.getString("media_recorder_audio_source_value", null)
//        return value?.let { Pair(0, it) } // Replace `0` with the appropriate default Int value
//    }
//    fun setMediaRecorderAudioSource(sourcePair: Pair<Int, String>) {
//        sharedPreferences.edit().putString("media_recorder_audio_source_value", sourcePair.second).apply()
//        selectedMediaRecorderAudioSource = sourcePair.second // Update with the string value for consistency
//    }

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

    fun updateText(newValue: String) {
        _text.value = newValue
    }

    fun updateServiceRunning(value: Boolean) {
        _isServiceRunning.update {
            value
        }
    }

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
