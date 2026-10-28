package com.example.safetile.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.example.safetile.model.TileData

@Composable
fun TileBoard(
    tiles: List<TileData>,
    rows: Int,
    columns: Int,
    onTileClick: (TileData) -> Unit,
    reducedMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth - 24.dp
        val availableHeight = maxHeight - 24.dp
        val maxBoardDimension = min(availableWidth, availableHeight)

        val spacing = when {
            columns >= 7 -> 4.dp
            columns >= 6 -> 6.dp
            else -> 8.dp
        }

        val totalSpacingWidth = spacing * (columns - 1)
        val tileSize = (maxBoardDimension - totalSpacingWidth) / columns
        val boardActualWidth = (tileSize * columns) + totalSpacingWidth
        val boardActualHeight = (tileSize * rows) + (spacing * (rows - 1))

        Box(
            modifier = Modifier.size(boardActualWidth, boardActualHeight),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (r in 0 until rows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (c in 0 until columns) {
                            val index = r * columns + c
                            val tile = tiles.getOrNull(index)
                            if (tile != null) {
                                TileView(
                                    tile = tile,
                                    tileSize = tileSize,
                                    isDarkTheme = isDark,
                                    reducedMotion = reducedMotion,
                                    onClick = { onTileClick(tile) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
