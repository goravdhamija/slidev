package com.globewaystechnologies.slidevideospy.screens


import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
        val scrollStateW = rememberScrollState()


        val text by sharedViewModel.text.collectAsState()
        val context = LocalContext.current
        val serviceIntent = Intent(context.applicationContext, PinkService::class.java)



            Column(modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(scrollStateW), // Make Column scrollable
            horizontalAlignment = Alignment.Start
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


