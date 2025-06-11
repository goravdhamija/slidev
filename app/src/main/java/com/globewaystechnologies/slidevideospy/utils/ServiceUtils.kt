package com.globewaystechnologies.slidevideospy.utils

import android.content.Context
import android.content.Intent
import com.globewaystechnologies.slidevideospy.services.*


object ServiceUtils {

    fun getPinkServiceIntent(context: Context): Intent {
        return Intent(context, PinkService::class.java)
    }

    fun getSecondCameraServiceIntent(context: Context): Intent {
        return Intent(context, SecondCameraService::class.java)
    }

    fun getThirdCameraServiceIntent(context: Context): Intent {
        return Intent(context, ThirdCameraService::class.java)
    }

    fun getFourthCameraServiceIntent(context: Context): Intent {
        return Intent(context, FourthCameraServices::class.java)
    }
}


fun isMyServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
    @Suppress("DEPRECATION")
    for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}