package com.example.alirinmobile.data.repository

// Pencocokan keluaran Geocoder Android ke master wilayah.
//
// Dipisah dari KelurahanRepository supaya bisa diuji tanpa Context/assets.
// Geocoder tidak seragam: kadang berawalan "Kelurahan ", "Kel. ", "Kec. ",
// kadang beda kapitalisasi, dan sering mengembalikan nama jalan. Yang tidak
// cocok dengan master dikembalikan null, bukan dipaksakan — supaya nama jalan
// tidak pernah lolos sebagai kelurahan lalu ditolak saat laporan dikirim.
object AreaMatcher {

    private val PREFIX = Regex("^(kelurahan|kel\\.?|kecamatan|kec\\.?|desa)\\s+", RegexOption.IGNORE_CASE)

    fun normalise(raw: String?): String = raw.orEmpty()
        .trim()
        .replace(PREFIX, "")
        .replace(Regex("\\s+"), " ")
        .lowercase()

    fun isValidArea(areas: Map<String, List<String>>, kecamatan: String?, kelurahan: String?): Boolean {
        val kec = kecamatan?.trim().orEmpty()
        val kel = kelurahan?.trim().orEmpty()
        if (kec.isBlank() || kel.isBlank()) return false
        val list = areas.entries.firstOrNull { it.key.equals(kec, ignoreCase = true) }?.value ?: return false
        return list.any { it.equals(kel, ignoreCase = true) }
    }

    fun match(areas: Map<String, List<String>>, kecamatanRaw: String?, kelurahanRaw: String?): Pair<String, String>? {
        val kec = normalise(kecamatanRaw)
        val kel = normalise(kelurahanRaw)
        if (kel.isBlank()) return null

        // Kecamatan diketahui: cari kelurahan di dalamnya saja.
        val kecMatch = areas.keys.firstOrNull { normalise(it) == kec }
        if (kecMatch != null) {
            val kelMatch = areas[kecMatch]?.firstOrNull { normalise(it) == kel }
            if (kelMatch != null) return kecMatch to kelMatch
        }

        // Kecamatan tidak dikenali: cari di seluruh kota. Hanya diterima bila
        // kecocokannya tunggal, supaya nama yang ambigu tidak ditebak-tebak.
        val hits = areas.entries.flatMap { (kecName, list) ->
            list.filter { normalise(it) == kel }.map { kecName to it }
        }
        return hits.singleOrNull()
    }
}
