package com.example.alirinmobile.feature.peta

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView

private val BANDAR_LAMPUNG = GeoPoint(-5.3971, 105.2668)

/**
 * Peta mini (tile asli OSM) untuk thumbnail/preview di detail laporan & layar sukses.
 * Tidak interaktif — sentuhan dikonsumsi supaya parent (Column scroll) tetap bisa di-scroll.
 * Pin penanda digambar oleh pemanggil sebagai overlay Compose di tengah Box.
 */
@Composable
fun StaticMapPreview(
    lat: Double?,
    lng: Double?,
    modifier: Modifier = Modifier,
    zoom: Double = 16.0,
) {
    val point = remember(lat, lng) {
        if (lat != null && lng != null) GeoPoint(lat, lng) else BANDAR_LAMPUNG
    }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(false)
                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                isHorizontalMapRepetitionEnabled = false
                isVerticalMapRepetitionEnabled = false
                controller.setZoom(zoom)
                controller.setCenter(point)
                @Suppress("ClickableViewAccessibility")
                setOnTouchListener { _, _ -> true } // konsumsi sentuhan -> peta diam, parent bisa scroll
            }
        },
        update = { map ->
            map.controller.setCenter(point)
        },
    )
}
