package com.globewaystechnologies.slidevideospy.utils

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioDeviceInfo
import android.media.MediaRecorder
import android.os.Build
import android.util.Size
import androidx.annotation.RequiresApi

object CameraUtils {

    /**
     * Checks if the device supports concurrent camera recording.
     * Available from Android R (API 30) onwards.
     */
    fun supportsConcurrentRecording(cameraManager: CameraManager): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                cameraManager.concurrentCameraIds.isNotEmpty()
    }

    /**
     * Returns a list of concurrent camera ID pairs.
     * Requires API level 30 (Android R).
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun getConcurrentCameraPairs(cameraManager: CameraManager): List<Set<String>> {
        return cameraManager.concurrentCameraIds.toList()
    }


    fun getAvailableAudioSources(context: Context): List<AudioDeviceInfo> {
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

    val audioBitrates = listOf("64 kbps", "96 kbps", "128 kbps", "192 kbps", "256 kbps", "320 kbps")


    fun getAvailableVideoQualities(context: Context): List<Size> {
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




}