package com.globewaystechnologies.slidevideospy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow
import kotlin.math.sqrt

private const val GRID_SIZE = 3 // 3x3 grid
private const val DOT_RADIUS_DP = 12f
private const val DOT_SPACING_DP = 60f // Spacing between centers of dots
private const val HIT_RADIUS_DP = 24f // How close finger needs to be to a dot
private const val LINE_THICKNESS_DP = 6f

data class Dot(val id: Int, val x: Float, val y: Float, var isSelected: Boolean = false)

@Composable
fun PatternLockView(
    modifier: Modifier = Modifier,
    gridSize: Int = GRID_SIZE,
    dotRadius: Dp = DOT_RADIUS_DP.dp,
    hitRadius: Dp = HIT_RADIUS_DP.dp,
    lineThickness: Dp = LINE_THICKNESS_DP.dp,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    idleColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    errorColor: Color = MaterialTheme.colorScheme.error,
    onPatternComplete: (List<Int>) -> Unit,
    showError: Boolean = false,
    message: String? = null
) {
    var currentPath by remember { mutableStateOf<List<Dot>>(emptyList()) }
    var currentLineEnd by remember { mutableStateOf<Offset?>(null) }
    val dots = remember { mutableStateListOf<Dot>() }

    val canvasSize = (dotRadius * 2 * gridSize) + (DOT_SPACING_DP.dp * (gridSize - 1))

    val density = LocalDensity.current
    LaunchedEffect(key1 = gridSize) {
        dots.clear()

        val spacingPx = with(density) { DOT_SPACING_DP.dp.toPx() }
        val radiusPx = with(density) { dotRadius.toPx() }

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                dots.add(
                    Dot(
                        id = row * gridSize + col,
                        x = col * (radiusPx * 2 + spacingPx) + radiusPx,
                        y = row * (radiusPx * 2 + spacingPx) + radiusPx
                    )
                )
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (message != null) {
            Text(
                text = message,
                color = if (showError) errorColor else MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Canvas(
            modifier = Modifier
                .size(canvasSize)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPath = emptyList()
                            dots.forEach { it.isSelected = false }
                            val hitDot = dots.find { dot ->
                                distance(offset, Offset(dot.x, dot.y)) < hitRadius.toPx()
                            }
                            hitDot?.let {
                                it.isSelected = true
                                currentPath = currentPath + it
                                currentLineEnd = offset // Start line from center of first dot
                            }
                        },
                        onDrag = { change, _ ->
                            currentLineEnd = change.position
                            val hitDot = dots.find { dot ->
                                !dot.isSelected && distance(
                                    change.position,
                                    Offset(dot.x, dot.y)
                                ) < hitRadius.toPx()
                            }
                            hitDot?.let {
                                it.isSelected = true
                                currentPath = currentPath + it
                            }
                        },
                        onDragEnd = {
                            if (currentPath.isNotEmpty()) {
                                onPatternComplete(currentPath.map { it.id })
                            }
                            // Reset for next input unless an error state persists
                            if (!showError) {
                                currentPath = emptyList()
                                dots.forEach { it.isSelected = false }
                            }
                            currentLineEnd = null
                        },
                        onDragCancel = {
                            if (!showError) {
                                currentPath = emptyList()
                                dots.forEach { it.isSelected = false }
                            }
                            currentLineEnd = null
                        }
                    )
                }
        ) {
            // Draw lines
            if (currentPath.size >= 1) {
                for (i in 0 until currentPath.size - 1) {
                    drawLine(
                        color = if (showError) errorColor else selectedColor,
                        start = Offset(currentPath[i].x, currentPath[i].y),
                        end = Offset(currentPath[i + 1].x, currentPath[i + 1].y),
                        strokeWidth = lineThickness.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                // Draw line to current finger position
                currentLineEnd?.let { end ->
                    val lastSelectedDot = currentPath.last()
                    drawLine(
                        color = if (showError) errorColor else selectedColor,
                        start = Offset(lastSelectedDot.x, lastSelectedDot.y),
                        end = end,
                        strokeWidth = lineThickness.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Draw dots
            dots.forEach { dot ->
                drawCircle(
                    color = when {
                        showError && dot.isSelected -> errorColor
                        dot.isSelected -> selectedColor
                        else -> idleColor
                    },
                    radius = dotRadius.toPx(),
                    center = Offset(dot.x, dot.y)
                )
                // Optional: inner circle for selected state
                if (dot.isSelected && !showError) {
                    drawCircle(
                        color = selectedColor.copy(alpha = 0.5f), // Or a different shade
                        radius = dotRadius.toPx() / 2.5f,
                        center = Offset(dot.x, dot.y)
                    )
                }
            }
        }
    }
}

private fun distance(p1: Offset, p2: Offset): Float {
    return sqrt((p1.x - p2.x).pow(2) + (p1.y - p2.y).pow(2))
}