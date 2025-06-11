package com.globewaystechnologies.slidevideospy.ui.components

// CameraSelectionScreen.kt (or wherever your Composable is)
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Import for viewModel
import com.globewaystechnologies.slidevideospy.viewmodel.CameraViewModel

// Assuming CameraSelection enum and CameraViewModel are defined as above

@Composable
fun CameraSelectionScreen(
    cameraViewModel: CameraViewModel = viewModel(),
    onCameraIdsSelected: (Set<Any>) -> Unit
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



//        for (cameraGroup in uiState.allCameraGroupsForSelection) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                RadioButton(
//                    selected = (uiState.selectedCameraGroup == cameraGroup),
//                    onClick = {
//                        cameraViewModel.selectedCameraGroupFun(cameraGroup)
//                        onCameraIdsSelected(uiState.selectedCameraGroup)
//                    }
//                )
//                Text("Camera: ${cameraGroup}")
//            }
//        }



        for (cameraGroup in uiState.allCameraGroupsForSelection) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .clickable {
                        cameraViewModel.selectedCameraGroupFun(cameraGroup)
                        onCameraIdsSelected(cameraGroup)
                    }
            ) {
                RadioButton(
                    selected = (uiState.selectedCameraGroup == cameraGroup),
                    onClick = null // Let Row handle the click
                )
                Text(
                    text = "Camera: $cameraGroup",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }





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

            println("Camera IDs selected in Composable: $selectedIds")
        }

        if (cameraUiState.selectedCameraGroup.isNotEmpty()) {
            Text(
                "ViewModel says: Selected ${cameraUiState.selectedCameraGroup} " +
                        "with IDs: ${cameraUiState.selectedCameraGroup.joinToString()}",
                modifier = Modifier.padding(16.dp)
            )

        } else {
            Text(
                "ViewModel says: Selected ${cameraUiState.selectedCameraGroup} but no valid camera IDs found (check logic).",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}