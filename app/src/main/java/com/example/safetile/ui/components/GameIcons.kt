package com.example.safetile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ArrowBackIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 2.4.dp.toPx()
        val w = this.size.width
        val h = this.size.height

        // Horizontal line
        drawLine(
            color = tint,
            start = Offset(w * 0.2f, h * 0.5f),
            end = Offset(w * 0.85f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Arrow head
        val path = Path().apply {
            moveTo(w * 0.45f, h * 0.25f)
            lineTo(w * 0.2f, h * 0.5f)
            lineTo(w * 0.45f, h * 0.75f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun SettingsIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 2.dp.toPx()
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val outerRadius = this.size.minDimension * 0.38f
        val innerRadius = this.size.minDimension * 0.16f

        // Outer gear body
        drawCircle(
            color = tint,
            radius = outerRadius,
            center = center,
            style = Stroke(width = strokeWidth)
        )

        // Inner hub
        drawCircle(
            color = tint,
            radius = innerRadius,
            center = center,
            style = Stroke(width = strokeWidth)
        )

        // 6 gear notches
        for (i in 0 until 6) {
            val angle = Math.toRadians((i * 60.0))
            val startX = center.x + (outerRadius * 0.9f * Math.cos(angle)).toFloat()
            val startY = center.y + (outerRadius * 0.9f * Math.sin(angle)).toFloat()
            val endX = center.x + (outerRadius * 1.25f * Math.cos(angle)).toFloat()
            val endY = center.y + (outerRadius * 1.25f * Math.sin(angle)).toFloat()

            drawLine(
                color = tint,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = strokeWidth * 1.4f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun InfoIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 2.dp.toPx()
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = this.size.minDimension * 0.42f

        // Outer circle
        drawCircle(
            color = tint,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )

        // Top dot of 'i'
        drawCircle(
            color = tint,
            radius = strokeWidth * 0.8f,
            center = Offset(center.x, center.y - (radius * 0.45f))
        )

        // Stem of 'i'
        drawLine(
            color = tint,
            start = Offset(center.x, center.y - (radius * 0.15f)),
            end = Offset(center.x, center.y + (radius * 0.5f)),
            strokeWidth = strokeWidth * 1.2f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun PlayIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val path = Path().apply {
            moveTo(w * 0.28f, h * 0.2f)
            lineTo(w * 0.82f, h * 0.5f)
            lineTo(w * 0.28f, h * 0.8f)
            close()
        }
        drawPath(path = path, color = tint)
    }
}

@Composable
fun RefreshIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 2.dp.toPx()
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = this.size.minDimension * 0.38f

        // 270 degree arc
        drawArc(
            color = tint,
            startAngle = -45f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Arrow head on the arc end
        val arrowTip = Offset(center.x + radius, center.y - 1.dp.toPx())
        val path = Path().apply {
            moveTo(arrowTip.x - 3.dp.toPx(), arrowTip.y - 5.dp.toPx())
            lineTo(arrowTip.x + 3.dp.toPx(), arrowTip.y)
            lineTo(arrowTip.x - 5.dp.toPx(), arrowTip.y + 4.dp.toPx())
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun HomeIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 2.dp.toPx()
        val w = this.size.width
        val h = this.size.height

        val roof = Path().apply {
            moveTo(w * 0.15f, h * 0.5f)
            lineTo(w * 0.5f, h * 0.2f)
            lineTo(w * 0.85f, h * 0.5f)
        }
        drawPath(
            path = roof,
            color = tint,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val walls = Path().apply {
            moveTo(w * 0.25f, h * 0.48f)
            lineTo(w * 0.25f, h * 0.82f)
            lineTo(w * 0.75f, h * 0.82f)
            lineTo(w * 0.75f, h * 0.48f)
        }
        drawPath(
            path = walls,
            color = tint,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun CheckmarkIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 18.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 2.2.dp.toPx()
        val w = this.size.width
        val h = this.size.height

        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.52f)
            lineTo(w * 0.42f, h * 0.75f)
            lineTo(w * 0.82f, h * 0.25f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
