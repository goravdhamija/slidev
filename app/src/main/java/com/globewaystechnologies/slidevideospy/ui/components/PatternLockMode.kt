package com.globewaystechnologies.slidevideospy.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.globewaystechnologies.slidevideospy.viewmodel.PatternLockManager
import kotlinx.coroutines.launch

enum class PatternLockMode {
    VERIFY,      // Verify existing pattern
    SET_INITIAL, // Set pattern for the first time
    SET_CONFIRM  // Confirm the new pattern
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternLockScreen(
    initialMode: PatternLockMode = PatternLockMode.VERIFY,
    onPatternVerified: () -> Unit, // Called when pattern is successfully verified
    onPatternSet: () -> Unit,      // Called when a new pattern is successfully set
    onCancel: (() -> Unit)? = null // Optional: For a cancel/back action
) {
    val context = LocalContext.current
    val patternLockManager = remember { PatternLockManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var currentMode by remember { mutableStateOf(initialMode) }
    var firstPatternAttempt by remember { mutableStateOf<List<Int>?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }
    var attempts by remember { mutableIntStateOf(0) } // For verify mode

    val title = when (currentMode) {
        PatternLockMode.VERIFY -> "Enter Pattern"
        PatternLockMode.SET_INITIAL -> "Draw new pattern"
        PatternLockMode.SET_CONFIRM -> "Confirm pattern"
    }

    LaunchedEffect(currentMode) { // Reset message/error on mode change
        message = when (currentMode) {
            PatternLockMode.VERIFY -> "Enter your pattern to unlock."
            PatternLockMode.SET_INITIAL -> "Draw an unlock pattern (at least 4 dots)."
            PatternLockMode.SET_CONFIRM -> "Draw the pattern again to confirm."
            else -> null
        }
        showError = false
        if (currentMode != PatternLockMode.VERIFY) attempts = 0
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PatternLockView(
                modifier = Modifier.padding(bottom = 32.dp),
                showError = showError,
                message = message,
                onPatternComplete = { pattern ->
                    showError = false // Reset error on new attempt
                    when (currentMode) {
                        PatternLockMode.VERIFY -> {
                            coroutineScope.launch {
                                if (patternLockManager.verifyPattern(pattern)) {
                                    onPatternVerified()
                                } else {
                                    attempts++
                                    message = "Incorrect pattern. Attempt ${attempts}."
                                    showError = true
                                    if (attempts >= 5) { // Max attempts
                                        message = "Too many incorrect attempts."
                                        // Potentially lock out for a while or trigger other action
                                    }
                                }
                            }
                        }

                        PatternLockMode.SET_INITIAL -> {
                            if (pattern.size < 4) {
                                message = "Pattern must be at least 4 dots long."
                                showError = true
                            } else {
                                firstPatternAttempt = pattern
                                currentMode = PatternLockMode.SET_CONFIRM
                                message = "Draw the pattern again to confirm."
                            }
                        }

                        PatternLockMode.SET_CONFIRM -> {
                            if (firstPatternAttempt == pattern) {
                                coroutineScope.launch {
                                    patternLockManager.savePattern(pattern)
                                    message = "Pattern set successfully!"
                                    onPatternSet()
                                }
                            } else {
                                message = "Patterns do not match. Try again."
                                showError = true
                                // Optionally go back to SET_INITIAL or allow retry in confirm
                                currentMode = PatternLockMode.SET_INITIAL
                                firstPatternAttempt = null // Reset first attempt
                            }
                        }
                    }
                }
            )

            if (onCancel != null && currentMode != PatternLockMode.VERIFY) { // Don't show cancel for verify if it's mandatory
                Button(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        }
    }
}