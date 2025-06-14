package com.globewaystechnologies.slidevideospy.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.stringPreferencesKey


object PreferenceKeys {
    val SELECTED_CAMERA_GROUP = stringPreferencesKey("selected_camera_group")
    val SELECTED_SLOT_DURATION_MILLIS = stringPreferencesKey("selectedSlotDurationMillis")
    val SELECTED_IS_AUDIO_ENABLED = stringPreferencesKey("isAudioEnabled")
    val SELECTED_MEDIA_RECORDER_AUDIO_SOURCE = stringPreferencesKey("selectedMediaRecorderAudioSource")
    val AUDIO_SOURCES = stringPreferencesKey("audioSources")
    val SELECTED_AUDIO_BITRATE = stringPreferencesKey("selectedAudioBitrate")
    val SELECTED_VIDEO_RESOLUTION = stringPreferencesKey("selectedVideoResolution")

}

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    suspend fun saveSomeSetting(key: Preferences.Key<String>, value: String) {
        dataStore.edit { settings ->
            settings[key] = value
        }
    }

    fun readSomeSetting(key: Preferences.Key<String>): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[key] ?: "default_value"
        }
    }


}





