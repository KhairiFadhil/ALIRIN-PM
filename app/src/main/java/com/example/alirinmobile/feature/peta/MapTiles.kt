package com.example.alirinmobile.feature.peta

import org.osmdroid.tileprovider.tilesource.XYTileSource

// tile.openstreetmap.org (MAPNIK) memblokir app/emulator. CARTO gratis & tidak seketat itu.
val CartoTiles: XYTileSource = XYTileSource(
    "CartoVoyager", 0, 20, 256, ".png",
    arrayOf(
        "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://c.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://d.basemaps.cartocdn.com/rastertiles/voyager/",
    ),
    "© OpenStreetMap contributors, © CARTO",
)
