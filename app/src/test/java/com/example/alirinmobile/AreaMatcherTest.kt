package com.example.alirinmobile

import com.example.alirinmobile.data.repository.AreaMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Pencocokan keluaran Geocoder ke master wilayah. Kasus-kasus di sini adalah
// bentuk yang benar-benar dikembalikan Geocoder Android dan dulu membuat
// laporan ditolak saat dikirim.
class AreaMatcherTest {

    private val areas = mapOf(
        "Rajabasa" to listOf("Gedong Meneng", "Gedong Meneng Baru", "Rajabasa", "Rajabasa Nunyai"),
        "Kemiling" to listOf("Beringin Raya", "Pinang Jaya", "Sumber Agung"),
        "Teluk Betung Selatan" to listOf("Gedong Pakuan", "Talang", "Teluk Betung"),
    )

    @Test
    fun `cocok persis`() {
        assertEquals("Rajabasa" to "Gedong Meneng", AreaMatcher.match(areas, "Rajabasa", "Gedong Meneng"))
    }

    @Test
    fun `mengabaikan awalan Kelurahan dan Kecamatan`() {
        assertEquals(
            "Rajabasa" to "Gedong Meneng",
            AreaMatcher.match(areas, "Kecamatan Rajabasa", "Kelurahan Gedong Meneng"),
        )
        assertEquals(
            "Rajabasa" to "Gedong Meneng",
            AreaMatcher.match(areas, "Kec. Rajabasa", "Kel. Gedong Meneng"),
        )
    }

    @Test
    fun `mengabaikan kapitalisasi dan spasi ganda`() {
        assertEquals(
            "Kemiling" to "Pinang Jaya",
            AreaMatcher.match(areas, "KEMILING", "pinang  jaya"),
        )
    }

    @Test
    fun `menemukan kelurahan walau kecamatan tidak dikenali`() {
        // Geocoder sering mengembalikan "Bandar Lampung" sebagai kecamatan.
        assertEquals(
            "Kemiling" to "Beringin Raya",
            AreaMatcher.match(areas, "Bandar Lampung", "Beringin Raya"),
        )
    }

    @Test
    fun `nama jalan tidak pernah lolos sebagai kelurahan`() {
        // Ini akar bug lama: reverseGeocode mencadangkan ke thoroughfare.
        assertNull(AreaMatcher.match(areas, "Rajabasa", "Jalan Zainal Abidin Pagar Alam"))
        assertNull(AreaMatcher.match(areas, "Bandar Lampung", "Gg. Merak"))
    }

    @Test
    fun `masukan kosong ditolak`() {
        assertNull(AreaMatcher.match(areas, "Rajabasa", null))
        assertNull(AreaMatcher.match(areas, null, ""))
    }

    @Test
    fun `validasi wilayah`() {
        assertTrue(AreaMatcher.isValidArea(areas, "Rajabasa", "Gedong Meneng"))
        assertTrue(AreaMatcher.isValidArea(areas, "rajabasa", "gedong meneng"))
        assertFalse(AreaMatcher.isValidArea(areas, "Rajabasa", "Pinang Jaya"))
        assertFalse(AreaMatcher.isValidArea(areas, "", "Gedong Meneng"))
        assertFalse(AreaMatcher.isValidArea(areas, "Rajabasa", ""))
    }

    @Test
    fun `kelurahan yang dulu hilang kini dikenali`() {
        // Lima kelurahan ini tidak ada di master lama, sehingga titik di sana
        // selalu gagal validasi.
        val full = mapOf(
            "Kedamaian" to listOf("Tanjung Gading"),
            "Rajabasa" to listOf("Gedong Meneng", "Gedong Meneng Baru"),
            "Tanjung Karang Pusat" to listOf("Pasir Gintung"),
            "Teluk Betung Utara" to listOf("Gulak Galik"),
        )
        assertTrue(AreaMatcher.isValidArea(full, "Kedamaian", "Tanjung Gading"))
        assertTrue(AreaMatcher.isValidArea(full, "Rajabasa", "Gedong Meneng Baru"))
        assertTrue(AreaMatcher.isValidArea(full, "Tanjung Karang Pusat", "Pasir Gintung"))
        assertTrue(AreaMatcher.isValidArea(full, "Teluk Betung Utara", "Gulak Galik"))
    }
}
