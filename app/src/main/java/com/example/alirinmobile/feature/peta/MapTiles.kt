package com.example.alirinmobile.feature.peta

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.XYTileSource

// Server volunteer OSM memblokir aplikasi (tile usage policy). Pakai basemap CARTO yang
// permisif untuk pemakaian wajar, jadi peta tetap tampil tanpa 403.
val AlirinTileSource: OnlineTileSourceBase = XYTileSource(
    "CartoVoyager", 0, 20, 256, ".png",
    arrayOf(
        "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://c.basemaps.cartocdn.com/rastertiles/voyager/",
    ),
    "© OpenStreetMap contributors © CARTO",
)
