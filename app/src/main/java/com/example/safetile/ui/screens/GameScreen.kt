package com.example.safetile.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safetile.model.GameState
import com.example.safetile.model.TileData
import com.example.safetile.ui.components.PauseDialog
import com.example.safetile.ui.components.TileBoard
import com.example.safetile.ui.theme.DangerRed
import com.example.safetile.ui.theme.SafeGreen
import com.example.safetile.ui.theme.WarningAmber
import com.example.safetile.viewmodel.GameUiState
import java.util.Locale

@Composable
fun GameScreen(
    uiState: GameUiState,
    reducedMotion: Boolean,
    onTileClick: (TileData) -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    onExitClick: () -> Unit,
    onGameOverNavigate: () -> Unit
) {
    // Intercept back button to trigger pause
    BackHandler {
        if (uiState.gameState == GameState.PLAYING || uiState.gameState == GameState.MEMORIZE) {
            onPauseClick()
        } else {
            onExitClick()
        }
    }

    // Automatically navigate to ResultScreen when GameState reaches GAME_OVER
    LaunchedEffect(uiState.gameState) {
        if (uiState.gameState == GameState.GAME_OVER) {
            onGameOverNavigate()
        }
    }

    // Timer urgency pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (uiState.isTimerUrgent && !reducedMotion) 1.08f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Scaffold(
        topBar = {
            TopGameBar(
                round = uiState.round,
                score = uiState.score,
                streak = uiState.currentStreak,
                onPauseClick = onPauseClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Phase / Status Banner
                PhaseStatusCard(
                    gameState = uiState.gameState,
                    memorizeSeconds = uiState.memorizeRemainingSeconds,
                    roundSeconds = uiState.remainingTimeSeconds,
                    safeTapped = uiState.safeTilesTappedInRound,
                    safeTotal = uiState.roundConfig.safeTilesCount,
                    isUrgent = uiState.isTimerUrgent,
                    pulseScale = pulseScale,
                    transitionMessage = uiState.transitionMessage
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Responsive Board Area (uses weight to occupy max available screen height)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    TileBoard(
                        tiles = uiState.tiles,
                        rows = uiState.roundConfig.rows,
                        columns = uiState.roundConfig.columns,
                        onTileClick = onTileClick,
                        reducedMotion = reducedMotion
                    )
                }

                // Bottom Safe Tiles progress tracker
                BottomStatusIndicator(
                    safeTapped = uiState.safeTilesTappedInRound,
                    safeTotal = uiState.roundConfig.safeTilesCount,
                    dangerousTotal = uiState.roundConfig.dangerousCount,
                    gameState = uiState.gameState
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Pause Overlay Dialog
            if (uiState.gameState == GameState.PAUSED) {
                PauseDialog(
                    onResume = onResumeClick,
                    onRestart = onRestartClick,
                    onExit = onExitClick
                )
            }
        }
    }
}

@Composable
private fun TopGameBar(
    round: Int,
    score: Int,
    streak: Int,
    onPauseClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "ROUND $round",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (streak > 1) {
                Text(
                    text = "STREAK ×$streak",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarningAmber
                )
            }
        }

        Text(
            text = "SCORE $score",
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        IconButton(onClick = onPauseClick) {
            Text(
                text = "Ⅱ",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PhaseStatusCard(
    gameState: GameState,
    memorizeSeconds: Double,
    roundSeconds: Double,
    safeTapped: Int,
    safeTotal: Int,
    isUrgent: Boolean,
    pulseScale: Float,
    transitionMessage: String?
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                transitionMessage != null -> {
                    Text(
                        text = transitionMessage,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafeGreen,
                        textAlign = TextAlign.Center
                    )
                }

                gameState == GameState.MEMORIZE -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MEMORIZE DANGER:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = DangerRed
                        )
                        Text(
                            text = String.format(Locale.US, "%.1fs", memorizeSeconds),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = DangerRed
                        )
                    }
                }

                gameState == GameState.PLAYING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "SAFE:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$safeTapped / $safeTotal",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = SafeGreen
                            )
                        }

                        Box(
                            modifier = Modifier.scale(pulseScale)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "TIME:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUrgent) DangerRed else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1fs", roundSeconds),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isUrgent) DangerRed else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                else -> {
                    Text(
                        text = "SAFE TILE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomStatusIndicator(
    safeTapped: Int,
    safeTotal: Int,
    dangerousTotal: Int,
    gameState: GameState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (gameState == GameState.MEMORIZE) "Danger tiles: $dangerousTotal" else "Remaining: ${safeTotal - safeTapped}",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = if (gameState == GameState.MEMORIZE) "Remember danger positions" else "Tap all safe tiles",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
