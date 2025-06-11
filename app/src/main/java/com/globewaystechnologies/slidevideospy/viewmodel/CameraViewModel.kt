package com.globewaystechnologies.slidevideospy.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// CameraViewModel.kt
// Enum for camera selection options (can be in the same file or a separate one)


// Data class to hold the state of camera selection and available cameras
data class CameraSelectionUiState(

    val allCameraGroupsForSelection: Set<Set<Any>> = emptySet(), // IDs to use for opening camera
    val selectedCameraGroup: Set<Any> = emptySet(),

    val error: String? = null
) {

}

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val cameraManager = application.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val _uiState = MutableStateFlow(CameraSelectionUiState())
    val uiState: StateFlow<CameraSelectionUiState> = _uiState.asStateFlow()

    init {
        loadCameraDetails()
    }

    private fun loadCameraDetails() {
        viewModelScope.launch {
            try {
                val allSelectableCameraItems = mutableMapOf<Int,Set<String>>()
                val allSelectableCameraItemsSets = mutableSetOf<Set<Any>>()

                cameraManager.cameraIdList.forEach { cameraId ->
                    val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                    when (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                        CameraCharacteristics.LENS_FACING_FRONT -> {

                            var setTemp = mutableSetOf<Any>()
                            setTemp.add("single")
                            setTemp.add(CameraCharacteristics.LENS_FACING_FRONT)
                            setTemp.add(cameraId)
                            allSelectableCameraItemsSets.add(setTemp)
                        }
                        CameraCharacteristics.LENS_FACING_BACK -> {


                            var setTemp = mutableSetOf<Any>()
                            setTemp.add("single")
                            setTemp.add(CameraCharacteristics.LENS_FACING_BACK)
                            setTemp.add(cameraId)
                            allSelectableCameraItemsSets.add(setTemp)

                        }
                        else -> { /* Handle external or unknown cameras if needed */
                        }
                    }
                }



                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                    cameraManager.concurrentCameraIds.forEach { cameraIds ->
                        var setTemp = mutableSetOf<Any>()
                        setTemp.add("double")
                        setTemp.add(2)
                        setTemp.add(cameraIds)
                        allSelectableCameraItemsSets.add(setTemp)
                    }
                }

                Log.d("Concurent Cameras all", "${allSelectableCameraItemsSets}" )


                _uiState.update {
                    it.copy(

                        allCameraGroupsForSelection = allSelectableCameraItemsSets,
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



    fun selectedCameraGroupFun(selection: Set<Any>) {
        _uiState.update { currentState ->

            currentState.copy(
                selectedCameraGroup = selection,

            )
        }
    }

    // Call this if you need to refresh camera details (e.g., if a camera is connected/disconnected)
    fun refreshCameraDetails() {
        loadCameraDetails()
    }
}