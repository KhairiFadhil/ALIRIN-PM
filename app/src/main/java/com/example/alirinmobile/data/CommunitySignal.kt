package com.example.alirinmobile.data

import com.example.alirinmobile.data.repository.ReportCodegen
import com.example.alirinmobile.data.scoring.RiskEngine

// Sinyal validasi gotong-royong.
//
// Aplikasi menjanjikan aturan yang jelas di dua tempat: layar Tentang menulis
// "3+ laporan di radius 100 m / 24 jam", dan layar pilihan mode menulis "jika
// 3+ warga lapor di area yang sama". Yang dihitung kode sebelumnya bukan itu:
// ia menghitung SELURUH laporan di kelurahan yang sama, tanpa batas waktu, lalu
// menampilkannya sebagai "N orang".
//
// Dua penyimpangan sekaligus:
//   1. Kelurahan jauh lebih luas daripada 100 m, dan "sepanjang waktu" bukan
//      24 jam. Angkanya karena itu hampir selalu lebih besar dari yang
//      dimaksud aturannya.
//   2. Yang dihitung adalah laporan, bukan orang. Satu warga yang mengirim tiga
//      laporan akan terbaca "3 orang, memenuhi syarat gotong-royong".
//
// Penyimpangan pertama diperbaiki di sini. Penyimpangan kedua tidak bisa
// diperbaiki sampai setiap perangkat punya identitas yang bisa dipercaya
// (rekomendasi P-8, butuh anonymous auth Supabase). Sampai saat itu, angkanya
// disebut apa adanya sebagai jumlah laporan -- bukan jumlah orang.

object CommunitySignal {

    const val RADIUS_KM = 0.1
    const val WINDOW_HOURS = 24
    const val AMBANG = 3

    data class Result(
        val reportCount: Int,
        val memenuhiAmbang: Boolean,
        // Selama identitas pelapor belum ada, jumlah orang tidak bisa dihitung.
        val pelaporUnik: Int?,
    )

    // Pembungkus untuk layar, yang memegang Report bukan NeighbourReport.
    fun evaluate(report: Report, others: List<Report>): Result {
        val atMs = ReportCodegen.parseIsoMillis(report.createdAt) ?: System.currentTimeMillis()
        return evaluate(
            reportId = report.id,
            lat = report.lat,
            lng = report.lng,
            atMs = atMs,
            others = others.mapNotNull { other ->
                val lat = other.lat ?: return@mapNotNull null
                val lng = other.lng ?: return@mapNotNull null
                RiskEngine.NeighbourReport(
                    id = other.id,
                    lat = lat,
                    lng = lng,
                    createdAtMs = ReportCodegen.parseIsoMillis(other.createdAt) ?: 0L,
                    status = other.status.toWire(),
                )
            },
        )
    }

    fun evaluate(
        reportId: String?,
        lat: Double?,
        lng: Double?,
        atMs: Long,
        others: List<RiskEngine.NeighbourReport>,
    ): Result {
        if (lat == null || lng == null) return Result(1, false, null)

        val windowStart = atMs - WINDOW_HOURS * 3_600_000L
        val count = others.count { other ->
            other.id != reportId &&
                other.status != "ditolak" &&
                other.createdAtMs in windowStart..atMs &&
                RiskEngine.distanceKm(lat, lng, other.lat, other.lng) <= RADIUS_KM
        } + 1

        return Result(
            reportCount = count,
            memenuhiAmbang = count >= AMBANG,
            pelaporUnik = null,
        )
    }
}
