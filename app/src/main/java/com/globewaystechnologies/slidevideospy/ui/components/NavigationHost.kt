package com.globewaystechnologies.slidevideospy.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.globewaystechnologies.slidevideospy.screens.Gallery
import com.globewaystechnologies.slidevideospy.screens.Home
import com.globewaystechnologies.slidevideospy.screens.Settings
import com.globewaystechnologies.slidevideospy.viewmodel.CameraViewModel
import com.globewaystechnologies.slidevideospy.viewmodel.SharedViewModel


sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("home")
    object Gallery : NavRoutes("gallery")
    object Settings : NavRoutes("settings")
}


@Composable
public fun NavigationHost(
    navController: NavHostController,
    sharedViewModel: SharedViewModel = viewModel(),
    cameraViewModel: CameraViewModel
) {

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Home.route,
    ) {
        composable(NavRoutes.Home.route) {
            Home(sharedViewModel,cameraViewModel)
        }
        composable(NavRoutes.Gallery.route) {
            Gallery(sharedViewModel)
        }
        composable(NavRoutes.Settings.route) {
            Settings(sharedViewModel)
        }
    }
}