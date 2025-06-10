package com.globewaystechnologies.slidevideospy.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



open class SharedViewModel: ViewModel() {
    private val _text = MutableStateFlow("Initial Text")
    open val text: StateFlow<String> = _text

    fun updateText(newValue: String) {
        _text.value = newValue
    }
}


