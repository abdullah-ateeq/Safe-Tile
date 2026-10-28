package com.example.safetile.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safetile.ui.theme.DangerRed
import com.example.safetile.ui.theme.DangerRedContainerDark
import com.example.safetile.ui.theme.DangerRedContainerLight
import com.example.safetile.ui.theme.DangerRedDark
import com.example.safetile.ui.theme.SafeGreen
import com.example.safetile.ui.theme.SafeGreenContainerDark
import com.example.safetile.ui.theme.SafeGreenContainerLight
import com.example.safetile.ui.theme.SafeGreenDark
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // 3x3 layout pattern matching prompt:
    // ✓ ✓ !
    // ✓ ! ✓
    // ! ✓ ✓
    val splashPattern = remember {
        listOf(
            false, false, true,
            false, true, false,
            true, false, false
        )
    }

    val visibleTiles = remember { mutableStateListOf<Boolean>().apply { repeat(9) { add(false) } } }

    LaunchedEffect(Unit) {
        // Animate sequential tile reveals
        for (i in 0 until 9) {
            delay(80)
            visibleTiles[i] = true
        }
        delay(400) // Brief pause to appreciate the logo
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SAFE TILE",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 3x3 preview grid
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (row in 0 until 3) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0 until 3) {
                            val index = row * 3 + col
                            val isDanger = splashPattern[index]
                            val isVisible = visibleTiles[index]

                            val (bg, border, textClr, char) = if (isDanger) {
                                val b = if (isDark) DangerRedContainerDark else DangerRedContainerLight
                                val bo = if (isDark) DangerRed else DangerRedDark
                                val t = if (isDark) DangerRed else DangerRedDark
                                Quad(b, bo, t, "!")
                            } else {
                                val b = if (isDark) SafeGreenContainerDark else SafeGreenContainerLight
                                val bo = if (isDark) SafeGreen else SafeGreenDark
                                val t = if (isDark) SafeGreen else SafeGreenDark
                                Quad(b, bo, t, "✓")
                            }

                            AnimatedVisibility(
                                visible = isVisible,
                                enter = scaleIn(tween(150)) + fadeIn(tween(150))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bg)
                                        .border(2.dp, border, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char,
                                        color = textClr,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Choose carefully. Stay safe.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
