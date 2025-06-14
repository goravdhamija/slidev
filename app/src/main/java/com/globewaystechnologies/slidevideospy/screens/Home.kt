package com.globewaystechnologies.slidevideospy.screens


import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.globewaystechnologies.slidevideospy.ui.components.MyCameraAppWithViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.globewaystechnologies.slidevideospy.services.PinkService
import com.globewaystechnologies.slidevideospy.viewmodel.CameraViewModel
import com.globewaystechnologies.slidevideospy.viewmodel.SharedViewModel


@Composable
fun Home(
    sharedViewModel: SharedViewModel,
    cameraViewModel: CameraViewModel
) {
    val scrollStateW = rememberScrollState()
    val context = LocalContext.current
    val serviceIntent = Intent(context.applicationContext, PinkService::class.java)
    val sharedServiceState by sharedViewModel.isServiceRunning.collectAsState()


    Box(modifier = Modifier.fillMaxSize()) {


        Column(
            modifier = Modifier
                .padding(2.dp)
                .fillMaxWidth()
                .verticalScroll(scrollStateW), // Make Column scrollable
            horizontalAlignment = Alignment.Start
        ) {
            MyCameraAppWithViewModel(cameraViewModel)
            Spacer(modifier = Modifier.height(54.dp))
        }



        FloatingActionButton(
            onClick = {
                if (sharedServiceState) {
                    context.stopService(serviceIntent)

                } else {

                    cameraViewModel.stopDualPreview()
                    context.startService(serviceIntent)
                }
                sharedViewModel.updateServiceRunning(!sharedServiceState)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp),
            containerColor = if (sharedServiceState) Color.Red else Color(0xFF4A148C)
        ) {
            Text(
                text = if (sharedServiceState) "STOP RECORDING" else "START BACKGROUND RECORDING",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }




    }

}


@Preview(showBackground = true, name = "Greeting Preview")
@Composable
fun HomeScreenPreview() {
//    val fakeViewModel = object : SharedViewModel() {
//        override val text = MutableStateFlow("Preview Text")
//    }
//    Home(fakeViewModel)
}


