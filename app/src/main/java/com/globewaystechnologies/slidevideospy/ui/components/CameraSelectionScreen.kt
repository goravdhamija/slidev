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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.globewaystechnologies.slidevideospy.viewmodel.CameraViewModel

@Composable
fun CameraSelectionScreen(
    cameraViewModel: CameraViewModel = viewModel(),
    onCameraIdsSelected: (Set<Any>) -> Unit
) {
    val uiState by cameraViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val cameraManager = context.getSystemService(CAMERA_SERVICE) as CameraManager
    val showPreviews by cameraViewModel.showPreviews.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f)) // Left spacer

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(360.dp)
                .padding(16.dp)
                .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        ) {
            Text("Available Cameras:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))

            if (uiState.error != null) {
                Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            val cornerRadius = 16.dp
            val gradientBrush = Brush.linearGradient(colors = listOf(Color.Cyan, Color.Blue))

            for (cameraGroup in uiState.allCameraGroupsForSelection) {
                var localShowPreview by remember(cameraGroup) { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(brush = gradientBrush, shape = RoundedCornerShape(cornerRadius))
                        .clip(RoundedCornerShape(cornerRadius))
                        .clickable {
                            cameraViewModel.selectedCameraGroupFun(cameraGroup)
                            onCameraIdsSelected(cameraGroup)
                        }
                        .padding(1.dp)
                ) {
                    val parts = cameraGroup.toString().split(",").map { it.trim() }
                    val type = parts[0].removePrefix("[").removeSuffix("]").trim()
                    val val2 = parts[2].removePrefix("[").removeSuffix("]").trim()

                    var cameraDeviceID1: String? = null
                    var cameraDeviceID2: String? = null

                    if (type == "single") {
                        cameraDeviceID1 = val2.toInt().toString()
                    } else if (type == "double") {
                        val itemsx = val2.removePrefix("{").removeSuffix("}")
                            .split(",").map { it.trim() }
                        cameraDeviceID1 = itemsx[0].toInt().toString()

                        val val3 = parts[3].removePrefix("[").removeSuffix("]").trim()
                        val itemsy = val3.removePrefix("{").removeSuffix("}")
                            .split(",").map { it.trim() }
                        cameraDeviceID2 = itemsy[0].toInt().toString()
                    }

                    val characteristics1 = cameraManager.getCameraCharacteristics(cameraDeviceID1.toString())
                    val facing = when (characteristics1.get(CameraCharacteristics.LENS_FACING)) {
                        CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                        CameraCharacteristics.LENS_FACING_BACK -> "Back"
                        else -> "Unknown"
                    }
                    val orientation = characteristics1.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
                    val map = characteristics1.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    val resolutions = map?.getOutputSizes(MediaRecorder::class.java)?.map { "${it.width}x${it.height}" } ?: emptyList()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(cornerRadius - 1.dp))
                            .background(Color.White)
                            .padding(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {

                            Button(
                                onClick = {
                                    localShowPreview = !localShowPreview
                                    cameraViewModel.setShowPreviews(localShowPreview)
                                },
                                enabled = (uiState.selectedCameraGroup == cameraGroup.toString()),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Text(text = if (localShowPreview) "Hide Preview" else "Preview")
                            }

                            if (uiState.selectedCameraGroup == cameraGroup.toString() && localShowPreview) {
                                DualCameraPreviewScreenWithParams(
                                    frontCameraId = cameraDeviceID1,
                                    backCameraId = cameraDeviceID2,
                                    frontModifier = Modifier
                                        .width(144.dp)
                                        .height(176.dp)
                                        .padding(start = 16.dp, top = 16.dp),
                                    backModifier = Modifier
                                        .width(144.dp)
                                        .height(176.dp)
                                        .padding(end = 16.dp, top = 16.dp)
                                )
                            }

                            Text("Camera ID: $cameraDeviceID1", style = MaterialTheme.typography.bodyLarge)
                            Text("Facing: $facing", style = MaterialTheme.typography.bodyMedium)
                            Text("Orientation: $orientation", style = MaterialTheme.typography.bodyMedium)
                            Text("Resolutions: ${resolutions.joinToString()}", style = MaterialTheme.typography.bodySmall)
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
}


@Composable
fun MyCameraAppWithViewModel(cameraViewModel: CameraViewModel) {
    Column(
        modifier = Modifier
            .padding(2.dp)
            .fillMaxWidth()
    ) {
        CameraSelectionScreen(cameraViewModel = cameraViewModel) { selectedIds ->
            println("Camera IDs selected in Composable: $selectedIds")
        }
    }
}
