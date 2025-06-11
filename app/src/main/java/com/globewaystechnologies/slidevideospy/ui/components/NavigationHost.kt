package com.globewaystechnologies.slidevideospy.ui.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.globewaystechnologies.slidevideospy.screens.Contacts
import com.globewaystechnologies.slidevideospy.screens.Favorites
import com.globewaystechnologies.slidevideospy.screens.Home
import com.globewaystechnologies.slidevideospy.viewmodel.SharedViewModel


sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("home")
    object Gallery : NavRoutes("gallery")
    object Settings : NavRoutes("settings")
}


@Composable
public fun NavigationHost(navController: NavHostController, sharedViewModel: SharedViewModel) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Home.route,
    ) {
        composable(NavRoutes.Home.route) {
            Home(sharedViewModel)
        }
        composable(NavRoutes.Gallery.route) {
            Contacts(sharedViewModel)
        }
        composable(NavRoutes.Settings.route) {
            Favorites(sharedViewModel)
        }
    }
}