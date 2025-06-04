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
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import android.os.Handler
import android.os.HandlerThread
import android.hardware.camera2.CameraAccessException
import androidx.core.content.ContextCompat.getSystemService


class MainActivity : ComponentActivity() {


    lateinit var mService: PinkService
    var mBound: Boolean = false


    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            val binder = service as PinkService.PinkServiceBinder
            mService = binder.getService()
            mBound = true
            Log.d("PinkService", "On Service Connected" )


        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
            Log.d("PinkService", "On Service Disconnected" )
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
        super.onCreate(savedInstanceState)

        var cameraManager: CameraManager
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIdList = cameraManager.cameraIdList
        val cameraThread = HandlerThread("CameraThread").apply { start() }
        val cameraHandler = Handler(cameraThread.looper)
        val firstCamera = cameraIdList[0]

        cameraManager.getCameraCharacteristics(firstCamera)

       // cameraManager.openCamera(firstCamera)

//        cameraManager.openCamera(firstCamera, object : CameraDevice.StateCallback() {
//            override fun onOpened(camera: CameraDevice) {
////                cameraDevice = camera
////                createCameraPreviewSession()
//            }
//
//            override fun onDisconnected(camera: CameraDevice) {
//                camera.close()
//            }
//
//            override fun onError(camera: CameraDevice, error: Int) {
//                camera.close()
//            }
//        }, null)



        enableEdgeToEdge()
        setContent {

            SlideVideoSPYTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 20.dp, top = 60.dp, end = 20.dp, bottom = 20.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        FilledButtonStartForground {

                            startForegroundService(serviceIntent)

                        }

                        FilledButtonStopForground {

                            stopService(serviceIntent)

                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {

                            items(cameraIdList) { item ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(20.dp),
                                            modifier = Modifier
                                            .size(width = 150.dp, height = 700.dp)
                                    .border(1.dp, Color.Magenta)
                                ) {
                                    Text(
                                        text = "Camera No. :$item ",

                                    )
                                    Surface(
                                        modifier = Modifier
                                            .size(width = 145.dp, height = 200.dp)
                                            .border(1.dp, Color.Cyan)
                                    ) {

                                    }

                                        var charactersticsCamera = cameraManager.getCameraCharacteristics(item)
                                    var previewSize = charactersticsCamera.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.getOutputSizes(ImageFormat.JPEG).maxByOrNull { it.height * it.width }!!


                                    Text(
                                        text = "Camera Facing. :${charactersticsCamera.get(CameraCharacteristics.LENS_FACING)} "
                                        )
                                    Text(
                                        text = "Camera FlashInfo. :${charactersticsCamera.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)} "
                                    )
                                    Text(
                                        text = "Camera Facing Front. :${CameraCharacteristics.LENS_FACING_FRONT} "
                                    )
                                    Text(
                                        text = "Camera Preview Size. : WIDTH- ${previewSize.width} by HEIGHT- ${ previewSize.height} "
                                    )


                                }
                            }
                        }

                        CameraDashView(LocalContext.current)

                      //  CameraFirstView()

                      //  CameraSecondView()

                    }
                }
            }
        }

        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.FOREGROUND_SERVICE_CAMERA,
                Manifest.permission.CAMERA,
                Manifest.permission.FOREGROUND_SERVICE_MICROPHONE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION),
            200)


    }
}


@Composable
fun FilledButtonStartForground(onClick: () -> Unit) {

    var context = LocalContext.current

    Button(onClick = { onClick() }) {
        Text("Start Foreground Services With Notification")
    }
}

@Composable
fun FilledButtonStopForground(onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Red,
            contentColor = Color.Black)
    ) {
        Text("Stop Foreground Services With Notification Removal")
    }
}

@Composable
fun CameraFirstView() {
    var context = LocalContext.current
    var lifecycleOwner = LocalLifecycleOwner.current
    var cameraController = remember { LifecycleCameraController(context) }
    val camWidth = 100.dp
    val camHeight = 140.dp
    AndroidView(
        modifier = Modifier
            .width(camWidth)
            .height(camHeight)
            .border(1.dp, Color.Gray),
        factory = { context ->
            PreviewView(context).apply {
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT,MATCH_PARENT)
//                setBackgroundColor(Color.Black)
                scaleType = PreviewView.ScaleType.FIT_START
            }.also { previewView ->
                previewView.controller = cameraController
                cameraController.bindToLifecycle(lifecycleOwner)
            }
        }
    )

}



@Composable
fun CameraSecondView() {
    var context = LocalContext.current
    var lifecycleOwner = LocalLifecycleOwner.current
    var cameraController2 = remember { LifecycleCameraController(context) }
    val camWidth = 100.dp
    val camHeight = 140.dp
    AndroidView(
        modifier = Modifier
            .width(camWidth)
            .height(camHeight)
            .border(1.dp, Color.Gray),
        factory = { context ->
            PreviewView(context).apply {
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT,MATCH_PARENT)
//                setBackgroundColor(Color.Black)
                scaleType = PreviewView.ScaleType.FIT_START
            }.also { previewView ->
                previewView.controller = cameraController2
                cameraController2.bindToLifecycle(lifecycleOwner)
            }
        }
    )

}


@Composable
fun CameraDashView(context: Context) {
    var cameraManager: CameraManager
    cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraIdList = cameraManager.cameraIdList
    val cameraThread = HandlerThread("CameraThread").apply { start() }
    val cameraHandler = Handler(cameraThread.looper)
    val firstCamera = cameraIdList[0]

    var ch = cameraManager.getCameraCharacteristics(firstCamera)

    ch.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.getOutputSizes(ImageFormat.JPEG).maxByOrNull { it.height * it.width }!!

}



//fun startCameraSession() {
//    val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
//    if (cameraManager.cameraIdList.isEmpty()) {
//        // no cameras
//        return
//    }
//    val firstCamera = cameraIdList[0]
//    cameraManager.openCamera(firstCamera, object: CameraDevice.StateCallback() {
//        override fun onDisconnected(p0: CameraDevice) { }
//        override fun onError(p0: CameraDevice, p1: Int) { }
//
//        override fun onOpened(cameraDevice: CameraDevice) {
//            // use the camera
//            val cameraCharacteristics =    cameraManager.getCameraCharacteristics(cameraDevice.id)
//
//            cameraCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]?.let { streamConfigurationMap ->
//                streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888)?.let { yuvSizes ->
//                    val previewSize = yuvSizes.last()
//
//                }
//
//            }
//        }
//    }, Handler { true })
//}