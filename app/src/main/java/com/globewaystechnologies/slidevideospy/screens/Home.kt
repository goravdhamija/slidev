package com.globewaystechnologies.slidevideospy.screens


import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.globewaystechnologies.slidevideospy.ui.components.MyCameraAppWithViewModel
import com.globewaystechnologies.slidevideospy.viewmodel.SharedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.globewaystechnologies.slidevideospy.services.PinkService
import com.globewaystechnologies.slidevideospy.utils.isMyServiceRunning
import androidx.compose.ui.graphics.Color


@Composable
fun Home(sharedViewModel: SharedViewModel) {
    val scrollStateW = rememberScrollState()
    val context = LocalContext.current
    val serviceIntent = Intent(context.applicationContext, PinkService::class.java)
    val isServiceRunning = remember {
        mutableStateOf(isMyServiceRunning(context, PinkService::class.java))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(scrollStateW), // Make Column scrollable
            horizontalAlignment = Alignment.Start
        ) {
            MyCameraAppWithViewModel()
        }

        FloatingActionButton(
            onClick = {
                if (isServiceRunning.value) {
                    context.stopService(serviceIntent)
                } else {
                    context.startForegroundService(serviceIntent)
                }
                isServiceRunning.value = !isServiceRunning.value
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = if (isServiceRunning.value) Color.Red else Color(0xFF00695C)
        ) {
            Text(
                text = if (isServiceRunning.value) "STOP" else "START",
                color = Color.White
            )
        }
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


