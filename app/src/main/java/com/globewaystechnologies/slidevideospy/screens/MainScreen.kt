package com.globewaystechnologies.slidevideospy.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.globewaystechnologies.slidevideospy.ui.components.BottomNavigationBar
import com.globewaystechnologies.slidevideospy.ui.components.NavigationHost
import com.globewaystechnologies.slidevideospy.ui.theme.SlideVideoSPYTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun MainScreen(modifier: Modifier = Modifier) {

    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text("Bottom Navigation Demo")
            })
        },
        content = { padding ->
            Column(Modifier.padding(padding)) {
                NavigationHost(navController = navController)
            }
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    )

}

@Preview(showBackground = true, name = "Greeting Preview")
@Composable
fun MainActivityPreview() {

        MainScreen(modifier = Modifier)

}