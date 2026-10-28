package com.example.safetile.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safetile.model.TileData
import com.example.safetile.model.TileState
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun TileView(
    tile: TileData,
    tileSize: Dp,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    reducedMotion: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1f) }
    val shakeOffset = remember { Animatable(0f) }

    // Trigger shake animation when incorrectly tapped
    LaunchedEffect(tile.isWronglyTapped) {
        if (tile.isWronglyTapped && !reducedMotion) {
            repeat(3) {
                shakeOffset.animateTo(12f, tween(35, easing = LinearEasing))
                shakeOffset.animateTo(-12f, tween(35, easing = LinearEasing))
            }
            shakeOffset.animateTo(0f, tween(35))
        }
    }

    // Determine colors and symbol based on tile state
    val (bgColor, borderColor, contentColor, symbol) = when (tile.state) {
        TileState.SAFE_REVEALED -> {
            val bg = if (isDarkTheme) SafeGreenContainerDark else SafeGreenContainerLight
            val border = if (isDarkTheme) SafeGreen else SafeGreenDark
            val content = if (isDarkTheme) SafeGreen else SafeGreenDark
            Quad(bg, border, content, "✓")
        }
        TileState.DANGER_REVEALED -> {
            val bg = if (tile.isWronglyTapped) {
                if (isDarkTheme) DangerRedDark else DangerRed
            } else {
                if (isDarkTheme) DangerRedContainerDark else DangerRedContainerLight
            }
            val border = if (isDarkTheme) DangerRed else DangerRedDark
            val content = if (tile.isWronglyTapped) Color.White else (if (isDarkTheme) DangerRed else DangerRedDark)
            Quad(bg, border, content, "!")
        }
        TileState.TAPPED_SAFE -> {
            val bg = if (isDarkTheme) SafeGreenContainerDark else SafeGreenContainerLight
            val border = if (isDarkTheme) SafeGreen else SafeGreenDark
            val content = if (isDarkTheme) SafeGreen else SafeGreenDark
            Quad(bg, border, content, "✓")
        }
        TileState.SAFE_HIDDEN, TileState.DANGER_HIDDEN -> {
            val bg = if (isDarkTheme) HiddenTileBgDark else HiddenTileBgLight
            val border = if (isDarkTheme) HiddenTileBorderDark else HiddenTileBorderLight
            val content = if (isDarkTheme) HiddenTileTextDark else HiddenTileTextLight
            Quad(bg, border, content, "?")
        }
    }

    val animatedBg by animateColorAsState(
        targetValue = bgColor,
        animationSpec = tween(if (reducedMotion) 0 else 180),
        label = "tile_bg"
    )

    val animatedBorder by animateColorAsState(
        targetValue = borderColor,
        animationSpec = tween(if (reducedMotion) 0 else 180),
        label = "tile_border"
    )

    val cornerRadius = (tileSize * 0.20f).coerceIn(8.dp, 18.dp)
    val borderWidth = if (tile.isWronglyTapped) 3.dp else 2.dp
    val fontSize = (tileSize.value * 0.42f).coerceIn(16f, 32f).sp

    Box(
        modifier = modifier
            .size(tileSize)
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .scale(scaleAnim.value)
            .clip(RoundedCornerShape(cornerRadius))
            .background(animatedBg)
            .border(borderWidth, animatedBorder, RoundedCornerShape(cornerRadius))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !tile.tapped
            ) {
                if (!reducedMotion) {
                    coroutineScope.launch {
                        scaleAnim.animateTo(0.90f, tween(60, easing = FastOutSlowInEasing))
                        scaleAnim.animateTo(1f, tween(100, easing = FastOutSlowInEasing))
                    }
                }
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = contentColor,
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
