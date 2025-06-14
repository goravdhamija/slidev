package com.globewaystechnologies.slidevideospy.screens

import android.Manifest
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Environment
import android.os.StatFs
import android.media.AudioDeviceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import android.provider.Settings as AndroidSettings
import androidx.compose.foundation.background
import android.media.MediaRecorder
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn // Keep this for the outer LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.LazyColumn // Keep this for the outer LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.globewaystechnologies.slidevideospy.viewmodel.SharedViewModel
import java.text.DecimalFormat
import okio.Path.Companion.toPath
import android.util.Size
import java.util.concurrent.TimeUnit

@Composable
fun Settings(sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
var cardSpace = 10.dp
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp) // Outer padding for the whole screen
    ) {
        item {
            Text("App Settings", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
        }

        // Permissions Section
        item { PermissionsSection(context) }
        item { Spacer(modifier = Modifier.height(cardSpace)) }
        // Audio Settings for Media Recorder
        item {
            SectionCard {
                AudioSettings(sharedViewModel)
            }
        }

        // Media Recorder Settings
        item { Spacer(modifier = Modifier.height(cardSpace)) }
        item { MediaRecorderSettings(sharedViewModel) }
        item { Spacer(modifier = Modifier.height(cardSpace)) }
        item { StorageInfoSection() }
        item { Spacer(modifier = Modifier.height(cardSpace)) }


        // Storage Location Settings
        item { StorageLocationSettings(sharedViewModel) }


        item { Spacer(modifier = Modifier.height(cardSpace)) }

        // Video Slot Settings
        item { VideoSlotSettings(sharedViewModel) }

        item { Spacer(modifier = Modifier.height(cardSpace)) }

        // App Lock Settings
        item { AppLockSettings(sharedViewModel = sharedViewModel, navController = rememberNavController()) }

        item { Spacer(modifier = Modifier.height(cardSpace)) }

        // Widget Preview Section
        item { WidgetPreviewSection(sharedViewModel) }

        item { Spacer(modifier = Modifier.height(cardSpace)) }
        // App Upgrade Section
        item { AppUpgradeSection(sharedViewModel) }

        item { Spacer(modifier = Modifier.height(cardSpace)) }

    }
}


@Composable
fun VideoSlotSettings(sharedViewModel: SharedViewModel) {
    val videoSlotOptions = listOf(
        "10 minutes" to 10 * 60 * 1000, // 10 minutes in milliseconds
        "15 minutes" to 15 * 60 * 1000, // 15 minutes
        "25 minutes" to 25 * 60 * 1000, // 25 minutes
        "30 minutes" to 30 * 60 * 1000, // 30 minutes
        "Complete in 1 slot (Long Video)" to 0 // 0 for continuous recording
    )

    var selectedSlotDurationMillis by remember {
        mutableStateOf(sharedViewModel.getVideoSlotDurationMillis() ?: videoSlotOptions.first().second)
    }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                "Video Recording Slots",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Choose the duration for each video segment. Selecting 'Complete in 1 slot' will record one long video, which might be more prone to corruption on some devices.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(videoSlotOptions.find { it.second == selectedSlotDurationMillis }?.first ?: "Select Slot Duration")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    videoSlotOptions.forEach { (label, durationMillis) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = {
                            selectedSlotDurationMillis = durationMillis
                            sharedViewModel.setVideoSlotDurationMillis(durationMillis.toLong())
                            expanded = false
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetPreviewSection(sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                "Widget Previews",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SimpleIconWidgetPreview {
                        // Action for Simple Icon Widget
                       // sharedViewModel.setWidgetStyle("simple_icon")
                        Toast.makeText(context, "Simple Icon Widget Selected", Toast.LENGTH_SHORT).show()
                    }
                }
                item {
                    DetailedInfoWidgetPreview {
                        // Action for Detailed Info Widget
                      //  sharedViewModel.setWidgetStyle("detailed_info")
                        Toast.makeText(context, "Detailed Info Widget Selected", Toast.LENGTH_SHORT).show()
                    }
                }
                item {
                    MinimalistWidgetPreview {
                        // Action for Minimalist Widget
                      //  sharedViewModel.setWidgetStyle("minimalist")
                        Toast.makeText(context, "Minimalist Widget Selected", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@Composable
fun AppUpgradeSection(sharedViewModel: SharedViewModel) {
    // Simulate trial period - replace with your actual logic
    val trialEndDate = remember { System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7) } // 7 days from now
    val currentTime = System.currentTimeMillis()
    val daysLeft = TimeUnit.MILLISECONDS.toDays(trialEndDate - currentTime).coerceAtLeast(0)
    val isTrialActive = currentTime < trialEndDate
    val isPaidUser = sharedViewModel.isPaidUser.collectAsState().value // Assuming you have this in ViewModel

    val context = LocalContext.current

    if (isPaidUser) {
        PaidUserThankYouCard()
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Upgrade Icon",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isTrialActive) "Unlock Premium Features!" else "Your Free Trial Has Ended",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (isTrialActive) {
                    Text(
                        text = "You have $daysLeft days left in your free trial.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Upgrade to continue enjoying all features without limitations.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        // Handle upgrade action - e.g., navigate to payment screen
                        Toast.makeText(context, "Upgrade Clicked!", Toast.LENGTH_SHORT).show()
                        // sharedViewModel.initiatePurchaseFlow() // Example ViewModel call
                    },
                    shape = RoundedCornerShape(50), // Pill shape
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(0.8f) // Make button slightly less than full width
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Lock Icon",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Upgrade to Pro", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Subscribe yearly to unlock all features!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun PaidUserThankYouCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD4EFDF)) // Light green
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.CheckCircle, "Paid User", tint = Color(0xFF28B463), modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text("Thank you for being a Pro user!", style = MaterialTheme.typography.titleMedium, color = Color(0xFF1D8348))
        }
    }
}
@Composable
fun SimpleIconWidgetPreview(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp) // Approximate width of a small widget
            .background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(16.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for a record icon (e.g., using an Icon composable)
            Text("REC", color = Color.White, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Record", fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun DetailedInfoWidgetPreview(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(180.dp) // Approximate width of a medium widget
            .background(Color.DarkGray.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .clickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.Red, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("R", color = Color.White, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Recording...", color = Color.White, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Video: 1080p", color = Color.LightGray, fontSize = 10.sp)
        Text("Audio: Enabled", color = Color.LightGray, fontSize = 10.sp)
        Text("Time: 00:05:32", color = Color.LightGray, fontSize = 10.sp)
    }
}

@Composable
fun MinimalistWidgetPreview(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp) // Approximate size of a very small widget
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f), CircleShape)
            .padding(12.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Placeholder for a simple status indicator or icon
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.Green, CircleShape) // Green for 'ready' or 'recording'
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Tap", fontSize = 8.sp, color = Color.White)
        }
    }
}

@Composable
fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        content()

    }
}

@Composable
fun StorageInfoSection() {
    val context = LocalContext.current
    val statFs = StatFs(Environment.getExternalStorageDirectory().path)
    val blockSize = statFs.blockSizeLong
    val totalBlocks = statFs.blockCountLong
    val availableBlocks = statFs.availableBlocksLong

    val totalSpace = totalBlocks * blockSize
    val availableSpace = availableBlocks * blockSize
    val usedSpace = totalSpace - availableSpace

    val percentageUsed = (usedSpace.toDouble() / totalSpace.toDouble() * 100).toFloat()

    fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Storage Information", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 20.dp.toPx()
                    drawArc(
                        color = Color.LightGray,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                    )
                    drawArc(
                        color = if (percentageUsed > 85) Color.Red else if (percentageUsed > 60) Color.Yellow else Color.Green,
                        startAngle = -90f, // Start from the top
                        sweepAngle = percentageUsed * 3.6f, // 360 degrees for 100%
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                    )
                }
                Text("${percentageUsed.toInt()}% Used", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Total Space: ${formatSize(totalSpace)}", style = MaterialTheme.typography.bodyMedium)
            Text("Used Space: ${formatSize(usedSpace)}", style = MaterialTheme.typography.bodyMedium)
            Text("Available Space: ${formatSize(availableSpace)}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun AppLockSettings(sharedViewModel: SharedViewModel, navController: androidx.navigation.NavController) {
    var isAppLockEnabled by remember { mutableStateOf(sharedViewModel.isAppLockEnabled()) }
    var showPatternSetupDialog by remember { mutableStateOf(false) }
    var showPatternConfirmationDialog by remember { mutableStateOf(false) }
    var currentPattern by remember { mutableStateOf(sharedViewModel.getAppLockPattern() ?: emptyList()) }
    var patternToConfirm by remember { mutableStateOf<List<Int>?>(null) }
    // Add states for pattern or password if you implement those

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text("App Lock", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        isAppLockEnabled = !isAppLockEnabled
                        sharedViewModel.setAppLockEnabled(isAppLockEnabled)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable App Lock", modifier = Modifier.weight(1f))
                Switch(checked = isAppLockEnabled, onCheckedChange = {
                    isAppLockEnabled = it
                    sharedViewModel.setAppLockEnabled(it)
                })
            }
            if (isAppLockEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (currentPattern.isEmpty()) {
                            showPatternSetupDialog = true
                        } else {
                            // If a pattern exists, ideally you'd verify it first
                            showPatternSetupDialog = true // Or navigate to a change pattern screen
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (currentPattern.isEmpty()) "Set Pattern" else "Change Pattern")
                }
            }
        }
    }

    if (showPatternSetupDialog) {
        PatternSetupDialog(
            onDismiss = { showPatternSetupDialog = false },
            isConfirming = false,
            onPatternSet = { pattern ->
                patternToConfirm = pattern
                showPatternSetupDialog = false
                showPatternConfirmationDialog = true
            }
        )
    }

    if (showPatternConfirmationDialog) {
        PatternSetupDialog(
            onDismiss = { showPatternConfirmationDialog = false },
            isConfirming = true,
            onPatternSet = { confirmedPattern ->
                if (patternToConfirm == confirmedPattern) {
                    sharedViewModel.setAppLockPattern(confirmedPattern)
                    currentPattern = confirmedPattern
                } // Else, show error or retry
                showPatternConfirmationDialog = false
            }
        )
    }
}

@Composable
fun PatternSetupDialog(onDismiss: () -> Unit, onPatternSet: (List<Int>) -> Unit) {
    // This is a placeholder for your actual pattern setup UI.
    // You would replace this with a composable that allows users to draw a pattern.
    // For simplicity, we'll just have a button to simulate setting a pattern.
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Pattern") },
        text = { Text("Implement your pattern drawing UI here.") },
        confirmButton = { Button(onClick = { onPatternSet(listOf(1, 2, 3, 6, 9, 8, 7, 4)); onDismiss() }) { Text("Set Dummy Pattern") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}


@Composable
fun PatternSetupDialog(
    onDismiss: () -> Unit,
    onPatternSet: (List<Int>) -> Unit,
    isConfirming: Boolean = false
) {
    var currentPattern by remember { mutableStateOf(listOf<Int>()) }
    var patternMessage by remember { mutableStateOf(if (isConfirming) "Confirm Pattern" else "Draw Pattern") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isConfirming) "Confirm Pattern" else "Set Pattern") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(patternMessage, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                PatternLockGrid(
                    currentPattern = currentPattern,
                    onDotSelected = { dot ->
                        if (!currentPattern.contains(dot)) {
                            currentPattern = currentPattern + dot
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentPattern.size >= 4) { // Minimum pattern length
                        onPatternSet(currentPattern)
                        onDismiss()
                    } else {
                        patternMessage = "Pattern must be at least 4 dots"
                    }
                }
            ) { Text("Confirm") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun PatternLockGrid(currentPattern: List<Int>, onDotSelected: (Int) -> Unit) {
    // A simple 3x3 grid for pattern lock
    // You can use androidx.compose.foundation.lazy.grid.LazyVerticalGrid for a more robust solution
    // For simplicity, this example uses nested Rows and Columns.

    Column {
        (0..2).forEach { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (1..3).forEach { colIndex ->
                    val dotNumber = rowIndex * 3 + colIndex
                    PatternDot(
                        isSelected = currentPattern.contains(dotNumber),
                        onClick = { onDotSelected(dotNumber) },
                        dotNumber = dotNumber
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp)) // Spacing between rows
        }
    }
}


@Composable
fun PatternDot(isSelected: Boolean, onClick: () -> Unit, dotNumber: Int) {
    Box(
        modifier = Modifier
            .size(60.dp) // Size of the dot
            .clickable(onClick = onClick)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                shape = RoundedCornerShape(50) // Circular shape
            )
            .padding(8.dp), // Padding inside the dot
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dotNumber.toString(),
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 16.sp
        )
    }
}



@Composable
fun PermissionsSection(context: Context) {



    val requiredPermissions = mapOf(
        Manifest.permission.POST_NOTIFICATIONS to "Post Notifications",
        Manifest.permission.CAMERA to "Camera Access",
        Manifest.permission.RECORD_AUDIO to "Record Audio",
        Manifest.permission.ACCESS_FINE_LOCATION to "Location Access",
    )

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White), // Added white background for better visibility
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)

    ) {
        // Changed from LazyColumn to Column as it's inside another LazyColumn item
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(8.dp) // Ensuring Column also has a white background
        ) {
            Text("Permissions", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))

            requiredPermissions.forEach { (permissionKey, permissionValue) ->
                val isGranted = ContextCompat.checkSelfPermission(context, permissionKey) == PackageManager.PERMISSION_GRANTED

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = permissionValue, modifier = Modifier.weight(1f))
                    Text(
                        text = if (isGranted) "Granted" else "Denied",
                        color = if (isGranted) Color.Green else Color.Red,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    if (!isGranted) {
                        Button(onClick = {
                            val intent = Intent(AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = android.net.Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }) {
                            Text("Enable")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AudioSettings(sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    var isAudioEnabled by remember { mutableStateOf(sharedViewModel.isAudioEnabled.value ?: false) }
    val audioBitrates = listOf("64 kbps", "96 kbps", "128 kbps", "192 kbps", "256 kbps", "320 kbps")
    var selectedAudioBitrate by remember { mutableStateOf(sharedViewModel.getAudioBitrate() ?: audioBitrates.first()) }
    var expandedAudioBitrate by remember { mutableStateOf(false) }

    val mediaRecorderAudioSources = remember {
        listOf(
            Pair(MediaRecorder.AudioSource.DEFAULT, "Default"),
            Pair(MediaRecorder.AudioSource.MIC, "Microphone"),
            Pair(MediaRecorder.AudioSource.VOICE_UPLINK, "Voice Uplink"),
            Pair(MediaRecorder.AudioSource.VOICE_DOWNLINK, "Voice Downlink"),
            Pair(MediaRecorder.AudioSource.VOICE_CALL, "Voice Call"),
            Pair(MediaRecorder.AudioSource.CAMCORDER, "Camcorder"),
            Pair(MediaRecorder.AudioSource.VOICE_RECOGNITION, "Voice Recognition"),
            Pair(MediaRecorder.AudioSource.VOICE_COMMUNICATION, "Voice Communication"),
            Pair(MediaRecorder.AudioSource.REMOTE_SUBMIX, "Remote Submix"),
            Pair(MediaRecorder.AudioSource.UNPROCESSED, "Unprocessed"),
            Pair(MediaRecorder.AudioSource.VOICE_PERFORMANCE, "Voice Performance")
        )
    }


    var selectedMediaRecorderAudioSource by remember {
        mutableStateOf(sharedViewModel.getMediaRecorderAudioSource() ?: mediaRecorderAudioSources.first())
    }
    var expandedMediaRecorderAudioSource by remember { mutableStateOf(false) }

    val audioSources = remember { getAvailableAudioSources(context) }
    var selectedAudioSource by remember {
        mutableStateOf(
            sharedViewModel.getAudioSource()?.let { id ->
                audioSources.find { it.id.toString() == id }
            } ?: audioSources.firstOrNull()
        )
    }
    var expandedAudioSource by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        fun getAudioSourceDisplayName(source: AudioDeviceInfo): String {
            val typeName = when (source.type) {
                AudioDeviceInfo.TYPE_BUILTIN_MIC -> "Built-in Mic"
                AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired Headset"
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth SCO"
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth A2DP"
                // Add more types as needed
                else -> "Unknown Source"
            }
            return "${source.productName} ($typeName)"
        }
        Text(
            "Audio Settings",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp) // Increased bottom padding
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable {
                    isAudioEnabled = !isAudioEnabled
                    sharedViewModel.setAudioEnabled(isAudioEnabled)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable Audio Recording", modifier = Modifier.weight(1f))
            Switch(checked = isAudioEnabled, onCheckedChange = {
                isAudioEnabled = it
                sharedViewModel.setAudioEnabled(it)
            })
        }

        if (isAudioEnabled) {
            Spacer(modifier = Modifier.height(16.dp))

            // MediaRecorder Audio Source Dropdown
            Text(
                "MediaRecorder Audio Source",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp) // Adjusted padding
            )
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                OutlinedButton(
                    onClick = { expandedMediaRecorderAudioSource = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(selectedMediaRecorderAudioSource?.second ?: "Unknown")
                }
                DropdownMenu(
                    expanded = expandedMediaRecorderAudioSource,
                    onDismissRequest = { expandedMediaRecorderAudioSource = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    mediaRecorderAudioSources.forEach { sourcePair ->
                        DropdownMenuItem(
                            text = { Text(sourcePair.second) },
                            onClick = {
                                selectedMediaRecorderAudioSource = sourcePair // Corrected assignment
                                sharedViewModel.setMediaRecorderAudioSource(sourcePair) // You'll need to add this method to SharedViewModel
                                expandedMediaRecorderAudioSource = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Audio Source",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp) // Adjusted padding
            )
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                OutlinedButton(
                    onClick = { expandedAudioSource = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    enabled = audioSources.isNotEmpty()
                ) {
                    Text(selectedAudioSource?.let { getAudioSourceDisplayName(it) } ?: "No audio sources available")
                }
                DropdownMenu(
                    expanded = expandedAudioSource && audioSources.isNotEmpty(),
                    onDismissRequest = { expandedAudioSource = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    audioSources.forEach { source -> // Use the descriptive name for the dropdown item
                        DropdownMenuItem(
                            text = { Text(source.productName.toString()) },
                            onClick = {
                                selectedAudioSource = source
                                sharedViewModel.setAudioSource(source.id.toString()) // You'll need to add this method to SharedViewModel
                                expandedAudioSource = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Audio Quality (Bitrate)",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp) // Adjusted padding
            )
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                OutlinedButton(
                    onClick = { expandedAudioBitrate = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(selectedAudioBitrate) // Display only the bitrate value
                }
                DropdownMenu(
                    expanded = expandedAudioBitrate,
                    onDismissRequest = { expandedAudioBitrate = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    audioBitrates.forEach { bitrate ->
                        DropdownMenuItem(
                            text = { Text(bitrate) },
                            onClick = {
                                selectedAudioBitrate = bitrate
                                sharedViewModel.setAudioBitrate(bitrate) // You'll need to add this method to SharedViewModel
                                expandedAudioBitrate = false
                            }
                        )
                    }
                }
            }
        }
    }
}

data class VideoQuality(val label: String, val resolution: String, val qualityLevel: String)

@Composable
fun MediaRecorderSettings(sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    val availableVideoQualities = remember { getAvailableVideoQualities(context) }

    // Filter the predefined qualities based on what's available
    val videoQualities = remember(availableVideoQualities) {
        listOf(
            VideoQuality("SD 480p", "640x480", "sd"),
            VideoQuality("SD 576p", "720x576", "sd"),
            VideoQuality("HD 720p", "1280x720", "hd"),
            VideoQuality("HD 1080p", "1920x1080", "hd"),
            VideoQuality("QHD 1440p", "2560x1440", "qhd"),
            VideoQuality("UHD 4K", "3840x2160", "uhd"),
            VideoQuality("UHD 5K", "5120x2880", "uhd"),
            VideoQuality("UHD 8K", "7680x4320", "uhd"),
            // You can add even lower resolutions if your hardware supports and your use case requires it
            VideoQuality("LD 360p", "640x360", "ld"),
            VideoQuality("VLD 240p", "426x240", "vld")
        ).filter { predefinedQuality ->
            val resolutionParts = predefinedQuality.resolution.split("x")
            if (resolutionParts.size == 2) {
                try {
                    val width = resolutionParts[0].toInt()
                    val height = resolutionParts[1].toInt()
                    availableVideoQualities.any { availableSize ->
                        availableSize.width == width && availableSize.height == height
                    }
                } catch (e: NumberFormatException) {
                    false // Invalid resolution format
                }
            } else {
                false // Invalid resolution format
            }
        }.ifEmpty {
            // Fallback to the lowest available quality if no predefined matches,
            // or a sensible default if availableVideoQualities is also empty.
            availableVideoQualities.minByOrNull { it.width * it.height }
                ?.let { listOf(VideoQuality("Lowest Available", "${it.width}x${it.height}", "custom")) } ?: listOf(VideoQuality("Default (No Cam)", "640x480", "sd"))
        }
    }
    var selectedQuality by remember {
        mutableStateOf(
            videoQualities.find { it.resolution == sharedViewModel.getResolution() } ?: videoQualities.first()
        )
    }
    var zoomLevel by remember { mutableStateOf(sharedViewModel.getVideoZoom()) } // Assuming you have a getter for zoom
    var expandedQuality by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Media Recorder Preferences", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp), // Add padding around the card
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Optional: Add elevation
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) { // Padding inside the card
                // Video Quality Dropdown
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            OutlinedButton(
                onClick = { expandedQuality = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Video Quality: ${selectedQuality.label}")
            }
            DropdownMenu(
                expanded = expandedQuality,
                onDismissRequest = { expandedQuality = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                videoQualities.forEach { quality ->
                    DropdownMenuItem(
                        text = { Text("${quality.label} (${quality.resolution})") },
                        onClick = {
                            selectedQuality = quality
                            sharedViewModel.setResolution(quality.resolution)
                            // Optionally set bitrate based on quality or keep it separate
                            // sharedViewModel.setBitrate(getBitrateForQuality(quality.qualityLevel))
                            expandedQuality = false
                        }
                    )
                }
            }
        }

        // Screen Size (derived from selected quality)
        Text(
            "Screen Size: ${selectedQuality.resolution}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        )


        // Video Zoom Slider
        Text(
            "Video Zoom: ${zoomLevel.toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 4.dp)
        )
        Slider(
            value = zoomLevel,
            onValueChange = {
                zoomLevel = it
                sharedViewModel.setVideoZoom(it) // Assuming you have a setter for zoom
            },
            valueRange = 1f..100f,
            steps = 98, // (100 - 1) - 1 for discrete steps
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )
            }
        }

    }
}

private fun getAvailableVideoQualities(context: Context): List<Size> {
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val supportedSizes = mutableListOf<Size>()

    try {
        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            map?.getOutputSizes(MediaRecorder::class.java)?.let { sizes ->
                supportedSizes.addAll(sizes)
            }
        }
    } catch (e: Exception) {
        // Handle exceptions, e.g., camera access permission denied or no camera available
        e.printStackTrace()
    }

    // Filter out very small or impractical sizes and sort them
    return supportedSizes
        .filter { it.width >= 640 && it.height >= 480 } // Example filter
        .distinct() // Remove duplicates if cameras report same sizes
        .sortedWith(compareByDescending<Size> { it.width * it.height }.thenByDescending { it.width })
}

private fun getAvailableAudioSources(context: Context): List<AudioDeviceInfo> {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
    val devices = audioManager.getDevices(android.media.AudioManager.GET_DEVICES_INPUTS)
    return devices.filter {
        it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                it.type == AudioDeviceInfo.TYPE_BUILTIN_MIC ||
                it.type == AudioDeviceInfo.TYPE_FM_TUNER ||
                it.type == AudioDeviceInfo.TYPE_HDMI ||
                it.type == AudioDeviceInfo.TYPE_IP ||
                it.type == AudioDeviceInfo.TYPE_LINE_ANALOG ||
                it.type == AudioDeviceInfo.TYPE_LINE_DIGITAL ||
                it.type == AudioDeviceInfo.TYPE_TELEPHONY ||
                it.type == AudioDeviceInfo.TYPE_USB_ACCESSORY ||
                it.type == AudioDeviceInfo.TYPE_USB_DEVICE ||
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET
    }.distinctBy { it.id }
}

@Composable
fun StorageLocationSettings(sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    var selectedStorageLocation by remember { mutableStateOf(sharedViewModel.getStorageLocation()) }
    val currentStoragePath = sharedViewModel.getVideoStoragePath(context) // Get the current path
    var expanded by remember { mutableStateOf(false) }

    val storageOptions = listOf(
        "Internal App Storage (mediaSync)" to "internal_app_media_sync",
        "External App Storage (mediaSync)" to "external_app_media_sync",
        "External Public Storage (mediaSync)" to "external_public_media_sync"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                "Storage Location for Recordings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                "Choose where to save recorded videos. The 'mediaSync' folder will be used in the selected directory.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(storageOptions.find { it.second == selectedStorageLocation }?.first ?: "Select Storage Location")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    storageOptions.forEach { (label, value) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                selectedStorageLocation = value
                                sharedViewModel.setStorageLocation(value)
                                expanded = false
                            }
                        )
                    }
                }
            }
            // Display the current storage path below the dropdown
            Text(
                text = "Current Path: /${currentStoragePath?.toPath() ?: "Not set"}/mediaSync",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}






// Helper function (optional, if you want to set bitrate based on quality)
// fun getBitrateForQuality(qualityLevel: String): String {
// return when (qualityLevel) {
// "hd" -> "5000000" // Example bitrate for HD
// "uhd" -> "20000000" // Example bitrate for UHD
// else -> "5000000"
//    }
// }
