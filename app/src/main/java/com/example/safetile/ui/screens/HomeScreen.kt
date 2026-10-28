package com.example.safetile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safetile.model.Difficulty
import com.example.safetile.model.GameStatistics
import com.example.safetile.ui.components.InfoIcon
import com.example.safetile.ui.components.PlayIcon
import com.example.safetile.ui.components.SettingsIcon
import com.example.safetile.ui.theme.DangerRed
import com.example.safetile.ui.theme.DangerRedContainerDark
import com.example.safetile.ui.theme.DangerRedContainerLight
import com.example.safetile.ui.theme.DangerRedDark
import com.example.safetile.ui.theme.HiddenTileBgDark
import com.example.safetile.ui.theme.HiddenTileBgLight
import com.example.safetile.ui.theme.HiddenTileBorderDark
import com.example.safetile.ui.theme.HiddenTileBorderLight
import com.example.safetile.ui.theme.HiddenTileTextDark
import com.example.safetile.ui.theme.HiddenTileTextLight
import com.example.safetile.ui.theme.SafeGreen
import com.example.safetile.ui.theme.SafeGreenContainerDark
import com.example.safetile.ui.theme.SafeGreenContainerLight
import com.example.safetile.ui.theme.SafeGreenDark

@Composable
fun HomeScreen(
    selectedDifficulty: Difficulty,
    statistics: GameStatistics,
    onDifficultySelected: (Difficulty) -> Unit,
    onPlayClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onAboutClicked: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onAboutClicked) {
                    InfoIcon(
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onSettingsClicked) {
                    SettingsIcon(
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header
            Text(
                text = "Safe Tile",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Tap safe tiles. Avoid danger.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Statistics Summary Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem(label = "BEST SCORE", value = statistics.bestScore.toString())
                    StatDivider()
                    StatItem(label = "BEST ROUND", value = statistics.bestRound.toString())
                    StatDivider()
                    StatItem(label = "BEST STREAK", value = statistics.bestStreak.toString())
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Difficulty Selector
            Text(
                text = "DIFFICULTY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Difficulty.entries.forEach { diff ->
                    val isSelected = diff == selectedDifficulty
                    Button(
                        onClick = { onDifficultySelected(diff) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = if (isSelected) {
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        } else {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    ) {
                        Text(
                            text = diff.name,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary PLAY Button
            Button(
                onClick = onPlayClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlayIcon(
                        tint = Color.White,
                        size = 24.dp
                    )
                    Text(
                        text = "PLAY",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // HOW TO PLAY Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "HOW TO PLAY",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val rules = listOf(
                        "1. Watch the dangerous tiles (!)",
                        "2. Remember their positions",
                        "3. Tap only safe tiles (✓)",
                        "4. Danger positions change every round",
                        "5. Survive as many rounds as possible"
                    )

                    rules.forEach { rule ->
                        Text(
                            text = rule,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Visual demonstration diagram: SHOW -> THEN HIDE -> TAP SAFE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TutorialMiniGrid(
                            title = "SHOW",
                            symbols = listOf("✓", "!", "✓", "✓", "✓", "!", "!", "✓", "✓"),
                            isDangerList = listOf(false, true, false, false, false, true, true, false, false),
                            isDark = isDark
                        )

                        Text(
                            text = "→",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        TutorialMiniGrid(
                            title = "HIDE",
                            symbols = List(9) { "?" },
                            isDangerList = List(9) { false },
                            isDark = isDark
                        )

                        Text(
                            text = "→",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        TutorialMiniGrid(
                            title = "SAFE",
                            symbols = List(9) { "✓" },
                            isDangerList = List(9) { false },
                            isDark = isDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .height(32.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun TutorialMiniGrid(
    title: String,
    symbols: List<String>,
    isDangerList: List<Boolean>,
    isDark: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            for (r in 0 until 3) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    for (c in 0 until 3) {
                        val index = r * 3 + c
                        val sym = symbols[index]
                        val isDanger = isDangerList[index]

                        val (bg, border, textClr) = when {
                            isDanger -> {
                                val b = if (isDark) DangerRedContainerDark else DangerRedContainerLight
                                val bo = if (isDark) DangerRed else DangerRedDark
                                val t = if (isDark) DangerRed else DangerRedDark
                                Triple(b, bo, t)
                            }
                            sym == "✓" -> {
                                val b = if (isDark) SafeGreenContainerDark else SafeGreenContainerLight
                                val bo = if (isDark) SafeGreen else SafeGreenDark
                                val t = if (isDark) SafeGreen else SafeGreenDark
                                Triple(b, bo, t)
                            }
                            else -> {
                                val b = if (isDark) HiddenTileBgDark else HiddenTileBgLight
                                val bo = if (isDark) HiddenTileBorderDark else HiddenTileBorderLight
                                val t = if (isDark) HiddenTileTextDark else HiddenTileTextLight
                                Triple(b, bo, t)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(bg)
                                .border(1.dp, border, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sym,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = textClr
                            )
                        }
                    }
                }
            }
        }
    }
}
