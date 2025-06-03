package com.globewaystechnologies.slidevideospy

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.globewaystechnologies.slidevideospy.ui.theme.SlideVideoSPYTheme



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

        enableEdgeToEdge()
        setContent {

            SlideVideoSPYTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        FilledButtonStartForground {

                            startForegroundService(serviceIntent)

                        }

                        FilledButtonStopForground {

                            stopService(serviceIntent)

                        }
                    }
                }
            }
        }

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS,  Manifest.permission.FOREGROUND_SERVICE_CAMERA, Manifest.permission.CAMERA, Manifest.permission.FOREGROUND_SERVICE_MICROPHONE, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION),200)


    }
}


@Composable
fun FilledButtonStartForground(onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
        Text("Start Foreground")
    }
}

@Composable
fun FilledButtonStopForground(onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Red,
            contentColor = Color.Black)
    ) {
        Text("Stop Foreground")
    }
}



