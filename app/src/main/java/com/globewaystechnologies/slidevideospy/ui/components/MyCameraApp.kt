package com.globewaystechnologies.slidevideospy.ui.components

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

enum class CameraSelection {
    FRONT, BACK, BOTH
}

@Composable
fun CameraSelectionScreen(
    onSelectionChanged: (CameraSelection, List<String>) -> Unit // Passes selection and relevant camera IDs
) {
    val context = LocalContext.current
    val cameraManager = remember {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    var selectedOption by remember { mutableStateOf<CameraSelection?>(null) }
    val (frontCameraId, backCameraId, concurrentCameraIds) = remember {
        getCameraDetails(cameraManager)
    }

    val canStreamConcurrently = concurrentCameraIds.isNotEmpty()

    Log.d("Concurent Camers", "${canStreamConcurrently}" )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Select Camera:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        // Front Camera Option
        if (frontCameraId != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedOption == CameraSelection.FRONT,
                    onClick = {
                        selectedOption = CameraSelection.FRONT
                        onSelectionChanged(CameraSelection.FRONT, listOfNotNull(frontCameraId))
                    }
                )
                Text("Front Camera")
            }
        }

        // Back Camera Option
        if (backCameraId != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedOption == CameraSelection.BACK,
                    onClick = {
                        selectedOption = CameraSelection.BACK
                        onSelectionChanged(CameraSelection.BACK, listOfNotNull(backCameraId))
                    }
                )
                Text("Back Camera")
            }
        }

        // Both Cameras (Concurrent) Option
        if (canStreamConcurrently) {
            // Check if there's a pair specifically for front and back
            val frontAndBackConcurrent = concurrentCameraIds.any {
                it.contains(frontCameraId) && it.contains(backCameraId)
            }

            Log.d("Concurent Camers2", "${frontAndBackConcurrent}" )
            if (frontAndBackConcurrent) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedOption == CameraSelection.BOTH,
                        onClick = {
                            selectedOption = CameraSelection.BOTH
                            onSelectionChanged(
                                CameraSelection.BOTH,
                                listOfNotNull(frontCameraId, backCameraId)
                            )
                        }
                    )
                    Text("Both Front and Back Cameras")
                }
            }
        }
        if (frontCameraId == null && backCameraId == null) {
            Text("No cameras available on this device.")
        } else if (selectedOption == null) {
            Text("Please select a camera option.")
        }
    }
}

// Helper function to get camera IDs and concurrent capabilities
private fun getCameraDetails(cameraManager: CameraManager): Triple<String?, String?, Set<Set<String>>> {
    var frontCameraId: String? = null
    var backCameraId: String? = null
    val concurrentCameraIds = mutableSetOf<Set<String>>()

    try {
        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            when (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                CameraCharacteristics.LENS_FACING_FRONT -> frontCameraId = cameraId
                CameraCharacteristics.LENS_FACING_BACK -> backCameraId = cameraId
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            concurrentCameraIds.addAll(cameraManager.concurrentCameraIds)
        }
    } catch (e: Exception) {
        // Handle CameraAccessException or other exceptions
        e.printStackTrace()
    }
    return Triple(frontCameraId, backCameraId, concurrentCameraIds)
}

// Example Usage in your Composable
@Composable
fun MyCameraApp() {
    var selectedCameraInfo by remember { mutableStateOf<Pair<CameraSelection, List<String>>?>(null) }

    Column {
        CameraSelectionScreen { selection, cameraIds ->
            selectedCameraInfo = selection to cameraIds
            // Now you know which camera(s) to open
            // Proceed to initialize CameraCaptureSession with the selected cameraIds
            println("Selected: $selection, Camera IDs: $cameraIds")
        }

        selectedCameraInfo?.let { (selection, ids) ->
            Text(
                "You selected: $selection with ${ids.joinToString()}",
                modifier = Modifier.padding(16.dp)
            )
            // Here you would typically navigate to a camera preview screen
            // or start the camera capture process based on the selection.
        }
    }
}