package com.example.alirinmobile.feature.peta

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView

private val BANDAR_LAMPUNG = GeoPoint(-5.3971, 105.2668)

/**
 * Peta interaktif untuk memilih titik laporan. Titik yang dipilih = tengah layar
 * (pin diletakkan di tengah oleh pemanggil). Geser peta -> [onCenterChanged] terpanggil.
 * Naikkan [recenterKey] untuk memaksa peta loncat ke (lat,lng) — dipakai saat tombol GPS.
 */
@Composable
fun LocationPickerMap(
    lat: Double?,
    lng: Double?,
    recenterKey: Int,
    onCenterChanged: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cb by rememberUpdatedState(onCenterChanged)
    val start = remember { if (lat != null && lng != null) GeoPoint(lat, lng) else BANDAR_LAMPUNG }
    var appliedKey by remember { mutableIntStateOf(recenterKey) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                controller.setZoom(16.0)
                controller.setCenter(start)
                isHorizontalMapRepetitionEnabled = false
                isVerticalMapRepetitionEnabled = false
                addMapListener(object : MapListener {
                    override fun onScroll(event: ScrollEvent?): Boolean {
                        val c = mapCenter
                        cb(c.latitude, c.longitude)
                        return true
                    }
                    override fun onZoom(event: ZoomEvent?): Boolean = false
                })
            }
        },
        update = { map ->
            if (recenterKey != appliedKey && lat != null && lng != null) {
                appliedKey = recenterKey
                map.controller.setZoom(17.0)
                map.controller.animateTo(GeoPoint(lat, lng))
            }
        },
    )
}
