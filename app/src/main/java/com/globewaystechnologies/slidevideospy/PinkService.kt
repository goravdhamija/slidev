package com.globewaystechnologies.slidevideospy


import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
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

class PinkService : Service() {


    private var allowRebind: Boolean = false
    private var serviceRunningCurrently: Boolean = true

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

        startForeground(
            111,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING
            } else {
                0
            }
        )
        startForeground(
            111,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else {
                0
            }
        )
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

}