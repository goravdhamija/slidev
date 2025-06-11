package com.globewaystechnologies.slidevideospy.services


import android.Manifest
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.util.Random
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.PowerManager
import android.provider.MediaStore
import android.view.Surface
import android.view.SurfaceView
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import java.io.File
import java.io.FileDescriptor
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import com.globewaystechnologies.slidevideospy.R
import com.globewaystechnologies.slidevideospy.data.SettingsRepository
import com.globewaystechnologies.slidevideospy.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class PinkService : Service() {


    private var allowRebind: Boolean = true
    private var serviceRunningCurrently: Boolean = true
    private lateinit var cameraManager: CameraManager
    private lateinit var backgroundHandlerThread: HandlerThread
    private lateinit var backgroundHandler: Handler
    private lateinit var recordingSurface: Surface
    private lateinit var settingsRepository: SettingsRepository
    var concurencyType:String =  "Single"

    var mainCameraID:Int =  0
    var secondaryCameraID:Int =  1


    private var cameraDevice: CameraDevice? = null
    private var mediaRecorder: MediaRecorder? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private lateinit var cameraId: String

    private lateinit var wakeLock: PowerManager.WakeLock
    var videoUri: Uri? = null
    var fileDescriptor: FileDescriptor? = null

    private lateinit var overlayView: View
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView2: FrameLayout
    private lateinit var surfaceView: SurfaceView


        private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SlideViewSpy1::CameraWakeLock")
        wakeLock.acquire()
    }

    private fun releaseWakeLock() {
        if (::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
        }
    }

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

    fun getPendingIntent(): PendingIntent{
        val serviceIntent = Intent(this, PinkService::class.java)
        return PendingIntent.getService(this,0,serviceIntent, PendingIntent.FLAG_IMMUTABLE)
    }

    inner class PinkServiceBinder: Binder() {
        fun getService(): PinkService = this@PinkService
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @RequiresPermission(Manifest.permission.CAMERA)
    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(applicationContext.dataStore)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        CoroutineScope(Dispatchers.IO).launch {
            settingsRepository.readSelectedCameraGroup().collect { savedGroup ->
                if (savedGroup.isNotEmpty()) {
                    val parts = savedGroup.split(",").map { it.trim() }
                    val type = parts[0]     // "single"
                    val val1 = parts[1]     // "0"
                    val val2 = parts[2]     // "1"
                    concurencyType = type.trim()


                    if (concurencyType == "single") {
                        mainCameraID = val2.toInt()
                    }
                    else if (concurencyType == "double") {


                        val itemsx = val2.removePrefix("{").removeSuffix("}")
                            .split(",")
                            .map { it.trim() }
                        mainCameraID = itemsx[0].toInt()


                        val val3 = parts[3]
                        Log.d("Camera Group 1", "${val3}" )
                        val itemsy = val3.removePrefix("{").removeSuffix("}")
                            .split(",")
                            .map { it.trim() }
                        secondaryCameraID = itemsy[0].toInt()


                    }
                }
            }
        }

        acquireWakeLock()
        // addOverlayWithCloseButton()
//        startOverlayView()

        Log.d("PinkService", "On Create Services" )

    }


    @RequiresPermission(Manifest.permission.CAMERA)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // showCameraOverlay()

        startForeground(123,
            createNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC or
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION or
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )


        startCameraAndRecord()


        return START_STICKY

    }


    private fun createNotification(): Notification {
        val channelId = "audio_recording_channel"
        val channelName = "Audio Recording"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Smart Session Active #")
            .setContentText("Current session is active #")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }



    @RequiresPermission(Manifest.permission.CAMERA)
    private fun startCameraAndRecord() {
                    cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
                    cameraId = cameraManager.cameraIdList[mainCameraID]
                    cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                        @RequiresApi(Build.VERSION_CODES.S)
                        override fun onOpened(camera: CameraDevice) {
                            cameraDevice = camera
                            startRecordingSession()
                        }

                        override fun onDisconnected(camera: CameraDevice) {
                            camera.close()
                        }

                        override fun onError(camera: CameraDevice, error: Int) {
                            camera.close()
                        }
                    }, null)







    }




    private fun startRecordingSession() {
        requestAudioFocus()

        videoUri = createMediaStoreVideoUri(this)
        val mediaRecorder = createMediaRecorder(this)
        this.mediaRecorder = mediaRecorder

        val publicDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            "MediaSync"
        )
        if (!publicDir.exists()) publicDir.mkdirs()
        // Define subdirectories "1" and "2"
        val folder1 = File(publicDir, "1")
        // Create subdirectories if they don't exist
        if (!folder1.exists()) folder1.mkdirs()
        val videoFile = File(folder1, "video_${System.currentTimeMillis()}.mp4")
        Log.d(
                "PinkServiceCamera:",
                "Camera Facing. :${publicDir}"
            )

        videoUri?.let {
            val fileDescriptor = contentResolver.openFileDescriptor(it, "w")?.fileDescriptor
            this.fileDescriptor = fileDescriptor
        }

        mediaRecorder.apply {
            setOrientationHint(270)
            setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(fileDescriptor)
           // setOutputFile(videoFile.absolutePath)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(96000)
            setAudioSamplingRate(44100)
            setVideoSize(1920, 1080)
            setVideoFrameRate(30)
            setVideoEncodingBitRate(3 * 1024 * 1024)
            prepare()
        }





//            mediaRecorder.apply {
//                setAudioSource(MediaRecorder.AudioSource.MIC)
//                setVideoSource(MediaRecorder.VideoSource.SURFACE)
//                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//                setOutputFile(fileDescriptor)
//                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
//                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//                setVideoEncodingBitRate(10000000)
//                setVideoFrameRate(30)
//                setVideoSize(1280, 720)
//                prepare()
//            }









        val surfaces = ArrayList<Surface>()
        val recorderSurface = mediaRecorder.surface
//        val previewSurface = surfaceView.holder.surface
        surfaces.add(recorderSurface)
//        surfaces.add(previewSurface)



        cameraDevice?.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                cameraCaptureSession = session
                val captureRequest = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
                    addTarget(recorderSurface)
//                    addTarget(previewSurface)
                }
                session.setRepeatingRequest(captureRequest.build(), null, null)
                mediaRecorder.start()
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                Log.e("VideoService", "Camera configuration failed")
            }
        }, null)
    }


    private fun requestAudioFocus() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        //.setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .build()
            audioManager.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d("RecordingService", "App removed from task manager")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceRunningCurrently = false
        Log.d("PinkService", "On Destroy Called" )

        mediaRecorder?.apply {
            stop()
            reset()
            release()
        }
        cameraCaptureSession?.close()
        cameraDevice?.close()
        releaseWakeLock()

        videoUri?.let {
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.IS_PENDING, 0)
            }
            contentResolver.update(it, contentValues, null, null)
        }

//        stopOverlayView()

    }



    fun createMediaStoreVideoUri(context: Context): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "video_${System.currentTimeMillis()}.mp4")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/MediaSync") // Custom subfolder in Movies
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        return resolver.insert(collection, contentValues)
    }


    @Suppress("DEPRECATION")
    fun createMediaRecorder(context: Context): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }



    private fun showCameraOverlay() {
        surfaceView = SurfaceView(this)
        surfaceView.holder.setFormat(PixelFormat.TRANSLUCENT)

        val params = WindowManager.LayoutParams(
            400, 400,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 100

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(surfaceView, params)
    }

    private fun addOverlayWithCloseButton() {
     //   windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Create parent layout
        overlayView2 = FrameLayout(this)

        // Create and configure the SurfaceView
        surfaceView = SurfaceView(this)
        surfaceView.holder.setFormat(PixelFormat.TRANSLUCENT)
        val surfaceLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        // Add close button
        val closeButton = Button(this).apply {
            text = "✕"
            setBackgroundColor(0x88FF0000.toInt()) // Semi-transparent red
            setOnClickListener {
                try {
                    windowManager.removeView(overlayView2)
//                    overlayView2.visibility = View.GONE
//                    overlayView2.visibility = View.VISIBLE
                    toggleOverlay(true)
                    //stopSelf()
                    Toast.makeText(this@PinkService, "Overlay Closed", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        val buttonParams = FrameLayout.LayoutParams(
            100, 100,
            Gravity.TOP or Gravity.END
        )
        buttonParams.setMargins(16, 16, 16, 16)

        // Add views to layout
        overlayView2.addView(surfaceView, surfaceLayoutParams)
        overlayView2.addView(closeButton, buttonParams)

        // WindowManager parameters
        val overlayParams = WindowManager.LayoutParams(
            600, 900,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        overlayParams.gravity = Gravity.TOP or Gravity.START
        overlayParams.x = 200
        overlayParams.y = 900

        windowManager.addView(overlayView2, overlayParams)
    }

    fun toggleOverlay(show: Boolean) {
        if (::overlayView2.isInitialized) {
            overlayView2.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    fun startOverlayView() {
        val overlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

         overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_view, null)

        windowManager.addView(overlayView, overlayParams)
    }

    private fun stopOverlayView() {


        try {


            if (::overlayView2.isInitialized && overlayView2.windowToken != null) {
                windowManager.removeView(overlayView2)
            }
            windowManager.removeView(overlayView)
            windowManager.removeView(surfaceView)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}