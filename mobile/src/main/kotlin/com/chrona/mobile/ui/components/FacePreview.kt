package com.chrona.mobile.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.chrona.shared.FaceCategory
import com.chrona.shared.WatchFaceItem
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural live preview of each face, drawn with Canvas so the catalog
 * needs no bitmap assets and always shows the real current time.
 */
@Composable
fun FacePreview(face: WatchFaceItem, modifier: Modifier = Modifier, animate: Boolean = true) {
    val accent = Color(android.graphics.Color.parseColor(face.accentColorHex))
    val transition = rememberInfiniteTransition(label = "tick")
    val tick by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(60_000, easing = LinearEasing)), label = "sec")
    val cal = Calendar.getInstance()
    val h = cal.get(Calendar.HOUR); val m = cal.get(Calendar.MINUTE)
    val s = if (animate) (tick * 60f) else cal.get(Calendar.SECOND).toFloat()

    Box(
        modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(Color(0xFF1A1A2E), Color(0xFF07070C))))
    ) {
        Canvas(Modifier.matchParentSize()) {
            when (face.category) {
                FaceCategory.CLASSIC -> drawClassic(accent, h, m, s)
                FaceCategory.MINIMAL -> drawMinimal(accent, h, m)
                FaceCategory.SPORT -> drawSport(accent, h, m)
                FaceCategory.PHOTO -> drawPhoto(accent, h, m)
                FaceCategory.WEATHER -> drawWeather(accent, h, m)
                FaceCategory.INFO -> drawMinimal(accent, h, m)
            }
        }
    }
}

private fun DrawScope.hand(angleDeg: Float, length: Float, width: Float, color: Color) = rotate(angleDeg) {
    drawLine(color, center, Offset(center.x, center.y - length), width, StrokeCap.Round)
}

private fun DrawScope.drawClassic(accent: Color, h: Int, m: Int, s: Float) {
    val r = size.minDimension / 2
    // sunburst
    for (i in 0 until 60) rotate(i * 6f) {
        drawLine(if (i % 5 == 0) Color(0xFFEDEDF5) else Color(0x33FFFFFF), Offset(center.x, center.y - r * 0.94f), Offset(center.x, center.y - r * (if (i % 5 == 0) 0.84f else 0.9f)), if (i % 5 == 0) 3f else 1.5f, StrokeCap.Round)
    }
    drawCircle(accent.copy(alpha = 0.15f), r * 0.7f, center, style = Stroke(r * 0.02f))
    hand((h % 12 + m / 60f) * 30f, r * 0.5f, r * 0.06f, Color(0xFFF7F3EA))
    hand(m * 6f, r * 0.75f, r * 0.045f, Color(0xFFF7F3EA))
    hand(s * 6f, r * 0.82f, r * 0.015f, accent)
    drawCircle(accent, r * 0.045f, center)
}

private fun DrawScope.drawMinimal(accent: Color, h: Int, m: Int) {
    val paint = android.graphics.Paint().apply {
        color = Color(0xFFF7F3EA).toArgb(); textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
        textSize = size.minDimension * 0.36f; isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawText("%02d".format(if (h == 0) 12 else h), center.x, center.y - size.minDimension * 0.02f, paint)
    paint.color = accent.toArgb()
    drawContext.canvas.nativeCanvas.drawText("%02d".format(m), center.x, center.y + size.minDimension * 0.33f, paint)
    drawLine(accent.copy(alpha = 0.6f), Offset(size.width * 0.3f, center.y + size.height * 0.03f), Offset(size.width * 0.7f, center.y + size.height * 0.03f), 2f)
}

private fun DrawScope.drawSport(accent: Color, h: Int, m: Int) {
    val r = size.minDimension / 2
    val rings = listOf(accent to 0.72f, Color(0xFF5BE7C4) to 0.55f, Color(0xFF4CC9F0) to 0.38f)
    rings.forEachIndexed { i, (c, frac) ->
        val radius = r * (0.9f - i * 0.14f)
        val topLeft = Offset(center.x - radius, center.y - radius)
        drawArc(c.copy(alpha = 0.18f), -90f, 360f, false, topLeft, Size(radius * 2, radius * 2), style = Stroke(r * 0.08f, cap = StrokeCap.Round))
        drawArc(c, -90f, 360f * frac, false, topLeft, Size(radius * 2, radius * 2), style = Stroke(r * 0.08f, cap = StrokeCap.Round))
    }
    val paint = android.graphics.Paint().apply { color = Color.White.toArgb(); textAlign = android.graphics.Paint.Align.CENTER; textSize = r * 0.34f; isFakeBoldText = true; isAntiAlias = true }
    drawContext.canvas.nativeCanvas.drawText("%d:%02d".format(if (h == 0) 12 else h, m), center.x, center.y + r * 0.12f, paint)
}

private fun DrawScope.drawPhoto(accent: Color, h: Int, m: Int) {
    // Abstract "photo": layered dusk gradient + silhouette hills
    drawRect(Brush.verticalGradient(listOf(Color(0xFF2B1E4F), Color(0xFFB85C7C), accent)))
    drawCircle(Color(0xFFFFE9B0), size.minDimension * 0.12f, Offset(size.width * 0.68f, size.height * 0.36f))
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(0f, size.height * 0.75f)
        cubicTo(size.width * 0.3f, size.height * 0.55f, size.width * 0.5f, size.height * 0.85f, size.width, size.height * 0.62f)
        lineTo(size.width, size.height); lineTo(0f, size.height); close()
    }
    drawPath(path, Color(0xFF120C22))
    val paint = android.graphics.Paint().apply { color = Color.White.toArgb(); textSize = size.minDimension * 0.22f; isAntiAlias = true; isFakeBoldText = true }
    drawContext.canvas.nativeCanvas.drawText("%d:%02d".format(if (h == 0) 12 else h, m), size.width * 0.12f, size.height * 0.3f, paint)
}

private fun DrawScope.drawWeather(accent: Color, h: Int, m: Int) {
    val r = size.minDimension / 2
    // sun arc
    drawArc(accent.copy(alpha = 0.25f), 180f, 180f, false, Offset(center.x - r * 0.8f, center.y - r * 0.8f + r * 0.3f), Size(r * 1.6f, r * 1.6f), style = Stroke(r * 0.03f))
    val a = Math.toRadians(200.0 + 120.0 * (h % 12) / 12.0)
    drawCircle(Color(0xFFFFC857), r * 0.08f, Offset(center.x + (r * 0.8f * cos(a)).toFloat(), center.y + r * 0.3f + (r * 0.8f * sin(a)).toFloat()))
    val paint = android.graphics.Paint().apply { color = Color.White.toArgb(); textAlign = android.graphics.Paint.Align.CENTER; textSize = r * 0.4f; isAntiAlias = true }
    drawContext.canvas.nativeCanvas.drawText("%d:%02d".format(if (h == 0) 12 else h, m), center.x, center.y + r * 0.1f, paint)
    paint.textSize = r * 0.16f; paint.color = accent.toArgb()
    drawContext.canvas.nativeCanvas.drawText("21°  Clear", center.x, center.y + r * 0.42f, paint)
    // forecast dots
    for (i in 0 until 6) drawCircle(accent.copy(alpha = 0.3f + i * 0.1f), r * 0.03f, Offset(center.x - r * 0.5f + i * r * 0.2f, center.y + r * 0.62f))
}
