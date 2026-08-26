package com.example.alirinmobile

import com.example.alirinmobile.data.CommunitySignal
import com.example.alirinmobile.data.scoring.RiskEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Aturan yang diuji di sini adalah aturan yang tertulis di layar Tentang:
// 3+ laporan dalam radius 100 m dan 24 jam. Sebelum perbaikan ini, kode
// menghitung seluruh laporan di kelurahan yang sama tanpa batas waktu.
class CommunitySignalTest {

    private val now = 1_756_000_000_000L
    private val lat = -5.3971
    private val lng = 105.2668

    // 0.0009 derajat lintang kira-kira 100 m; 0.0045 kira-kira 500 m.
    private fun tetangga(
        id: String,
        dLat: Double = 0.0,
        jamLalu: Long = 1,
        status: String = "masuk",
    ) = RiskEngine.NeighbourReport(
        id = id,
        lat = lat + dLat,
        lng = lng,
        createdAtMs = now - jamLalu * 3_600_000L,
        status = status,
    )

    private fun evaluate(others: List<RiskEngine.NeighbourReport>) =
        CommunitySignal.evaluate("ini", lat, lng, now, others)

    @Test
    fun `laporan sendiri selalu terhitung satu`() {
        assertEquals(1, evaluate(emptyList()).reportCount)
        assertFalse(evaluate(emptyList()).memenuhiAmbang)
    }

    @Test
    fun `tiga laporan dekat dan baru memenuhi ambang`() {
        val hasil = evaluate(listOf(tetangga("a"), tetangga("b")))
        assertEquals(3, hasil.reportCount)
        assertTrue(hasil.memenuhiAmbang)
    }

    @Test
    fun `laporan di luar radius 100 m tidak dihitung`() {
        val hasil = evaluate(listOf(tetangga("a", dLat = 0.0045), tetangga("b", dLat = 0.0045)))
        assertEquals(1, hasil.reportCount)
    }

    @Test
    fun `laporan lebih lama dari 24 jam tidak dihitung`() {
        val hasil = evaluate(listOf(tetangga("a", jamLalu = 30), tetangga("b", jamLalu = 48)))
        assertEquals(1, hasil.reportCount)
    }

    @Test
    fun `laporan yang ditolak tidak dihitung`() {
        val hasil = evaluate(listOf(tetangga("a", status = "ditolak"), tetangga("b")))
        assertEquals(2, hasil.reportCount)
    }

    @Test
    fun `laporan dari masa depan tidak dihitung`() {
        assertEquals(1, evaluate(listOf(tetangga("a", jamLalu = -5))).reportCount)
    }

    @Test
    fun `tanpa koordinat tidak bisa dinilai`() {
        val hasil = CommunitySignal.evaluate("ini", null, null, now, listOf(tetangga("a"), tetangga("b")))
        assertEquals(1, hasil.reportCount)
        assertFalse(hasil.memenuhiAmbang)
    }

    // Jumlah orang belum bisa dihitung sampai tiap perangkat punya identitas
    // yang bisa dipercaya (rekomendasi P-8). Sampai saat itu nilainya null,
    // bukan ditebak dari jumlah laporan.
    @Test
    fun `jumlah pelapor unik belum tersedia`() {
        assertNull(evaluate(listOf(tetangga("a"), tetangga("b"))).pelaporUnik)
    }
}
