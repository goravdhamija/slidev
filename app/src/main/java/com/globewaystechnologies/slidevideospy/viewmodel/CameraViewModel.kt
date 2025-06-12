package com.globewaystechnologies.slidevideospy.viewmodel

import DualPreviewServiceWithIds

import android.app.Application
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.globewaystechnologies.slidevideospy.data.SettingsRepository
import com.globewaystechnologies.slidevideospy.dataStore
import com.globewaystechnologies.slidevideospy.services.PinkService
import com.globewaystechnologies.slidevideospy.utils.isMyServiceRunning
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
    val selectedCameraGroup: String = "", // Serialized string of selected camera group
    val error: String? = null
)

data class CameraGroupInfo(
    val id: String,
    val facing: String, // "Front" or "Back"
    val orientation: Int, // in degrees, e.g., 0, 90, 180, 270
    val resolutions: List<String>,
    val characteristics: Map<String, String>
)

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val cameraManager = application.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val _uiState = MutableStateFlow(CameraSelectionUiState())
    val uiState: StateFlow<CameraSelectionUiState> = _uiState.asStateFlow()

    private val _showPreviews = MutableStateFlow(true)
    val showPreviews: StateFlow<Boolean> = _showPreviews



    private var dualService: DualPreviewServiceWithIds? = null
    private var isServiceActive = false


    init {
        loadCameraDetails()
    }

    private fun loadCameraDetails() {
        viewModelScope.launch {
            try {

                val allSelectableCameraItemsSets = mutableSetOf<Set<Any>>()

                cameraManager.cameraIdList.forEach { cameraId ->

                    Log.d("cameraId", "${cameraId}")
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




                _uiState.update {
                    it.copy(

                        allCameraGroupsForSelection = allSelectableCameraItemsSets,
                        error = null // Clear any previous error
                    )
                }


                val settingsRepository = SettingsRepository(getApplication<Application>().dataStore)
                settingsRepository.readSelectedCameraGroup().collect { savedGroup ->
                    if (savedGroup.isNotEmpty()) {

                        _uiState.update {
                            it.copy(selectedCameraGroup = "[${savedGroup}]")
                        }
//                        Log.d("Camera Group 1", "[${savedGroup}]" )
                    }
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

    fun deserializeToSet(input: String): Set<String> {
        return input
            .removePrefix("[")
            .removeSuffix("]")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    fun selectedCameraGroupFun(selection: Set<Any>) {


        _uiState.update { currentState ->

            currentState.copy(
                selectedCameraGroup = selection.toString(),

            )
        }


        viewModelScope.launch {

            val settingsRepository = SettingsRepository(getApplication<Application>().dataStore)
            settingsRepository.saveSelectedCameraGroup(selection.joinToString()) // Use custom serialization if needed


        }
    }

    // Call this if you need to refresh camera details (e.g., if a camera is connected/disconnected)
    fun refreshCameraDetails() {
        loadCameraDetails()
    }


    fun hidePreviews() {
        _showPreviews.value = false
    }

    fun showPreviewsAgain() {
        _showPreviews.value = true
    }



    fun startDualPreview(
        context: Context,
        frontSurface: Surface,
        backSurface: Surface,
        frontId: String,
        backId: String
    ) {
        if (!isServiceActive) {
            dualService = DualPreviewServiceWithIds(
                context = context,
                frontCameraId = frontId,
                backCameraId = backId,
                frontSurface = frontSurface,
                backSurface = backSurface
            )
            dualService?.start()
            isServiceActive = true
        }
    }

    fun stopDualPreview() {
        dualService?.stop()
        dualService = null
        isServiceActive = false
    }

    fun resumeAfterPinkService(context: Context, frontSurface: Surface, backSurface: Surface) {
        stopDualPreview()
        // Optional small delay to ensure PinkService releases cameras
        Handler(Looper.getMainLooper()).postDelayed({
            startDualPreview(context, frontSurface, backSurface, "1", "0")
        }, 300)
    }


}