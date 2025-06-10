package com.globewaystechnologies.slidevideospy.ui.components

// CameraSelectionScreen.kt (or wherever your Composable is)
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Import for viewModel
import com.globewaystechnologies.slidevideospy.viewmodel.CameraSelection
import com.globewaystechnologies.slidevideospy.viewmodel.CameraViewModel

// Assuming CameraSelection enum and CameraViewModel are defined as above

@Composable
fun CameraSelectionScreen(
    cameraViewModel: CameraViewModel = viewModel(), // Get ViewModel instance
    // Remove onSelectionChanged if all logic is handled by ViewModel interactions
    // Or keep it if the parent composable needs to know the raw selection immediately for other reasons
    onCameraIdsSelected: (List<String>) -> Unit // Callback with the actual camera IDs to use
) {
    val uiState by cameraViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text("Select Camera:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (uiState.error != null) {
            Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

//        // Front Camera Option
//        if (uiState.frontCameraId != null) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                RadioButton(
//                    selected = uiState.selectedOption == CameraSelection.FRONT,
//                    onClick = {
//                        cameraViewModel.selectCamera(CameraSelection.FRONT)
//                        // Directly use the updated state or pass it via callback
//                        onCameraIdsSelected(listOfNotNull(uiState.frontCameraId))
//                    }
//                )
//                Text("Front Camera")
//            }
//        }
//
//
//
//        // Back Camera Option
//        if (uiState.backCameraId != null) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                RadioButton(
//                    selected = uiState.selectedOption == CameraSelection.BACK,
//                    onClick = {
//                        cameraViewModel.selectCamera(CameraSelection.BACK)
//                        onCameraIdsSelected(listOfNotNull(uiState.backCameraId))
//                    }
//                )
//                Text("Back Camera")
//            }
//        }


        if (uiState.frontCameraIds != null) {

            // Display all front camera IDs
            Text("Available Front Camera IDs: ${uiState.frontCameraIds.joinToString(", ")}")

            for (frontCamera in uiState.frontCameraIds) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.selectedOption == CameraSelection.FRONT,
                        onClick = {
                            cameraViewModel.selectCamera(CameraSelection.FRONT)
                            // Directly use the updated state or pass it via callback
                            onCameraIdsSelected(listOfNotNull(uiState.frontCameraId))
                        }
                    )
                    Text("Front Camera ID: $frontCamera")
                }
            }

        }


        if (uiState.backCameraIds != null) {

            // Display all front camera IDs
            Text("Available Back Camera IDs: ${uiState.backCameraIds.joinToString(", ")}")
            // Back Camera Option

                for (backCameraId in uiState.backCameraIds) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.selectedOption == CameraSelection.BACK,
                        onClick = {
                            cameraViewModel.selectCamera(CameraSelection.BACK)
                            onCameraIdsSelected(listOfNotNull(uiState.backCameraId))
                        }
                    )
                    Text("Back Camera ID: ${backCameraId}")
                }
            }

        }

        // Both Cameras (Concurrent) Option
        Log.d("Concurent Cameras", "${uiState.canStreamConcurrently}" )
        Log.d("Concurent Cameras", "${uiState.concurrentCameraIdSets.isNotEmpty()}" )
        if (uiState.concurrentCameraIdSets.isNotEmpty()) {
            for (pair in uiState.concurrentCameraIdSets) {
                Log.d("Concurent Cameras", "$pair" )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.selectedOption == CameraSelection.BOTH,
                        onClick = {
                            cameraViewModel.selectCamera(CameraSelection.BOTH)
                            onCameraIdsSelected(pair.toList())
                        }
                    )

                    Text("Concurrent Cameras: ${pair.joinToString(", ")}")

                }

            }


//            Row(verticalAlignment = Alignment.CenterVertically) {
//                RadioButton(
//                    selected = uiState.selectedOption == CameraSelection.BOTH,
//                    onClick = {
//                        cameraViewModel.selectCamera(CameraSelection.BOTH)
//                        onCameraIdsSelected(
//                            listOfNotNull(
//                                uiState.frontCameraId,
//                                uiState.backCameraId
//                            )
//                        )
//                    }
//                )
//                Text("Front Camera and Back Cameras")
//            }





        }

        if (uiState.frontCameraId == null && uiState.backCameraId == null && uiState.error == null) {
            Text("No cameras available on this device.")
        } else if (uiState.selectedOption == CameraSelection.NONE && uiState.error == null) {
            Text("Please select a camera option.")
        }

        // You can observe uiState.availableCameraIdsForSelection in the parent composable
        // or trigger actions based on it here.
    }
}

// Example Usage in your main app composable
@Composable
fun MyCameraAppWithViewModel() {
    val cameraViewModel: CameraViewModel = viewModel()
    val cameraUiState by cameraViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()

    ) {
        CameraSelectionScreen(
            cameraViewModel = cameraViewModel // Pass the ViewModel instance
        ) { selectedIds ->
            // This callback is useful if the parent needs immediate access to the IDs
            // However, most of the time you'd observe cameraUiState.availableCameraIdsForSelection
            println("Camera IDs selected in Composable: $selectedIds")
        }

        // Observe the selected camera IDs from the ViewModel's state
        if (cameraUiState.availableCameraIdsForSelection.isNotEmpty()) {
            Text(
                "ViewModel says: Selected ${cameraUiState.selectedOption} " +
                        "with IDs: ${cameraUiState.availableCameraIdsForSelection.joinToString()}",
                modifier = Modifier.padding(16.dp)
            )
            // Here you would typically navigate to a camera preview screen
            // or start the camera capture process based on cameraUiState.availableCameraIdsForSelection
            // and cameraUiState.selectedOption
        } else if (cameraUiState.selectedOption != CameraSelection.NONE) {
            Text(
                "ViewModel says: Selected ${cameraUiState.selectedOption} but no valid camera IDs found (check logic).",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}