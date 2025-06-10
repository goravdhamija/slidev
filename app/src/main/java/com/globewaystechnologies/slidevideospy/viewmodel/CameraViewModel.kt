package com.globewaystechnologies.slidevideospy.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// CameraViewModel.kt
// Enum for camera selection options (can be in the same file or a separate one)
enum class CameraSelection {
    FRONT, BACK, BOTH, NONE // Added NONE for initial state
}

// Data class to hold the state of camera selection and available cameras
data class CameraSelectionUiState(
    val selectedOption: CameraSelection = CameraSelection.NONE,
    val frontCameraId: String? = null,
    val backCameraId: String? = null,
    val concurrentCameraIdSets: Set<Set<String>> = emptySet(),
    val availableCameraIdsForSelection: List<String> = emptyList(), // IDs to use for opening camera
    val error: String? = null
) {
    val canStreamConcurrently: Boolean
        get() = concurrentCameraIdSets.any { set ->
            frontCameraId != null && backCameraId != null &&
                    set.contains(frontCameraId) && set.contains(backCameraId)
        }
}

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val cameraManager =
        application.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private val _uiState = MutableStateFlow(CameraSelectionUiState())
    val uiState: StateFlow<CameraSelectionUiState> = _uiState.asStateFlow()

    init {
        loadCameraDetails()
    }

    private fun loadCameraDetails() {
        viewModelScope.launch {
            try {
                var frontId: String? = null
                var backId: String? = null
                val concurrentIds = mutableSetOf<Set<String>>()

                cameraManager.cameraIdList.forEach { cameraId ->
                    val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                    when (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                        CameraCharacteristics.LENS_FACING_FRONT -> frontId = cameraId
                        CameraCharacteristics.LENS_FACING_BACK -> backId = cameraId
                        else -> { /* Handle external or unknown cameras if needed */
                        }
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    concurrentIds.addAll(cameraManager.concurrentCameraIds)
                }

                _uiState.update {
                    it.copy(
                        frontCameraId = frontId,
                        backCameraId = backId,
                        concurrentCameraIdSets = concurrentIds,
                        error = null // Clear any previous error
                    )
                }
            } catch (e: Exception) { // Catching generic Exception for brevity, be more specific
                _uiState.update {
                    it.copy(error = "Failed to load camera details: ${e.message}")
                }
                // Log the exception
                e.printStackTrace()
            }
        }
    }

    fun selectCamera(selection: CameraSelection) {
        _uiState.update { currentState ->
            val idsToUse = when (selection) {
                CameraSelection.FRONT -> listOfNotNull(currentState.frontCameraId)
                CameraSelection.BACK -> listOfNotNull(currentState.backCameraId)
                CameraSelection.BOTH -> {
                    if (currentState.canStreamConcurrently && currentState.frontCameraId != null && currentState.backCameraId != null) {
                        listOfNotNull(currentState.frontCameraId, currentState.backCameraId)
                    } else {
                        // Fallback or error: cannot select BOTH if not supported or cameras missing
                        currentState.availableCameraIdsForSelection // or emptyList()
                    }
                }

                CameraSelection.NONE -> emptyList()
            }
            currentState.copy(
                selectedOption = selection,
                availableCameraIdsForSelection = idsToUse
            )
        }
    }

    // Call this if you need to refresh camera details (e.g., if a camera is connected/disconnected)
    fun refreshCameraDetails() {
        loadCameraDetails()
    }
}