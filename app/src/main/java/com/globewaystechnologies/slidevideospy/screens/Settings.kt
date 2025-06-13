package com.globewaystechnologies.slidevideospy.screens

import android.Manifest
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioDeviceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Size
import androidx.compose.foundation.background
import android.media.MediaRecorder
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn // Keep this for the outer LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.globewaystechnologies.slidevideospy.viewmodel.SharedViewModel

@Composable
fun Settings(sharedViewModel: SharedViewModel) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // Outer padding for the whole screen
    ) {
        item {
            Text("App Settings", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
        }
        // Permissions Section
        item { PermissionsSection(context) }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Audio Settings for Media Recorder
        item { AudioSettings(sharedViewModel) }
        // Media Recorder Settings
        item { MediaRecorderSettings(sharedViewModel) }


    }
}

@Composable
fun PermissionsSection(context: Context) {



    val requiredPermissions = mapOf(
        Manifest.permission.POST_NOTIFICATIONS to "Post Notifications",
        Manifest.permission.CAMERA to "Camera Access",
        Manifest.permission.RECORD_AUDIO to "Record Audio",
        Manifest.permission.ACCESS_FINE_LOCATION to "Location Access",
    )

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White), // Added white background for better visibility
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)

    ) {
        // Changed from LazyColumn to Column as it's inside another LazyColumn item
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(8.dp) // Ensuring Column also has a white background
        ) {
            Text("Permissions", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))

            requiredPermissions.forEach { (permissionKey, permissionValue) ->
                val isGranted = ContextCompat.checkSelfPermission(context, permissionKey) == PackageManager.PERMISSION_GRANTED

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = permissionValue, modifier = Modifier.weight(1f))
                    Text(
                        text = if (isGranted) "Granted" else "Denied",
                        color = if (isGranted) Color.Green else Color.Red,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    if (!isGranted) {
                        Button(onClick = {
                            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = android.net.Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }) {
                            Text("Enable")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AudioSettings(sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    var isAudioEnabled by remember { mutableStateOf(sharedViewModel.isAudioEnabled.value ?: false) }
    val audioBitrates = listOf("64 kbps", "96 kbps", "128 kbps", "192 kbps", "256 kbps", "320 kbps")
    var selectedAudioBitrate by remember { mutableStateOf(sharedViewModel.getAudioBitrate() ?: audioBitrates.first()) }
    var expandedAudioBitrate by remember { mutableStateOf(false) }

    val mediaRecorderAudioSources = remember {
        listOf(
            Pair(MediaRecorder.AudioSource.DEFAULT, "Default"),
            Pair(MediaRecorder.AudioSource.MIC, "Microphone"),
            Pair(MediaRecorder.AudioSource.VOICE_UPLINK, "Voice Uplink"),
            Pair(MediaRecorder.AudioSource.VOICE_DOWNLINK, "Voice Downlink"),
            Pair(MediaRecorder.AudioSource.VOICE_CALL, "Voice Call"),
            Pair(MediaRecorder.AudioSource.CAMCORDER, "Camcorder"),
            Pair(MediaRecorder.AudioSource.VOICE_RECOGNITION, "Voice Recognition"),
            Pair(MediaRecorder.AudioSource.VOICE_COMMUNICATION, "Voice Communication"),
            Pair(MediaRecorder.AudioSource.REMOTE_SUBMIX, "Remote Submix"),
            Pair(MediaRecorder.AudioSource.UNPROCESSED, "Unprocessed"),
            Pair(MediaRecorder.AudioSource.VOICE_PERFORMANCE, "Voice Performance")
        )
    }


    var selectedMediaRecorderAudioSource by remember {
        mutableStateOf(sharedViewModel.getMediaRecorderAudioSource() ?: mediaRecorderAudioSources.first())
    }
    var expandedMediaRecorderAudioSource by remember { mutableStateOf(false) }

    val audioSources = remember { getAvailableAudioSources(context) }
    var selectedAudioSource by remember {
        mutableStateOf(
            sharedViewModel.getAudioSource()?.let { id ->
                audioSources.find { it.id.toString() == id }
            } ?: audioSources.firstOrNull()
        )
    }
    var expandedAudioSource by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        fun getAudioSourceDisplayName(source: AudioDeviceInfo): String {
            val typeName = when (source.type) {
                AudioDeviceInfo.TYPE_BUILTIN_MIC -> "Built-in Mic"
                AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired Headset"
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth SCO"
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth A2DP"
                // Add more types as needed
                else -> "Unknown Source"
            }
            return "${source.productName} ($typeName)"
        }
        Text(
            "Audio Settings",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable {
                    isAudioEnabled = !isAudioEnabled
                    sharedViewModel.setAudioEnabled(isAudioEnabled)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable Audio Recording", modifier = Modifier.weight(1f))
            Switch(checked = isAudioEnabled, onCheckedChange = {
                isAudioEnabled = it
                sharedViewModel.setAudioEnabled(it)
            })
        }

        if (isAudioEnabled) {
            Spacer(modifier = Modifier.height(16.dp))

            // MediaRecorder Audio Source Dropdown
            Text(
                "MediaRecorder Audio Source",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                OutlinedButton(
                    onClick = { expandedMediaRecorderAudioSource = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(selectedMediaRecorderAudioSource?.second ?: "Unknown")
                }// Handle null or uninitialized cases                }
                DropdownMenu(
                    expanded = expandedMediaRecorderAudioSource,
                    onDismissRequest = { expandedMediaRecorderAudioSource = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    mediaRecorderAudioSources.forEach { sourcePair ->
                        DropdownMenuItem(
                            text = { Text(sourcePair.second) },
                            onClick = {
                                selectedMediaRecorderAudioSource = sourcePair
                                sharedViewModel.setMediaRecorderAudioSource(sourcePair) // You'll need to add this method to SharedViewModel
                                expandedMediaRecorderAudioSource = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Audio Source",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                OutlinedButton(
                    onClick = { expandedAudioSource = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    enabled = audioSources.isNotEmpty()
                ) {
                    Text(selectedAudioSource?.let { getAudioSourceDisplayName(it) } ?: "No audio sources available")
                }
                DropdownMenu(
                    expanded = expandedAudioSource && audioSources.isNotEmpty(),
                    onDismissRequest = { expandedAudioSource = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    audioSources.forEach { source -> // Use the descriptive name for the dropdown item
                        DropdownMenuItem(
                            text = { Text(source.productName.toString()) },
                            onClick = {
                                selectedAudioSource = source
                                sharedViewModel.setAudioSource(source.id.toString()) // You'll need to add this method to SharedViewModel
                                expandedAudioSource = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Audio Quality (Bitrate)",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                OutlinedButton(
                    onClick = { expandedAudioBitrate = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Bitrate: $selectedAudioBitrate")
                }
                DropdownMenu(
                    expanded = expandedAudioBitrate,
                    onDismissRequest = { expandedAudioBitrate = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    audioBitrates.forEach { bitrate ->
                        DropdownMenuItem(
                            text = { Text(bitrate) },
                            onClick = {
                                selectedAudioBitrate = bitrate
                                sharedViewModel.setAudioBitrate(bitrate) // You'll need to add this method to SharedViewModel
                                expandedAudioBitrate = false
                            }
                        )
                    }
                }
            }
        }
    }
}

data class VideoQuality(val label: String, val resolution: String, val qualityLevel: String)

@Composable
fun MediaRecorderSettings(sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    val availableVideoQualities = remember { getAvailableVideoQualities(context) }

    // Filter the predefined qualities based on what's available
    val videoQualities = remember(availableVideoQualities) {
        listOf(
            VideoQuality("SD 480p", "640x480", "sd"),
            VideoQuality("SD 576p", "720x576", "sd"),
            VideoQuality("HD 720p", "1280x720", "hd"),
            VideoQuality("HD 1080p", "1920x1080", "hd"),
            VideoQuality("QHD 1440p", "2560x1440", "qhd"),
            VideoQuality("UHD 4K", "3840x2160", "uhd"),
            VideoQuality("UHD 5K", "5120x2880", "uhd"),
            VideoQuality("UHD 8K", "7680x4320", "uhd"),
            // You can add even lower resolutions if your hardware supports and your use case requires it
            VideoQuality("LD 360p", "640x360", "ld"),
            VideoQuality("VLD 240p", "426x240", "vld")
        ).filter { predefinedQuality ->
            val resolutionParts = predefinedQuality.resolution.split("x")
            if (resolutionParts.size == 2) {
                try {
                    val width = resolutionParts[0].toInt()
                    val height = resolutionParts[1].toInt()
                    availableVideoQualities.any { availableSize ->
                        availableSize.width == width && availableSize.height == height
                    }
                } catch (e: NumberFormatException) {
                    false // Invalid resolution format
                }
            } else {
                false // Invalid resolution format
            }
        }.ifEmpty {
            // Fallback to the lowest available quality if no predefined matches,
            // or a sensible default if availableVideoQualities is also empty.
            availableVideoQualities.minByOrNull { it.width * it.height }
                ?.let { listOf(VideoQuality("Lowest Available", "${it.width}x${it.height}", "custom")) } ?: listOf(VideoQuality("Default (No Cam)", "640x480", "sd"))
        }
    }
    var selectedQuality by remember {
        mutableStateOf(
            videoQualities.find { it.resolution == sharedViewModel.getResolution() } ?: videoQualities.first()
        )
    }
    var zoomLevel by remember { mutableStateOf(sharedViewModel.getVideoZoom()) } // Assuming you have a getter for zoom
    var expandedQuality by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Media Recorder Preferences", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))

        // Video Quality Dropdown
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            OutlinedButton(
                onClick = { expandedQuality = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Video Quality: ${selectedQuality.label}")
            }
            DropdownMenu(
                expanded = expandedQuality,
                onDismissRequest = { expandedQuality = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                videoQualities.forEach { quality ->
                    DropdownMenuItem(
                        text = { Text("${quality.label} (${quality.resolution})") },
                        onClick = {
                            selectedQuality = quality
                            sharedViewModel.setResolution(quality.resolution)
                            // Optionally set bitrate based on quality or keep it separate
                            // sharedViewModel.setBitrate(getBitrateForQuality(quality.qualityLevel))
                            expandedQuality = false
                        }
                    )
                }
            }
        }

        // Screen Size (derived from selected quality)
        Text(
            "Screen Size: ${selectedQuality.resolution}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        )


        // Video Zoom Slider
        Text(
            "Video Zoom: ${zoomLevel.toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 4.dp)
        )
        Slider(
            value = zoomLevel,
            onValueChange = {
                zoomLevel = it
                sharedViewModel.setVideoZoom(it) // Assuming you have a setter for zoom
            },
            valueRange = 1f..100f,
            steps = 98, // (100 - 1) - 1 for discrete steps
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )

    }
}

private fun getAvailableVideoQualities(context: Context): List<Size> {
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val supportedSizes = mutableListOf<Size>()

    try {
        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            map?.getOutputSizes(MediaRecorder::class.java)?.let { sizes ->
                supportedSizes.addAll(sizes)
            }
        }
    } catch (e: Exception) {
        // Handle exceptions, e.g., camera access permission denied or no camera available
        e.printStackTrace()
    }

    // Filter out very small or impractical sizes and sort them
    return supportedSizes
        .filter { it.width >= 640 && it.height >= 480 } // Example filter
        .distinct() // Remove duplicates if cameras report same sizes
        .sortedWith(compareByDescending<Size> { it.width * it.height }.thenByDescending { it.width })
}

private fun getAvailableAudioSources(context: Context): List<AudioDeviceInfo> {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
    val devices = audioManager.getDevices(android.media.AudioManager.GET_DEVICES_INPUTS)
    return devices.filter {
        it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                it.type == AudioDeviceInfo.TYPE_BUILTIN_MIC ||
                it.type == AudioDeviceInfo.TYPE_FM_TUNER ||
                it.type == AudioDeviceInfo.TYPE_HDMI ||
                it.type == AudioDeviceInfo.TYPE_IP ||
                it.type == AudioDeviceInfo.TYPE_LINE_ANALOG ||
                it.type == AudioDeviceInfo.TYPE_LINE_DIGITAL ||
                it.type == AudioDeviceInfo.TYPE_TELEPHONY ||
                it.type == AudioDeviceInfo.TYPE_USB_ACCESSORY ||
                it.type == AudioDeviceInfo.TYPE_USB_DEVICE ||
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET
    }.distinctBy { it.id }
}





// Helper function (optional, if you want to set bitrate based on quality)
// fun getBitrateForQuality(qualityLevel: String): String {
// return when (qualityLevel) {
// "hd" -> "5000000" // Example bitrate for HD
// "uhd" -> "20000000" // Example bitrate for UHD
// else -> "5000000"
//    }
// }
