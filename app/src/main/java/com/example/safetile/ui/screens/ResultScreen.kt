package com.example.safetile.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safetile.model.GameStatistics
import com.example.safetile.model.SafeTileGameResult
import com.example.safetile.ui.components.HomeIcon
import com.example.safetile.ui.components.RefreshIcon
import com.example.safetile.ui.theme.DangerRed
import com.example.safetile.ui.theme.SafeGreen
import com.example.safetile.ui.theme.WarningAmber

@Composable
fun ResultScreen(
    result: SafeTileGameResult?,
    statistics: GameStatistics,
    isTimeout: Boolean,
    onTryAgainClicked: () -> Unit,
    onHomeClicked: () -> Unit
) {
    // Back navigates to Home
    BackHandler {
        onHomeClicked()
    }

    val scrollState = rememberScrollState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SAFE TILE",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isTimeout) "TIME'S UP" else "GAME OVER",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = if (isTimeout) WarningAmber else DangerRed
            )

            Text(
                text = if (isTimeout) "The round timer expired" else "You hit a dangerous tile",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )

            // New Record banners
            if (result != null && (result.isNewBestScore || result.isNewBestRound)) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (result.isNewBestScore) {
                        RecordBadge(label = "NEW BEST SCORE!")
                    }
                    if (result.isNewBestRound) {
                        RecordBadge(label = "NEW BEST ROUND!")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Score Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FINAL SCORE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = (result?.score ?: 0).toString(),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MiniStat(
                            title = "ROUND REACHED",
                            value = ((result?.roundsSurvived ?: 0) + 1).toString()
                        )
                        MiniStat(
                            title = "BEST STREAK",
                            value = (result?.bestStreak ?: 0).toString()
                        )
                        MiniStat(
                            title = "PERFECT ROUNDS",
                            value = (result?.perfectRounds ?: 0).toString()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Detailed Game Summary Card
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "GAME SUMMARY",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    SummaryRow(label = "Difficulty", value = result?.difficulty?.label ?: "Easy")
                    SummaryRow(label = "Rounds Survived", value = (result?.roundsSurvived ?: 0).toString())
                    SummaryRow(label = "Safe Tiles Tapped", value = (result?.safeTilesTapped ?: 0).toString())
                    SummaryRow(label = "Danger Tiles Avoided", value = (result?.dangerTilesAvoided ?: 0).toString())
                    SummaryRow(label = "Danger Hit", value = if (result?.dangerHit == true) "1" else "0")
                    SummaryRow(label = "Best Overall Score", value = statistics.bestScore.toString())
                    SummaryRow(label = "Best Overall Round", value = statistics.bestRound.toString())

                    if (result?.wrongTileIndex != null) {
                        SummaryRow(
                            label = "Danger Tile Hit",
                            value = "Tile #${result.wrongTileIndex + 1}"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Button(
                onClick = onTryAgainClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RefreshIcon(
                        tint = androidx.compose.ui.graphics.Color.White,
                        size = 20.dp
                    )
                    Text(
                        text = "TRY AGAIN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onHomeClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeIcon(
                        tint = MaterialTheme.colorScheme.onSurface,
                        size = 20.dp
                    )
                    Text(
                        text = "HOME",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun RecordBadge(label: String) {
    Box(
        modifier = Modifier
            .background(SafeGreen, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.ui.graphics.Color.White
        )
    }
}

@Composable
private fun MiniStat(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
