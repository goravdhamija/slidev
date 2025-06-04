package com.globewaystechnologies.slidevideospy


import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat

import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Random
import kotlin.concurrent.thread

import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import android.view.TextureView
import androidx.annotation.RequiresPermission

class PinkService : Service() {


    private var allowRebind: Boolean = false
    private var serviceRunningCurrently: Boolean = true
    private lateinit var cameraManager: CameraManager
    private lateinit var backgroundHandlerThread: HandlerThread
    private lateinit var backgroundHandler: Handler

    private lateinit var backgroundHandlerThread2: HandlerThread
    private lateinit var backgroundHandler2: Handler

    private val binder: PinkServiceBinder by lazy {
        PinkServiceBinder()
    }

    // Random number generator.
    private val mGenerator = Random()

    val randomNumber: Int
        get() = mGenerator.nextInt(100)


    override fun onBind(intent: Intent): IBinder {
       // TODO("Return the communication channel to the service.")
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        // All clients have unbound with unbindService()
        Log.d("PinkService", "On Unbind Called" )
        return allowRebind
    }

    override fun onRebind(intent: Intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("PinkService", "On Create Called" )
        Toast.makeText(this, "On Create Called", Toast.LENGTH_SHORT).show()
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("PinkService", "On Start Command" )
        Toast.makeText(this, "On Start", Toast.LENGTH_SHORT).show()

        Log.d("PinkService", "Outer Threade ${Thread.currentThread().name}")
        serviceRunningCurrently = true

        thread(start = true){
            while (serviceRunningCurrently) {
                Log.d("PinkService", "Logging Message General Threade ${Thread.currentThread().name}")
                Thread.sleep(1000)

            }
        }

        var job = GlobalScope.launch(Dispatchers.Main) {

            while (serviceRunningCurrently) {
                Log.d("PinkService", "Logging Message Globalscope Coroutine Thread ${Thread.currentThread().name}")
                delay(1000L)

                withContext(Dispatchers.Main){
                    Log.d("PinkService", "Logging Message Globalscope Coroutine Thread--2-- ${Thread.currentThread().name}")
                }

            }

        }

        startLoggerForegroundServices()

        return super.onStartCommand(intent, flags, startId)

    }

    override fun onDestroy() {
        super.onDestroy()
        serviceRunningCurrently = false
        Log.d("PinkService", "On Destroy Called" )
        Toast.makeText(this, "On Destroy Called", Toast.LENGTH_SHORT).show()
    }


    @RequiresPermission(Manifest.permission.CAMERA)
    fun startLoggerForegroundServices(){
        createNotificationChannel()
        val notification = createNotification()
        Log.d("PinkService", "Foreground Service Started" )
        startForeground(
            111,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
                } else {
                    0
                }
            )
        startForeground(
            111,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            } else {
                0
            }
        )
        startForeground(
            111,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            } else {
                0
            }
        )
        startForeground(
            111,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            }
        )

//        startForeground(
//            111,
//            notification,
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING
//            } else {
//                0
//            }
//        )
        startForeground(
            111,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else {
                0
            }
        )



        cameraServicesPoint()





    }

    fun getPendingIntent(): PendingIntent{
        val serviceIntent = Intent(this, PinkService::class.java)
        return PendingIntent.getActivity(this,0,serviceIntent, PendingIntent.FLAG_IMMUTABLE)
    }

    fun createNotificationChannel(): NotificationChannel {
        val channel = NotificationChannel("ID","PINKSERVICE", NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java)
        notificationManager!!.createNotificationChannel(channel)
        return channel
    }

    fun createNotification():Notification{
            var notification = NotificationCompat.Builder(this, "ID")
                .setContentText("Foreground Pink Service Running")
                .setContentTitle("PinkServices")
                .setContentIntent(getPendingIntent())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .build()
            return notification
    }



    inner class PinkServiceBinder: Binder() {
        fun getService(): PinkService = this@PinkService
    }



    @RequiresPermission(Manifest.permission.CAMERA)
    fun cameraServicesPoint(){
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraIdList = cameraManager.cameraIdList

        for (cameraItem in cameraIdList) {
            var charactersticsCamera = cameraManager.getCameraCharacteristics(cameraItem)

            Log.d(
                "PinkServiceCamera",
                "Camera Facing. :${charactersticsCamera.get(CameraCharacteristics.LENS_FACING)}"
            )
        }

        startBackgroundThread("dipdip")
        cameraManager.openCamera(cameraIdList[0], cameraStateCallback,backgroundHandler)


        startBackgroundThread2("triptrip")
        cameraManager.openCamera(cameraIdList[1], cameraStateCallback2,backgroundHandler2)


    }



     val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Log.d("PinkServiceCamera", "Camera Opened 1")

        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            Log.d("PinkServiceCamera", "Camera Disconnected 1")
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            val errorMsg = when(error) {
                ERROR_CAMERA_DEVICE -> "Fatal (device)"
                ERROR_CAMERA_DISABLED -> "Device policy"
                ERROR_CAMERA_IN_USE -> "Camera in use"
                ERROR_CAMERA_SERVICE -> "Fatal (service)"
                ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                else -> "Unknown"
            }
            Log.e("PinkServiceCamera", "Error when trying to connect camera $errorMsg")
        }
    }


     val cameraStateCallback2 = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Log.d("PinkServiceCamera", "Camera Opened 2")
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            Log.d("PinkServiceCamera", "Camera Disconnected 2")
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            val errorMsg = when(error) {
                ERROR_CAMERA_DEVICE -> "Fatal (device)"
                ERROR_CAMERA_DISABLED -> "Device policy"
                ERROR_CAMERA_IN_USE -> "Camera in use"
                ERROR_CAMERA_SERVICE -> "Fatal (service)"
                ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                else -> "Unknown"
            }
            Log.e("PinkServiceCamera", "Error when trying to connect camera $errorMsg")
        }
    }


     fun startBackgroundThread(threadName:String) {
        backgroundHandlerThread = HandlerThread(threadName)
        backgroundHandlerThread.start()
        backgroundHandler = Handler(
            backgroundHandlerThread.looper)
    }

     fun stopBackgroundThread() {
        backgroundHandlerThread.quitSafely()
        backgroundHandlerThread.join()
    }


    fun startBackgroundThread2(threadName:String) {
        backgroundHandlerThread2 = HandlerThread(threadName)
        backgroundHandlerThread2.start()
        backgroundHandler2 = Handler(
            backgroundHandlerThread2.looper)
    }

    fun stopBackgroundThread2() {
        backgroundHandlerThread2.quitSafely()
        backgroundHandlerThread2.join()
    }


     val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            Log.d("PinkServiceCamera", "SurfaceTexture available")

        }
        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
            Log.d("PinkServiceCamera", "SurfaceTexture size changed")
        }

         override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
             Log.d("PinkServiceCamera", "SurfaceTexture destroyed")
             TODO("Not yet implemented")
         }

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {
            Log.d("PinkServiceCamera", "SurfaceTexture updated")

        }
    }



}