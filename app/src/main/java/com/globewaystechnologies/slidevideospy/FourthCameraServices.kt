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
import android.hardware.camera2.CameraCaptureSession
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
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.PowerManager
import android.view.Surface
import android.view.TextureView
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import java.io.File

class FourthCameraServices : Service() {

    private var allowRebind: Boolean = true
    private var serviceRunningCurrently: Boolean = true
    private lateinit var cameraManager: CameraManager
    private lateinit var backgroundHandlerThread: HandlerThread
    private lateinit var backgroundHandler: Handler

    private var cameraDevice: CameraDevice? = null
    private var mediaRecorder: MediaRecorder? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private lateinit var cameraId: String

    private lateinit var wakeLock: PowerManager.WakeLock



    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SlideViewSpy4::CameraWakeLock")
        wakeLock.acquire()
    }

    private fun releaseWakeLock() {
        if (::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    private val binder: FourthCameraServiceBinder by lazy {
        FourthCameraServiceBinder()
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

    inner class FourthCameraServiceBinder: Binder() {
        fun getService(): FourthCameraServices = this@FourthCameraServices
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @RequiresPermission(Manifest.permission.CAMERA)
    override fun onCreate() {
        super.onCreate()

        acquireWakeLock()

        startForeground(129,
            createNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC or
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION or
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )

        Log.d("PinkService", "On Create Services" )

    }


    @RequiresPermission(Manifest.permission.CAMERA)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startCameraAndRecord()
        return START_STICKY

    }


    private fun createNotification(): Notification {
        val channelId = "audio_recording_channel_4"
        val channelName = "Audio Recording Channel 4"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Event Logging @")
            .setContentText("Logging active @")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }



    @RequiresPermission(Manifest.permission.CAMERA)
    private fun startCameraAndRecord() {

        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
//        cameraId = cameraManager.cameraIdList.first()
        cameraId = cameraManager.cameraIdList[3]
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



    @RequiresApi(Build.VERSION_CODES.S)
    private fun startRecordingSession() {
        requestAudioFocus()
        val mediaRecorder = MediaRecorder(this)
        this.mediaRecorder = mediaRecorder
        val publicDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            "MediaSync"
        )
        val folder4 = File(publicDir, "4")
        if (!folder4.exists()) folder4.mkdirs()
        val videoFile = File(folder4, "video_${System.currentTimeMillis()}.mp4")
        Log.d(
            "FourthServiceCamera:",
            "Camera Facing. :${publicDir}"
        )

        mediaRecorder.apply {
            setOrientationHint(270)
//            setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(videoFile.absolutePath)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(96000)
            setAudioSamplingRate(44100)
//            setAudioSamplingRate(16000)
//            setVideoSize(1920, 1080)
            setVideoSize(1280, 720)
            setVideoFrameRate(30)
            setVideoEncodingBitRate(3 * 1024 * 1024)
//            setVideoEncodingBitRate(8 * 1024 * 1024)
//            setVideoEncodingBitRate(10 * 1024 * 1024)
            prepare()
        }

        val surfaces = ArrayList<Surface>()
        val recorderSurface = mediaRecorder.surface
        surfaces.add(recorderSurface)

        cameraDevice?.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                cameraCaptureSession = session
                val captureRequest = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
                    addTarget(recorderSurface)
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
        Log.d("SecondCameraService", "On Destroy Called" )

        mediaRecorder?.apply {
            stop()
            reset()
            release()
        }
        cameraCaptureSession?.close()
        cameraDevice?.close()
        releaseWakeLock()
    }



}