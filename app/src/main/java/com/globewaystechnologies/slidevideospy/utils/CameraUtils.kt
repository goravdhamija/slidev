package com.globewaystechnologies.slidevideospy.utils

import android.hardware.camera2.CameraManager
import android.os.Build
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
}