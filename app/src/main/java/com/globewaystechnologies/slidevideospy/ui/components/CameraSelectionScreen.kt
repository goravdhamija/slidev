package com.globewaystechnologies.slidevideospy.ui.components



import DualCameraPreviewScreenWithParams

import android.app.Service.CAMERA_SERVICE
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.MediaRecorder
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    var context = LocalContext.current
    var cameraManager = context.getSystemService(CAMERA_SERVICE) as CameraManager
    val showPreviews by cameraViewModel.showPreviews.collectAsState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text("Available Cameras in your Phone:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (uiState.error != null) {
            Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }



        val cornerRadius = 16.dp
        val gradientBrush = Brush.linearGradient(
            colors = listOf(Color.Cyan, Color.Blue)
        )

        for (cameraGroup in uiState.allCameraGroupsForSelection) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .background(brush = gradientBrush, shape = RoundedCornerShape(cornerRadius))
                    .clip(RoundedCornerShape(cornerRadius))
                    .clickable {
                        cameraViewModel.selectedCameraGroupFun(cameraGroup)
                        onCameraIdsSelected(cameraGroup)
                    }
                    .padding(1.dp) // Simulates border thickness
            ) {


                val parts = cameraGroup.toString().split(",").map { it.trim() }
                val type = parts[0].removePrefix("[").removeSuffix("]").trim()     // "single"
                val val1 = parts[1].removePrefix("[").removeSuffix("]").trim()     // "0"
                val val2 = parts[2].removePrefix("[").removeSuffix("]").trim()     // "1"

                var cameraDevice1 = cameraManager.cameraIdList.firstOrNull { it == val2 } ?: "Unknown Camera ID"
                var cameraDevice2 = cameraManager.cameraIdList.firstOrNull { it == val2 } ?: "Unknown Camera ID"

                var cameraDeviceID1:String? = null
                var cameraDeviceID2:String? = null

                if (type == "single") {
                    cameraDeviceID1 = val2.toInt().toString()

                }
                else if (type == "double") {

                    val itemsx = val2.removePrefix("{").removeSuffix("}")
                        .split(",")
                        .map { it.trim() }
                    cameraDeviceID1 = itemsx[0].toInt().toString()

                    val val3 = parts[3].removePrefix("[").removeSuffix("]").trim()
                    val itemsy = val3.removePrefix("{").removeSuffix("}")
                        .split(",")
                        .map { it.trim() }
                    cameraDeviceID2 = itemsy[0].toInt().toString()


                }

                val characteristics1 = cameraManager.getCameraCharacteristics(cameraDeviceID1.toString())
                val facing = when (characteristics1.get(CameraCharacteristics.LENS_FACING)) {
                    CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                    CameraCharacteristics.LENS_FACING_BACK -> "Back"
                    else -> "Unknown"
                }
                var orientation = characteristics1.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
                val map = characteristics1.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val resolutions = map?.getOutputSizes(MediaRecorder::class.java)?.map { "${it.width}x${it.height}" } ?: emptyList()



                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(cornerRadius - 1.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {

                        if (uiState.selectedCameraGroup == cameraGroup.toString() && showPreviews) {

                                DualCameraPreviewScreenWithParams(
                                    frontCameraId = cameraDeviceID1,
                                    backCameraId = cameraDeviceID2,
                                    frontModifier = Modifier
                                        .width(70.dp)
                                        .height(150.dp)
                                        .padding(start = 16.dp, top = 16.dp),
                                    backModifier = Modifier
                                        .width(70.dp)
                                        .height(150.dp)
                                        .padding(end = 16.dp, top = 16.dp)
                                )

                        }


                        Text(
                            text = "Camera ID: ${cameraDeviceID1}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Facing: ${facing}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Orientation: ${orientation}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Resolutions: ${resolutions.joinToString()}",
                            style = MaterialTheme.typography.bodySmall
                        )

                    }

                    RadioButton(
                        selected = (uiState.selectedCameraGroup == cameraGroup.toString()),
                        onClick = null
                    )
                }




            }
        }





    }
}

// Example Usage in your main app composable
@Composable
fun MyCameraAppWithViewModel(cameraViewModel: CameraViewModel) {

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()

    ) {
        CameraSelectionScreen( cameraViewModel = cameraViewModel ) { selectedIds ->
            println("Camera IDs selected in Composable: $selectedIds")
        }


    }
}