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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PermCameraMic
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material3.Icon


@Composable
fun CameraIdLabel(cameraId: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.PermCameraMic,
            contentDescription = "Camera ID",
            tint = Color(0xFF0D47A1), // Indigo
            modifier = Modifier
                .size(20.dp)
                .padding(end = 6.dp)
        )
        Text(
            text = "Camera ID: $cameraId",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF0D47A1)
        )
    }
}

@Composable
fun CameraFacingLabel(facing: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (facing == "Front") Icons.Filled.CameraFront else Icons.Filled.CameraRear,
            contentDescription = "Camera Facing Icon",
            tint = Color(0xFF0D47A1), // Dark Blue
            modifier = Modifier
                .size(20.dp)
                .padding(end = 6.dp)
        )
        Text(
            text = "$facing Camera",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black
        )
    }
}

@Composable
fun CameraOrientationLabel(orientation: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ScreenRotation,
            contentDescription = "Sensor Orientation Icon",
            tint = Color(0xFF6A1B9A), // A stylish deep purple
            modifier = Modifier
                .size(20.dp)
                .padding(end = 6.dp)
        )
        Text(
            text = "$orientation°",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black
        )
    }
}

@Composable
fun MaxResolutionLabel(resolutions: List<String>) {
    if (resolutions.isNotEmpty()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Hd,
                contentDescription = "Max Resolution",
                tint = Color(0xFF004D40), // Teal tone
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 6.dp)
            )
            Text(
                text = "${resolutions.first()}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black
            )
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
            .padding(1.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f)) // Left spacer

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(760.dp)
                .padding(16.dp)
                .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        ) {
            Text(
                "Available Cameras:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp)
            )

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

                    val characteristics1 =
                        cameraManager.getCameraCharacteristics(cameraDeviceID1.toString())
                    val facing = when (characteristics1.get(CameraCharacteristics.LENS_FACING)) {
                        CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                        CameraCharacteristics.LENS_FACING_BACK -> "Back"
                        else -> "Unknown"
                    }
                    val orientation =
                        characteristics1.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
                    val map =
                        characteristics1.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    val resolutions = map?.getOutputSizes(MediaRecorder::class.java)
                        ?.map { "${it.width}x${it.height}" } ?: emptyList()


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

                            if (cameraDeviceID2 != null) {
                                Row {
                                    Text(
                                        text = "✅ Concurrent Camera Support Available",
                                        color = Color(0xFF388E3C), // Green
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier
                                            .padding(bottom = 8.dp)
                                    )
                                }
                            }

                            Row (
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                            {

                                Column {


                                    CameraIdLabel(cameraId = cameraDeviceID1.toString())
                                    CameraFacingLabel(facing)
                                    CameraOrientationLabel(orientation)
                                    MaxResolutionLabel(resolutions)
                                }

                                if (cameraDeviceID2 != null) {
                                    val characteristics2 =
                                        cameraManager.getCameraCharacteristics(cameraDeviceID2)
                                    val facing2 =
                                        when (characteristics2.get(CameraCharacteristics.LENS_FACING)) {
                                            CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                                            CameraCharacteristics.LENS_FACING_BACK -> "Back"
                                            else -> "Unknown"
                                        }
                                    val orientation2 =
                                        characteristics2.get(CameraCharacteristics.SENSOR_ORIENTATION)
                                            ?: 0
                                    val map2 =
                                        characteristics2.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                                    val resolutions2 =
                                        map2?.getOutputSizes(MediaRecorder::class.java)
                                            ?.map { "${it.width}x${it.height}" } ?: emptyList()

                                    Column {
                                        CameraIdLabel(cameraId = cameraDeviceID2.toString())
                                        CameraFacingLabel(facing2)
                                        CameraOrientationLabel(orientation2)
                                        MaxResolutionLabel(resolutions2)
                                    }
                                }
                            }
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




