package com.example.alirinmobile

import com.example.alirinmobile.data.RiskLevel
import com.example.alirinmobile.data.scoring.RiskEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Vektor uji bersama dengan app/src/domain/scoring.test.js (web) dan trigger SQL.
// Sumber: C:\ALIRIN\docs\RISK-ENGINE.md
// Bila salah satu implementasi menyimpang, uji ini gagal.
class RiskEngineTest {

    @Test
    fun `mereproduksi contoh Lokasi A pada Proposal 4-4`() {
        assertEquals(82, RiskEngine.combine(severity = 90, history = 80, weather = 85, location = 60))
    }

    @Test
    fun `mereproduksi contoh Lokasi B pada Proposal 4-4`() {
        assertEquals(45, RiskEngine.combine(severity = 45, history = 40, weather = 40, location = 60))
    }

    @Test
    fun `memakai bobot 35-25-25-15`() {
        assertEquals(35, RiskEngine.combine(100, 0, 0, 0))
        assertEquals(25, RiskEngine.combine(0, 100, 0, 0))
        assertEquals(25, RiskEngine.combine(0, 0, 100, 0))
        assertEquals(15, RiskEngine.combine(0, 0, 0, 100))
    }

    @Test
    fun `membagi ulang bobot cuaca saat data BMKG tidak ada`() {
        // Tanpa cuaca total bobot aktif 75. Keparahan penuh -> 35/75 = 46,7 -> 47.
        assertEquals(47, RiskEngine.combine(100, 0, null, 0))
        assertEquals(100, RiskEngine.combine(100, 100, null, 100))
    }

    @Test
    fun `memetakan keparahan`() {
        assertEquals(25, RiskEngine.severityScore("ringan"))
        assertEquals(55, RiskEngine.severityScore("sedang"))
        assertEquals(80, RiskEngine.severityScore("parah"))
        assertEquals(100, RiskEngine.severityScore("kritis"))
    }

    @Test
    fun `memetakan curah hujan ke kelas intensitas BMKG`() {
        assertEquals(0, RiskEngine.weatherScore(0.0))
        assertEquals(20, RiskEngine.weatherScore(0.4))
        assertEquals(45, RiskEngine.weatherScore(3.0))
        assertEquals(70, RiskEngine.weatherScore(7.0))
        assertEquals(88, RiskEngine.weatherScore(15.0))
        assertEquals(100, RiskEngine.weatherScore(40.0))
    }

    @Test
    fun `cuaca tidak tersedia bernilai null bukan nol`() {
        assertNull(RiskEngine.weatherScore(null))
        assertNull(RiskEngine.weatherScore(-1.0))
    }

    @Test
    fun `memetakan skor ke kelas risiko`() {
        assertEquals(RiskLevel.Normal, RiskEngine.levelOf(39))
        assertEquals(RiskLevel.Waspada, RiskEngine.levelOf(40))
        assertEquals(RiskLevel.Tinggi, RiskEngine.levelOf(60))
        assertEquals(RiskLevel.Kritis, RiskEngine.levelOf(80))
    }

    private val createdAt = 1_780_000_000_000L
    private val day = 24L * 60 * 60 * 1000

    @Test
    fun `histori menghitung laporan lain di titik yang sama`() {
        val neighbours = listOf(
            RiskEngine.NeighbourReport("a", -5.3972, 105.2669, createdAt - 30 * day, "selesai"),
            RiskEngine.NeighbourReport("b", -5.3970, 105.2667, createdAt - 60 * day, "masuk"),
        )
        val count = RiskEngine.historyCount("current", -5.3971, 105.2668, createdAt, neighbours)
        assertEquals(2, count)
        assertEquals(40, RiskEngine.historyScore(count))
    }

    @Test
    fun `histori mengabaikan laporan ditolak dan di luar jendela 180 hari`() {
        val neighbours = listOf(
            RiskEngine.NeighbourReport("a", -5.3972, 105.2669, createdAt - 30 * day, "ditolak"),
            RiskEngine.NeighbourReport("b", -5.3972, 105.2669, createdAt - 400 * day, "masuk"),
            RiskEngine.NeighbourReport("c", -5.3972, 105.2669, createdAt + 10 * day, "masuk"),
        )
        assertEquals(0, RiskEngine.historyCount("current", -5.3971, 105.2668, createdAt, neighbours))
    }

    @Test
    fun `mode laporan tidak lagi memengaruhi skor`() {
        // Rumus lama mengalikan skor 0,7 untuk mode Cepat. Sekarang mode tidak
        // menjadi masukan sama sekali, jadi tidak ada jalur yang bisa berbeda.
        val cepat = RiskEngine.evaluate(
            id = "x", severity = "kritis", lat = -5.3971, lng = 105.2668,
            createdAtMs = createdAt, rainfallMm = 12.0, photoCount = 0, description = "",
        )
        val lengkap = RiskEngine.evaluate(
            id = "x", severity = "kritis", lat = -5.3971, lng = 105.2668,
            createdAtMs = createdAt, rainfallMm = 12.0, photoCount = 3, description = "Saluran tersumbat total.",
        )
        assertEquals(cepat.score, lengkap.score)
    }

    @Test
    fun `rincian memuat enam faktor tabel Proposal 4-4`() {
        val result = RiskEngine.evaluate(
            id = "x", severity = "sedang", lat = -5.3971, lng = 105.2668,
            createdAtMs = createdAt, rainfallMm = null, photoCount = 1, description = "Air menggenang di jalan.",
        )
        assertEquals(
            listOf("severity", "history", "weather", "location", "bukti", "sensor"),
            result.breakdown.map { it.id },
        )
        // Bobot yang ditampilkan selalu berjumlah 100 walau cuaca tidak tersedia.
        assertEquals(100, result.breakdown.sumOf { it.weight })
        assertTrue(result.breakdown.first { it.id == "weather" }.detail!!.contains("dialihkan"))
    }

    @Test
    fun `skor deterministik untuk masukan yang sama`() {
        fun run() = RiskEngine.evaluate(
            id = "x", severity = "parah", lat = -5.3971, lng = 105.2668,
            createdAtMs = createdAt, rainfallMm = 6.0, photoCount = 2, description = "Got jebol di depan sekolah.",
        ).score
        assertEquals(run(), run())
    }
}
