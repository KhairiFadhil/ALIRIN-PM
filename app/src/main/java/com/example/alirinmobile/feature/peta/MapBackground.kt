package com.example.alirinmobile.feature.peta

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

enum class MapStyle { Light, Muted, Dark }

private data class Palette(
    val bg: Color, val land: Color, val water: Color, val park: Color,
    val road: Color, val roadStroke: Color, val label: Color,
)

private fun palette(style: MapStyle): Palette = when (style) {
    MapStyle.Muted -> Palette(
        bg = Color(0xFFE8E1D2), land = Color(0xFFF0E9DC), water = Color(0xFFC8D8E0),
        park = Color(0xFFD9D5BD), road = Color.White, roadStroke = Color(0xFFB5AC97),
        label = Color(0xFF5A5040),
    )
    MapStyle.Dark -> Palette(
        bg = Color(0xFF1B1E26), land = Color(0xFF252836), water = Color(0xFF13182A),
        park = Color(0xFF1F2A28), road = Color(0xFF3D4555), roadStroke = Color(0xFF171930),
        label = Color(0xFF7B8290),
    )
    MapStyle.Light -> Palette(
        bg = Color(0xFFF1EDE4), land = Color(0xFFFBF9F4), water = Color(0xFFB9D7EE),
        park = Color(0xFFDEEBD6), road = Color.White, roadStroke = Color(0xFFD9D5CC),
        label = Color(0xFF9A9281),
    )
}

/**
 * Faux OSM-style map background. Mirrors the SVG in screens-peta.jsx using a virtual
 * 412x600 viewport scaled to fill.
 */
@Composable
fun MapBackground(style: MapStyle = MapStyle.Light, modifier: Modifier = Modifier) {
    val p = palette(style)
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val sx = w / 412f
        val sy = h / 600f

        fun p(x: Float, y: Float) = Offset(x * sx, y * sy)

        // Background
        drawRect(p.land, size = Size(w, h))

        // Grid (subtle)
        val grid = 40f
        val gridColor = p.bg.copy(alpha = 0.3f)
        var gx = 0f
        while (gx < 412f) {
            drawLine(gridColor, p(gx, 0f), p(gx, 600f), strokeWidth = 0.5f)
            gx += grid
        }
        var gy = 0f
        while (gy < 600f) {
            drawLine(gridColor, p(0f, gy), p(412f, gy), strokeWidth = 0.5f)
            gy += grid
        }

        // River (winding diagonal)
        val river = Path().apply {
            moveTo(-20f * sx, 200f * sy)
            quadraticBezierTo(60f * sx, 230f * sy, 100f * sx, 280f * sy)
            quadraticBezierTo(140f * sx, 330f * sy, 200f * sx, 320f * sy)
            quadraticBezierTo(270f * sx, 310f * sy, 320f * sx, 360f * sy)
            quadraticBezierTo(380f * sx, 410f * sy, 430f * sx, 430f * sy)
            lineTo(430f * sx, 470f * sy)
            quadraticBezierTo(380f * sx, 450f * sy, 320f * sx, 400f * sy)
            quadraticBezierTo(270f * sx, 350f * sy, 200f * sx, 360f * sy)
            quadraticBezierTo(140f * sx, 370f * sy, 100f * sx, 320f * sy)
            quadraticBezierTo(60f * sx, 270f * sy, -20f * sx, 240f * sy)
            close()
        }
        drawPath(river, color = p.water.copy(alpha = 0.9f))

        // Small lake (oval)
        drawOval(
            color = p.water.copy(alpha = 0.85f),
            topLeft = p(80f - 55f, 500f - 22f),
            size = Size(55f * 2 * sx, 22f * 2 * sy),
        )

        // Parks (ovals)
        drawOval(
            color = p.park.copy(alpha = 0.85f),
            topLeft = p(320f - 50f, 180f - 32f),
            size = Size(50f * 2 * sx, 32f * 2 * sy),
        )
        drawOval(
            color = p.park.copy(alpha = 0.85f),
            topLeft = p(120f - 40f, 120f - 28f),
            size = Size(40f * 2 * sx, 28f * 2 * sy),
        )

        // Major roads (white core + thin stroke)
        fun drawRoad(path: Path, coreWidth: Float, strokeWidth: Float) {
            drawPath(path, color = p.road, style = Stroke(width = coreWidth))
            drawPath(path, color = p.roadStroke, style = Stroke(width = strokeWidth))
        }

        val majors = listOf(
            Path().apply { moveTo(0f, 90f * sy); lineTo(412f * sx, 100f * sy) } to 11f,
            Path().apply { moveTo(220f * sx, 0f); lineTo(200f * sx, 600f * sy) } to 11f,
            Path().apply {
                moveTo(0f, 540f * sy)
                quadraticBezierTo(200f * sx, 525f * sy, 412f * sx, 545f * sy)
            } to 9f,
        )
        majors.forEach { (path, core) -> drawRoad(path, core, 0.7f) }

        val minors = listOf(
            Path().apply { moveTo(0f, 40f * sy); lineTo(412f * sx, 45f * sy) } to 5f,
            Path().apply { moveTo(60f * sx, 0f); lineTo(80f * sx, 600f * sy) } to 5f,
            Path().apply { moveTo(340f * sx, 0f); lineTo(330f * sx, 600f * sy) } to 5f,
            Path().apply { moveTo(0f, 380f * sy); lineTo(412f * sx, 385f * sy) } to 4f,
        )
        minors.forEach { (path, core) -> drawRoad(path, core, 0.5f) }

        // (District labels skipped — drawing text on Canvas is more involved;
        //  the design's faux labels are decorative.)
        @Suppress("UNUSED_VARIABLE")
        val unused = Rect(Offset.Zero, Size.Zero)
    }
}
