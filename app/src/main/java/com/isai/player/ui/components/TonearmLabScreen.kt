package com.isai.player.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isai.player.R
import com.isai.player.data.repository.ThemeMode
import com.isai.player.ui.theme.CardShape
import com.isai.player.ui.theme.IsaiTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TonearmLabScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDarkTheme by remember { mutableStateOf(true) }
    var showDebugOverlay by remember { mutableStateOf(true) }

    // Standalone stage measurements (360 x 360 dp)
    val density = LocalDensity.current
    val stageSizePx = with(density) { 360.dp.toPx() }
    val discCenter = Offset(stageSizePx * 0.45f, stageSizePx * 0.55f)
    val discRadius = stageSizePx * 0.36f

    val geometry = remember(discCenter, discRadius) {
        TonearmGeometry.forDisc(discCenter, discRadius)
    }

    var currentAngle by remember(geometry) { mutableFloatStateOf(geometry.alphaOuter) }
    var currentLift by remember { mutableFloatStateOf(0f) }

    val tonearmColors = if (isDarkTheme) TonearmColors.dark() else TonearmColors.light()

    IsaiTheme(themeMode = if (isDarkTheme) ThemeMode.DARK else ThemeMode.LIGHT) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Prompt A: Tonearm Lab") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.global_action_cancel),
                                tint = IsaiTheme.colors.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = IsaiTheme.colors.background,
                        titleContentColor = IsaiTheme.colors.onBackground
                    )
                )
            },
            containerColor = IsaiTheme.colors.background,
            modifier = modifier
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 360 dp square testing stage
                Box(
                    modifier = Modifier
                        .size(360.dp)
                        .background(IsaiTheme.colors.surface)
                ) {
                    // 1. Placeholder disc: black circle with center dot at geometry.center
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFF121216),
                            radius = discRadius,
                            center = discCenter
                        )
                        // Outer rim outline
                        drawCircle(
                            color = Color(0xFF2C2D35),
                            radius = discRadius,
                            center = discCenter,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        // Lighter center dot
                        drawCircle(
                            color = Color(0xFF888B96),
                            radius = 6.dp.toPx(),
                            center = discCenter
                        )
                    }

                    // 2. Tonearm Canvas layer
                    Tonearm(
                        geometry = geometry,
                        angleDeg = { currentAngle },
                        lift = { currentLift },
                        colors = tonearmColors,
                        modifier = Modifier.fillMaxSize()
                    )

                    // 3. Debug Overlay Layer
                    if (showDebugOverlay) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Disc outer and inner groove circles
                            drawCircle(
                                color = Color.Cyan.copy(alpha = 0.5f),
                                radius = geometry.rOuter,
                                center = discCenter,
                                style = Stroke(width = 1.dp.toPx())
                            )
                            drawCircle(
                                color = Color.Magenta.copy(alpha = 0.5f),
                                radius = geometry.rInner,
                                center = discCenter,
                                style = Stroke(width = 1.dp.toPx())
                            )

                            // Pivot point indicator
                            drawCircle(
                                color = Color.Red,
                                radius = 4.dp.toPx(),
                                center = geometry.pivot
                            )

                            // Stylus travel arc
                            val arcPath = Path()
                            val steps = 40
                            for (i in 0..steps) {
                                val t = i / steps.toFloat()
                                val a = geometry.alphaRest + t * (geometry.alphaInner - geometry.alphaRest)
                                val pos = geometry.stylusPosition(a)
                                if (i == 0) arcPath.moveTo(pos.x, pos.y) else arcPath.lineTo(pos.x, pos.y)
                            }
                            drawPath(
                                path = arcPath,
                                color = Color.Green.copy(alpha = 0.6f),
                                style = Stroke(width = 1.5.dp.toPx())
                            )

                            // Current Stylus tip dot
                            val currentTip = geometry.stylusPosition(currentAngle)
                            drawCircle(
                                color = Color.Yellow,
                                radius = 3.dp.toPx(),
                                center = currentTip
                            )

                            // 56 dp interaction hit area circle around headshell
                            drawCircle(
                                color = Color.Yellow.copy(alpha = 0.25f),
                                radius = 28.dp.toPx(),
                                center = currentTip,
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                    }
                }

                // Controls & Live Readouts
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Live stats card
                    Card(
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = IsaiTheme.colors.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            val progress = geometry.progressFromAngle(currentAngle)
                            val isOnRecord = geometry.isOnRecord(currentAngle)

                            Text(
                                text = String.format(
                                    Locale.US,
                                    "Angle: %.1f° | Progress: %.1f%% | On Record: %s",
                                    currentAngle,
                                    progress * 100f,
                                    if (isOnRecord) "YES" else "NO"
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isOnRecord) IsaiTheme.colors.accent else IsaiTheme.colors.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "α_rest: %.1f° | α_outer: %.1f° | α_inner: %.1f°",
                                    geometry.alphaRest,
                                    geometry.alphaOuter,
                                    geometry.alphaInner
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = IsaiTheme.colors.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Angle Slider (alphaRest .. alphaInner)
                    Text(
                        text = "Arm Angle: ${String.format(Locale.US, "%.1f°", currentAngle)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IsaiTheme.colors.onBackground
                    )
                    Slider(
                        value = currentAngle,
                        onValueChange = { currentAngle = it },
                        valueRange = geometry.alphaRest..geometry.alphaInner,
                        colors = SliderDefaults.colors(
                            thumbColor = IsaiTheme.colors.accent,
                            activeTrackColor = IsaiTheme.colors.accent
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Lift Slider (0 .. 1)
                    Text(
                        text = "Lift Height: ${String.format(Locale.US, "%.2f", currentLift)} (${if (currentLift == 0f) "On Disc" else "Parked"})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IsaiTheme.colors.onBackground
                    )
                    Slider(
                        value = currentLift,
                        onValueChange = { currentLift = it },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = IsaiTheme.colors.accent,
                            activeTrackColor = IsaiTheme.colors.accent
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Toggles: Theme and Debug Overlay
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dark Theme",
                            style = MaterialTheme.typography.bodyLarge,
                            color = IsaiTheme.colors.onBackground
                        )
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { isDarkTheme = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Debug Overlay (Grooves, Stylus Arc, Pivot)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = IsaiTheme.colors.onBackground
                        )
                        Switch(
                            checked = showDebugOverlay,
                            onCheckedChange = { showDebugOverlay = it }
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "TonearmLabScreen - Dark Theme")
@Composable
private fun TonearmLabScreenDarkPreview() {
    TonearmLabScreen(onBackClick = {})
}
