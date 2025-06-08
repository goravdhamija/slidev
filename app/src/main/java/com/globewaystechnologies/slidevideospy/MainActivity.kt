package com.globewaystechnologies.slidevideospy

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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


class MainActivity : ComponentActivity() {


    lateinit var mService: PinkService
    var mBound: Boolean = false


    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            val binder = service as PinkService.PinkServiceBinder
            mService = binder.getService()
            mBound = true
            Log.d("PinkService", "On Service Connected")


        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
            Log.d("PinkService", "On Service Disconnected")
        }
    }

    override fun onStart() {
        super.onStart()


        /* var serviceIntent = Intent(this, PinkService::class.java).also { intent ->
              bindService(intent, connection, Context.BIND_AUTO_CREATE)
          }*/

        //  startForegroundService(serviceIntent)
    }

    override fun onStop() {
        super.onStop()
        /* unbindService(connection)
         mBound = false*/
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {

        val serviceIntent = Intent(this, PinkService::class.java)
        val serviceSecondCameraIntent = Intent(this, SecondCameraService::class.java)
        val serviceThirdCameraIntent = Intent(this, ThirdCameraService::class.java)
        val serviceFourthCameraIntent = Intent(this, FourthCameraServices::class.java)
        super.onCreate(savedInstanceState)

        var cameraManager: CameraManager
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIdList = cameraManager.cameraIdList



        enableEdgeToEdge()
        setContent {

            SlideVideoSPYTheme {


                MainScreen(modifier = Modifier)
//
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(start = 20.dp, top = 90.dp, end = 20.dp, bottom = 20.dp),
//                    contentAlignment = Alignment.TopStart
//                ) {
//                    Column(
//                        verticalArrangement = Arrangement.spacedBy(20.dp)
//                    ) {
//                        FilledButtonStartForground {
//
//                            Toast.makeText(
//                                this@MainActivity,
//                                "Supports Concurent Camera ${
//                                    supportsConcurrentRecording(cameraManager)
//                                }",
//                                Toast.LENGTH_LONG
//                            ).show()
//                            if (supportsConcurrentRecording(cameraManager)) {
//                                Log.d(
//                                    "PinkServiceCameraPairs",
//                                    "${getConcurrentCameraPairs(cameraManager)}"
//                                )
//                            }
//                            //
//
//                            startForegroundService(serviceIntent)
////                            startForegroundService(serviceSecondCameraIntent)
////                            startForegroundService(serviceThirdCameraIntent)
////                            startForegroundService(serviceFourthCameraIntent)
//
//
//                        }
//
//                        FilledButtonStopForground {
//
//                            stopService(serviceIntent)
////                            stopService(serviceSecondCameraIntent)
////                            stopService(serviceThirdCameraIntent)
////                            stopService(serviceFourthCameraIntent)
//
//                        }
//
//                        LazyRow(
//                            horizontalArrangement = Arrangement.spacedBy(20.dp)
//                        ) {
//
//                            items(cameraIdList) { item ->
//                                Column(
//                                    horizontalAlignment = Alignment.CenterHorizontally,
//                                    verticalArrangement = Arrangement.spacedBy(20.dp),
//                                    modifier = Modifier
//                                        .size(width = 150.dp, height = 540.dp)
//                                        .border(1.dp, Color.Magenta)
//                                ) {
//                                    Text(
//                                        text = "Camera No. :$item ",
//
//                                        )
//                                    Surface(
//                                        modifier = Modifier
//                                            .size(width = 145.dp, height = 200.dp)
//                                            .border(1.dp, Color.Cyan)
//                                    ) {
//
//                                    }
//
//                                    var charactersticsCamera =
//                                        cameraManager.getCameraCharacteristics(item)
//                                    var previewSize =
//                                        charactersticsCamera.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
//                                            .getOutputSizes(ImageFormat.JPEG)
//                                            .maxByOrNull { it.height * it.width }!!
//
//
//                                    Text(
//                                        text = "Camera Facing. :${
//                                            charactersticsCamera.get(
//                                                CameraCharacteristics.LENS_FACING
//                                            )
//                                        } "
//                                    )
//                                    Text(
//                                        text = "Camera FlashInfo. :${
//                                            charactersticsCamera.get(
//                                                CameraCharacteristics.FLASH_INFO_AVAILABLE
//                                            )
//                                        } "
//                                    )
//                                    Text(
//                                        text = "Camera Facing Front. :${CameraCharacteristics.LENS_FACING_FRONT} "
//                                    )
//                                    Text(
//                                        text = "Camera Preview Size. : WIDTH- ${previewSize.width} by HEIGHT- ${previewSize.height} "
//                                    )
//
//
//                                }
//                            }
//                        }
//
//
//                    }
//                }
            }
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.FOREGROUND_SERVICE_CAMERA,
                Manifest.permission.CAMERA,
                Manifest.permission.FOREGROUND_SERVICE_MICROPHONE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            200
        )

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, 1234) // Use a constant request code
        }


    }


}


public fun supportsConcurrentRecording(cameraManager: CameraManager): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            cameraManager.concurrentCameraIds.isNotEmpty()
}

@RequiresApi(Build.VERSION_CODES.R)
public fun getConcurrentCameraPairs(cameraManager: CameraManager): List<Set<String>> {
    return cameraManager.concurrentCameraIds.toList()
}

@Composable
public fun FilledButtonStartForground(onClick: () -> Unit) {

    Button(onClick = { onClick() }) {
        Text("Start Foreground Services With Notification")
    }
}

@Composable
public fun FilledButtonStopForground(onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Red,
            contentColor = Color.Black
        )
    ) {
        Text("Stop Foreground Services With Notification Removal")
    }
}


sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("home")
    object Contacts : NavRoutes("contacts")
    object Favorites : NavRoutes("favorites")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text("Bottom Navigation Demo")
            })
        },
        content = { padding ->
            Column(Modifier.padding(padding)) {
                NavigationHost(navController = navController)
            }
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    )

}


@Composable
public fun NavigationHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Home.route,
    ) {
        composable(NavRoutes.Home.route) {
            Home()
        }
        composable(NavRoutes.Contacts.route) {
            Contacts()
        }
        composable(NavRoutes.Favorites.route) {
            Favorites()
        }
    }
}


@Composable
public fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        NavBarItems.BarItems.forEach { navItem ->
            NavigationBarItem(
                selected = currentRoute == navItem.route,
                onClick = {
                    navController.navigate(navItem.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = navItem.image,
                        contentDescription = navItem.title
                    )
                },
                label = {
                    Text(text = navItem.title)
                },
            )
        }

    }
}



