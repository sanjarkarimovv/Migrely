package com.migrely.app.designsystem.sky

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import com.migrely.app.designsystem.theme.MigrelyTheme

private const val RAIN_CYCLE_MILLIS = 1_600
private const val RAIN_STREAKS = 22

/**
 * Draws a sky from data.
 *
 * Decorations (stars, waves, clouds, lightning, rain) stay inside the top [decorationHeight] of the canvas, so text a
 * card places below it is never crossed, whatever height the font size gives the card. Nothing ever flashes: lightning
 * is a still, dim line and rain only moves downwards at a constant brightness (BRD EC-UX-07). With [animate] false
 * (reduce motion, EC-UX-08) the rain is drawn still.
 */
@Composable
fun SkyCanvas(
    appearance: SkyAppearance,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    decorationHeight: Dp = Dp.Unspecified
) {
    val rainPhase = rainPhase(animate = animate && appearance == SkyAppearance.Rain)
    Canvas(modifier = modifier) {
        drawRect(Brush.verticalGradient(SkyPalette.gradient(appearance)))
        val limit = if (decorationHeight.isSpecified) decorationHeight.toPx() else size.height
        val area = Size(size.width, minOf(size.height, limit))
        when (appearance) {
            SkyAppearance.Calm -> drawCalm(area)
            SkyAppearance.Hazy -> drawHaze(area)
            SkyAppearance.Cloudy -> drawCloud(
                area = area,
                center = Offset(area.width * 0.70f, area.height * 0.42f),
                color = SkyPalette.Cloud
            )

            SkyAppearance.Storm -> drawStorm(area)
            SkyAppearance.Rain -> drawRain(area, rainPhase.value)
        }
    }
}

@Composable
private fun rainPhase(animate: Boolean): State<Float> {
    if (!animate) return remember { mutableFloatStateOf(0f) }
    return rememberInfiniteTransition(label = "rain").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(RAIN_CYCLE_MILLIS, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "rainPhase"
    )
}

internal object SkyPalette {
    val Star = Color(0xFF9AA0A9)
    val Moon = Color(0xFFC8CCD2).copy(alpha = 0.82f)
    val Wave = Color(0xFF4A5468)
    val WaveFaint = Color(0xFF3D4558)
    val Cloud = Color(0xFF3B4253)
    val StormCloud = Color(0xFF2B303D)
    val RainCloudBack = Color(0xFF1C2130)
    val RainCloudFront = Color(0xFF222838)
    val RainStreak = Color(0xFF2E3546)
    val Lightning = Color(0xFFB8975A)
    val Outline = Color(0xFF0B0D10)

    fun gradient(appearance: SkyAppearance): List<Color> = when (appearance) {
        SkyAppearance.Calm -> listOf(Color(0xFF121A2E), Color(0xFF202945))
        SkyAppearance.Hazy -> listOf(Color(0xFF1A2132), Color(0xFF232A3A))
        SkyAppearance.Cloudy -> listOf(Color(0xFF1A1F2C), Color(0xFF20252F))
        SkyAppearance.Storm -> listOf(Color(0xFF11141B), Color(0xFF171A22))
        SkyAppearance.Rain -> listOf(Color(0xFF0C0F17), Color(0xFF131823))
    }
}

// Fractions of the decoration area, fixed so every sky of the same kind looks the same (no runtime noise).
private val STARS = listOf(
    Triple(0.13f, 0.20f, 1.2f),
    Triple(0.25f, 0.45f, 0.9f),
    Triple(0.38f, 0.15f, 1.1f),
    Triple(0.55f, 0.35f, 0.8f),
    Triple(0.66f, 0.12f, 1.0f),
    Triple(0.92f, 0.62f, 0.9f),
    Triple(0.10f, 0.72f, 0.8f),
    Triple(0.50f, 0.66f, 1.0f)
)

private fun unit(area: Size) = area.minDimension / 100f

private fun DrawScope.drawCalm(area: Size) {
    val u = unit(area)
    STARS.forEach { (x, y, r) ->
        drawCircle(
            SkyPalette.Star,
            radius = r * u,
            center = Offset(area.width * x, area.height * y)
        )
    }
    val moonCenter = Offset(area.width * 0.82f, area.height * 0.34f)
    val moonRadius = 14f * u
    drawCircle(SkyPalette.Moon, radius = moonRadius, center = moonCenter)
    // The cut-out takes the sky's colour at the moon's height, so no darker disc shows around the crescent.
    val gradient = SkyPalette.gradient(SkyAppearance.Calm)
    drawCircle(
        lerp(gradient.first(), gradient.last(), moonCenter.y / size.height),
        radius = moonRadius * 0.88f,
        center = moonCenter + Offset(moonRadius * 0.42f, -moonRadius * 0.3f)
    )
}

private fun DrawScope.drawHaze(area: Size) {
    wave(
        area,
        yFraction = 0.36f,
        startFraction = 0.30f,
        endFraction = 0.90f,
        color = SkyPalette.Wave,
        width = 2.2f
    )
    wave(
        area,
        yFraction = 0.58f,
        startFraction = 0.45f,
        endFraction = 0.82f,
        color = SkyPalette.WaveFaint,
        width = 1.8f
    )
}

@Suppress("LongParameterList")
private fun DrawScope.wave(
    area: Size,
    yFraction: Float,
    startFraction: Float,
    endFraction: Float,
    color: Color,
    width: Float
) {
    val y = area.height * yFraction
    val start = area.width * startFraction
    val end = area.width * endFraction
    val step = (end - start) / 3f
    val amplitude = 3.5f * unit(area)
    val path = Path().apply {
        moveTo(start, y)
        quadraticTo(start + step * 0.5f, y - amplitude, start + step, y)
        quadraticTo(start + step * 1.5f, y + amplitude, start + step * 2f, y - amplitude * 0.2f)
        quadraticTo(start + step * 2.5f, y - amplitude, end, y)
    }
    drawPath(path, color, style = Stroke(width = width * unit(area), cap = StrokeCap.Round))
}

/** A cloud made of overlapping circles, outlined first so the outline only shows on the outer edge. */
internal fun DrawScope.drawCloud(area: Size, center: Offset, color: Color) {
    val u = unit(area) * 0.9f
    // (dx, dy, r) with slightly uneven radii for a hand-drawn look.
    val puffs =
        listOf(
            Triple(-17f, 4f, 13f),
            Triple(0f, -5f, 18.5f),
            Triple(18f, 2f, 14.5f),
            Triple(4f, 9f, 15.5f)
        )
    puffs.forEach { (dx, dy, r) ->
        drawCircle(
            SkyPalette.Outline,
            radius = r * u + 1.4f * unit(area),
            center =
            center + Offset(dx * u, dy * u)
        )
    }
    puffs.forEach { (dx, dy, r) ->
        drawCircle(color, radius = r * u, center = center + Offset(dx * u, dy * u))
    }
}

private fun DrawScope.drawStorm(area: Size) {
    drawCloud(
        area = area,
        center = Offset(area.width * 0.70f, area.height * 0.36f),
        color = SkyPalette.StormCloud
    )
    val u = unit(area)
    val x = area.width * 0.72f
    val y = area.height * 0.60f
    val bolt = Path().apply {
        moveTo(x, y)
        lineTo(x - 7f * u, y + 11f * u)
        lineTo(x, y + 11f * u)
        lineTo(x - 5f * u, y + 21f * u)
    }
    drawPath(
        bolt,
        SkyPalette.Lightning,
        style = Stroke(width = 3f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawRain(area: Size, phase: Float) {
    val w = area.width
    val h = area.height
    val u = unit(area)
    val cloudBottom = h * 0.45f
    val back = Path().apply {
        moveTo(0f, h * 0.24f)
        cubicTo(w * 0.15f, h * 0.03f, w * 0.35f, h * 0.03f, w * 0.5f, h * 0.17f)
        cubicTo(w * 0.65f, h * 0.02f, w * 0.9f, h * 0.06f, w, h * 0.21f)
        lineTo(w, cloudBottom)
        cubicTo(w * 0.7f, cloudBottom + 4f * u, w * 0.3f, cloudBottom - 3f * u, 0f, cloudBottom)
        close()
    }
    drawPath(back, SkyPalette.RainCloudBack)
    val front = Path().apply {
        moveTo(0f, h * 0.36f)
        cubicTo(w * 0.25f, h * 0.26f, w * 0.45f, h * 0.30f, w * 0.6f, h * 0.35f)
        cubicTo(w * 0.75f, h * 0.26f, w * 0.9f, h * 0.30f, w, h * 0.36f)
        lineTo(w, cloudBottom + 3f * u)
        cubicTo(w * 0.6f, cloudBottom + 7f * u, w * 0.4f, cloudBottom, 0f, cloudBottom + 4f * u)
        close()
    }
    drawPath(front, SkyPalette.RainCloudFront)
    val rainTop = cloudBottom + 4f * u
    val fall = h * 0.95f - rainTop
    if (fall <= 0f) return
    for (index in 0 until RAIN_STREAKS) {
        val x = w * ((index * 37 % 97) / 97f)
        val baseOffset = fall * ((index * 23 % 41) / 41f)
        val y = rainTop + ((baseOffset + phase * fall) % fall)
        drawLine(
            SkyPalette.RainStreak,
            start = Offset(x, y),
            end = Offset(x - 2f * u, y + 5f * u),
            strokeWidth = 1.4f * u,
            cap = StrokeCap.Round
        )
    }
}

@Preview(name = "Skies", widthDp = 360, heightDp = 1100)
@Composable
private fun SkyCanvasPreview() {
    MigrelyTheme {
        Column {
            SkyAppearance.entries.forEach { appearance ->
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(28.dp))
                ) {
                    SkyCanvas(
                        appearance = appearance,
                        modifier = Modifier.matchParentSize(),
                        animate = false,
                        decorationHeight = 96.dp
                    )
                }
            }
        }
    }
}
