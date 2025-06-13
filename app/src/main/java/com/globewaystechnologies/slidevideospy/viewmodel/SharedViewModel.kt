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
import androidx.lifecycle.ViewModel
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
}


