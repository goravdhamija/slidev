package com.globewaystechnologies.slidevideospy.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.globewaystechnologies.slidevideospy.ui.components.BottomNavigationBar
import com.globewaystechnologies.slidevideospy.ui.components.BottomNavigationBarColored
import com.globewaystechnologies.slidevideospy.ui.components.BrandedTopAppBar
import com.globewaystechnologies.slidevideospy.ui.components.CustomTopBar
import com.globewaystechnologies.slidevideospy.ui.components.NavigationHost
import com.globewaystechnologies.slidevideospy.viewmodel.SharedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.unit.dp
import com.globewaystechnologies.slidevideospy.services.PinkService
import com.globewaystechnologies.slidevideospy.utils.isMyServiceRunning


@Composable
public fun MainScreen(modifier: Modifier = Modifier,sharedViewModel: SharedViewModel) {

    val text by sharedViewModel.text.collectAsState()
    val navController = rememberNavController()
    val context = LocalContext.current
    val serviceIntent = Intent(context.applicationContext, PinkService::class.java)
    val isServiceRunning = remember {
        mutableStateOf(isMyServiceRunning(context, PinkService::class.java))
    }

    Scaffold(
        topBar = {
            BrandedTopAppBar("Omni Multi Camera Service",
                isServiceRunning = isServiceRunning.value,
                onStartStopServiceClick = {
                    if (isServiceRunning.value) {
                        context.stopService(serviceIntent)
                    } else {
                        context.startForegroundService(serviceIntent)
                    }
                    isServiceRunning.value = !isServiceRunning.value
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
    val fakeViewModel = object : SharedViewModel() {
        override val text = MutableStateFlow("Preview Text")
    }
        MainScreen(modifier = Modifier,fakeViewModel)

}