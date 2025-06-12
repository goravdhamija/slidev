package com.globewaystechnologies.slidevideospy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.globewaystechnologies.slidevideospy.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandedTopAppBar(titleText: String = "Omni Vision",
                     isServiceRunning: Boolean,
                     onStartStopServiceClick: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.pi),
                    contentDescription = "App Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = titleText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },actions = {
            Button(
                onClick = onStartStopServiceClick,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isServiceRunning) Color.Red else Color(0xFF4A148C),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .padding(end = 16.dp)
                    .height(40.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = if (isServiceRunning) "STOP" else "START",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF00BCD4), // Cyan
                        Color(0xFFFF4081), // Pink
                        Color(0xFFFF9800)  // Orange
                    )
                )
            ),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}