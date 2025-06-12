package com.globewaystechnologies.slidevideospy.screens

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.globewaystechnologies.slidevideospy.ui.components.BottomNavigationBarColored
import com.globewaystechnologies.slidevideospy.ui.components.BrandedTopAppBar
import com.globewaystechnologies.slidevideospy.ui.components.NavigationHost
import com.globewaystechnologies.slidevideospy.viewmodel.SharedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import com.globewaystechnologies.slidevideospy.services.PinkService
import com.globewaystechnologies.slidevideospy.utils.isMyServiceRunning
import com.globewaystechnologies.slidevideospy.viewmodel.CameraViewModel


@Composable
public fun MainScreen(
    modifier: Modifier = Modifier,
    sharedViewModel: SharedViewModel = viewModel()
) {

    val text by sharedViewModel.text.collectAsState()
    val navController = rememberNavController()
    val context = LocalContext.current
    val serviceIntent = Intent(context.applicationContext, PinkService::class.java)

    val sharedServiceState by sharedViewModel.isServiceRunning.collectAsState()

    Scaffold(
        topBar = {
            BrandedTopAppBar(
                "Omni Multi Camera Service",
                isServiceRunning = sharedServiceState,
                onStartStopServiceClick = {
                    if (sharedServiceState) {
                        context.stopService(serviceIntent)
                    } else {
                        context.startForegroundService(serviceIntent)
                    }
                    sharedViewModel.updateServiceRunning(!sharedServiceState)

                }
            )

        },
        content = { padding ->
            Column(
                Modifier.padding(padding)
            ) {
                NavigationHost(navController = navController, sharedViewModel = sharedViewModel)
            }
        },
        bottomBar = {
            BottomNavigationBarColored(navController = navController)
        }
    )


}


@Preview(showBackground = true, name = "Greeting Preview")
@Composable
fun MainActivityPreview() {
//    val fakeViewModel = object : SharedViewModel() {
//        override val text = MutableStateFlow("Preview Text")
//    }
//        MainScreen(modifier = Modifier,fakeViewModel)

}