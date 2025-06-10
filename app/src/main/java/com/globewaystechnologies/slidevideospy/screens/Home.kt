package com.globewaystechnologies.slidevideospy.screens


import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.globewaystechnologies.slidevideospy.services.PinkService
import com.globewaystechnologies.slidevideospy.ui.components.MyCameraApp
import com.globewaystechnologies.slidevideospy.ui.components.MyCameraAppWithViewModel
import com.globewaystechnologies.slidevideospy.viewmodel.SharedViewModel
import kotlinx.coroutines.flow.MutableStateFlow


@Composable
fun Home(sharedViewModel: SharedViewModel) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val text by sharedViewModel.text.collectAsState()
        val context = LocalContext.current
        val serviceIntent = Intent(context.applicationContext, PinkService::class.java)

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

//                MyCameraApp()
                MyCameraAppWithViewModel()

            }
        }


    }

}


@Composable
fun FilledButtonStartForground(onClick: () -> Unit) {

    Button(onClick = { onClick() }) {
        Text("Start Foreground Services With Notification")
    }
}

@Composable
fun FilledButtonStopForground(onClick: () -> Unit) {
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




@Preview(showBackground = true, name = "Greeting Preview")
@Composable
fun HomeScreenPreview() {
    val fakeViewModel = object : SharedViewModel() {
        override val text = MutableStateFlow("Preview Text")
    }
        Home(fakeViewModel)
}


