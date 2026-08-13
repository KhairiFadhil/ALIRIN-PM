package com.example.alirinmobile.feature.peta

import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.alirinmobile.data.Hotspot
import com.example.alirinmobile.data.RiskLevel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.Marker

private val BANDAR_LAMPUNG = GeoPoint(-5.3971, 105.2668)

@Composable
fun OsmMapView(
    hotspots: List<Hotspot>,
    selectedId: Int?,
    onHotspotTap: (Hotspot) -> Unit,
    modifier: Modifier = Modifier,
    userLocation: GeoPoint? = null,
    onMapReady: (MapView) -> Unit = {},
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(CartoTiles)
                setMultiTouchControls(true)

                zoomController.setVisibility(
                    org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER
                )
                controller.setZoom(13.0)
                controller.setCenter(BANDAR_LAMPUNG)
                setUseDataConnection(true)
                isHorizontalMapRepetitionEnabled = false
                isVerticalMapRepetitionEnabled = false
                onMapReady(this)
            }
        },
        update = { map ->

            map.overlays.removeAll { it is Marker }
            hotspots.forEach { h ->
                val m = Marker(map).apply {
                    position = GeoPoint(h.lat, h.lng)
                    title = h.name
                    snippet = "${h.risk.label} · ${h.score}"
                    icon = pinDrawable(map.context, h.risk, h.count, active = h.id == selectedId)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    setOnMarkerClickListener { _, _ ->
                        onHotspotTap(h)
                        true
                    }
                }
                map.overlays.add(m)
            }
            if (userLocation != null) {
                val me = Marker(map).apply {
                    position = userLocation
                    title = "Lokasi kamu"
                    icon = userDotDrawable(map.context)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    setOnMarkerClickListener { _, _ -> true }
                }
                map.overlays.add(me)
            }
            map.invalidate()
        },
    )
}

private fun pinDrawable(
    ctx: android.content.Context,
    risk: RiskLevel,
    count: Int,
    active: Boolean,
): android.graphics.drawable.Drawable {
    val sizePx = (if (active) 56 else 48) * ctx.resources.displayMetrics.density
    val bmp = android.graphics.Bitmap.createBitmap(sizePx.toInt(), sizePx.toInt(), android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val cx = bmp.width / 2f
    val cy = bmp.height / 2f
    val outerR = bmp.width / 2f - 1f
    val ringW = 3f * ctx.resources.displayMetrics.density
    val innerR = outerR - ringW - 2f

    val ringColor = riskAndroidColor(risk)

    val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.WHITE }
    canvas.drawCircle(cx, cy, outerR, whitePaint)

    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = ringColor
        strokeWidth = ringW
    }
    canvas.drawCircle(cx, cy, outerR - ringW / 2f, ringPaint)

    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ringColor }
    canvas.drawCircle(cx, cy, innerR, innerPaint)

    if (count > 0) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            textAlign = Paint.Align.CENTER
            textSize = 14f * ctx.resources.displayMetrics.density
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val metrics = textPaint.fontMetrics
        val baseline = cy - (metrics.ascent + metrics.descent) / 2f
        canvas.drawText(count.toString(), cx, baseline, textPaint)
    }
    return android.graphics.drawable.BitmapDrawable(ctx.resources, bmp)
}

private fun userDotDrawable(ctx: android.content.Context): android.graphics.drawable.Drawable {
    val d = ctx.resources.displayMetrics.density
    val sizePx = (22 * d).toInt()
    val bmp = android.graphics.Bitmap.createBitmap(sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val cx = bmp.width / 2f
    val cy = bmp.height / 2f
    val blue = AndroidColor.parseColor("#1A73E8")

    canvas.drawCircle(cx, cy, cx, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = blue; alpha = 38
    })

    canvas.drawCircle(cx, cy, 7f * d, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.WHITE })

    canvas.drawCircle(cx, cy, 5f * d, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = blue })
    return android.graphics.drawable.BitmapDrawable(ctx.resources, bmp)
}

private fun riskAndroidColor(risk: RiskLevel): Int = when (risk) {
    RiskLevel.Normal  -> AndroidColor.parseColor("#2C7349")
    RiskLevel.Waspada -> AndroidColor.parseColor("#C99110")
    RiskLevel.Tinggi  -> AndroidColor.parseColor("#D26222")
    RiskLevel.Kritis  -> AndroidColor.parseColor("#C0332A")
}
