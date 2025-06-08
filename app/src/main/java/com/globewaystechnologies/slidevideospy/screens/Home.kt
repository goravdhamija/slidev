package com.globewaystechnologies.slidevideospy.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.globewaystechnologies.slidevideospy.*

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView.ScaleType
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.globewaystechnologies.slidevideospy.ui.theme.SlideVideoSPYTheme
import android.hardware.camera2.CameraManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface

import android.os.Handler
import android.os.HandlerThread
import android.hardware.camera2.CameraAccessException
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService


import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import com.globewaystechnologies.slidevideospy.screens.Contacts
import com.globewaystechnologies.slidevideospy.screens.Favorites
import com.globewaystechnologies.slidevideospy.screens.Home

import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.core.content.ContextCompat.startForegroundService


@Composable
fun Home() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        val context = LocalContext.current
        val serviceIntent = Intent(context, PinkService::class.java)

//        Icon(
//            imageVector = Icons.Filled.Home,
//            contentDescription = "home",
//            tint = Color.Blue,
//            modifier = Modifier
//                .size(150.dp)
//                .align(Alignment.Center)
//        )


                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 20.dp, top = 90.dp, end = 20.dp, bottom = 20.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        FilledButtonStartForground {


                            context.startForegroundService(serviceIntent)
//


                        }

                        FilledButtonStopForground {

                            context.stopService(serviceIntent)
//

                        }



                    }
                }



    }

}
