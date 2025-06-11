package com.globewaystechnologies.slidevideospy.screens

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
import androidx.compose.ui.Modifier
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


@Composable
public fun MainScreen(modifier: Modifier = Modifier,sharedViewModel: SharedViewModel) {

    val text by sharedViewModel.text.collectAsState()
    val navController = rememberNavController()

    Scaffold(
        topBar = { BrandedTopAppBar("Omni Multi Camera Service") },
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