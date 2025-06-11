package com.globewaystechnologies.slidevideospy.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PhotoLibrary
import com.globewaystechnologies.slidevideospy.viewmodel.SharedViewModel

@Composable
fun Gallery(sharedViewModel: SharedViewModel) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = Icons.Filled.PhotoLibrary,
            contentDescription = "contacts",
            tint = Color.Magenta,
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.Center)
        )
    }

}
