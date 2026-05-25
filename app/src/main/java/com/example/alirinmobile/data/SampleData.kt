package com.example.alirinmobile.data

object SampleData {

    val reports: List<Report> = listOf(
        Report(
            id = "r1", code = "ALR-2026-04217", mode = ReportMode.Lengkap,
            status = ReportStatus.InProgress, risk = RiskLevel.Tinggi, score = 72,
            category = "Sumbatan sampah",
            kelurahan = "Pinang Jaya", kecamatan = "Kemiling",
            address = "Jl. Imam Bonjol depan SD 2",
            createdAt = "12 Mei · 14:30",
            updatedAt = "13 Mei · 09:15",
            description = "Sampah plastik & daun menyumbat got, air mulai menggenang ke jalan.",
            photos = 2,
            history = listOf(
                HistoryEntry(ReportStatus.Pending,    "12 Mei · 14:30", "Laporan masuk dari aplikasi."),
                HistoryEntry(ReportStatus.Verified,   "12 Mei · 16:08", "Petugas verifikasi via foto & 3 laporan terdekat."),
                HistoryEntry(ReportStatus.Scheduled,  "13 Mei · 08:30", "Tim Kebersihan Kemiling dijadwalkan turun 13 Mei sore."),
                HistoryEntry(ReportStatus.InProgress, "13 Mei · 09:15", "Tim sudah di lokasi, mulai pembersihan.", live = true),
            ),
        ),
        Report(
            id = "r2", code = "ALR-2026-04201", mode = ReportMode.Cepat,
            status = ReportStatus.Verified, risk = RiskLevel.Waspada, score = 48,
            category = "Aliran lambat",
            kelurahan = "Way Halim Permai", kecamatan = "Way Halim",
            createdAt = "11 Mei · 18:42",
            description = "",
            photos = 0,
            history = listOf(
                HistoryEntry(ReportStatus.Pending,  "11 Mei · 18:42", "Lapor Cepat masuk."),
                HistoryEntry(ReportStatus.Verified, "12 Mei · 10:15", "Diverifikasi gotong-royong (3 warga di radius 100 m)."),
            ),
        ),
        Report(
            id = "r3", code = "ALR-2026-04088", mode = ReportMode.Lengkap,
            status = ReportStatus.Completed, risk = RiskLevel.Normal, score = 22,
            category = "Drainase rusak",
            kelurahan = "Kemiling Permai", kecamatan = "Kemiling",
            createdAt = "5 Mei · 09:14",
            description = "Tutup got pecah, anak2 hampir jatuh.",
            photos = 3,
            history = listOf(
                HistoryEntry(ReportStatus.Pending,    "5 Mei · 09:14"),
                HistoryEntry(ReportStatus.Verified,   "5 Mei · 11:00", "Admin verifikasi."),
                HistoryEntry(ReportStatus.Scheduled,  "5 Mei · 13:30", "Dijadwalkan 6 Mei pagi."),
                HistoryEntry(ReportStatus.InProgress, "6 Mei · 08:20", "Tim pasang tutup darurat."),
                HistoryEntry(ReportStatus.Completed,  "6 Mei · 11:45", "Tutup got permanen terpasang. Aman dilewati."),
            ),
        ),
    )

    // Bandar Lampung centre ≈ (-5.3971, 105.2668). Coords below are realistic-ish points.
    val hotspots: List<Hotspot> = listOf(
        Hotspot(1, RiskLevel.Kritis,  86, 7, 0.24f, 0.30f, -5.3700, 105.2900, "Sukabumi Indah RT 03",    "Sukabumi",         "Sukabumi",                "Genangan jalan",  ReportStatus.Verified,   HotspotSource.Warga,    "1.8 km", "2 jam lalu"),
        Hotspot(2, RiskLevel.Tinggi,  72, 4, 0.40f, 0.52f, -5.3826, 105.2589, "Pinang Jaya, depan SD",   "Pinang Jaya",      "Kemiling",                "Sumbatan sampah", ReportStatus.InProgress, HotspotSource.Warga,    "0.6 km", "5 jam lalu"),
        Hotspot(3, RiskLevel.Waspada, 51, 2, 0.68f, 0.40f, -5.3864, 105.3072, "Way Halim Permai blok B", "Way Halim",        "Way Halim",               "Aliran lambat",   ReportStatus.Pending,    HotspotSource.Warga,    "1.1 km", "1 hari lalu"),
        Hotspot(4, RiskLevel.Tinggi,  65, 1, 0.30f, 0.70f, -5.4148, 105.2597, "Jalan Untung Suropati",   "Gunung Sari",      "Tanjung Karang Pusat",    "Drainase rusak",  ReportStatus.Verified,   HotspotSource.Historis, "2.3 km", "Historis"),
        Hotspot(5, RiskLevel.Normal,  30, 0, 0.78f, 0.20f, -5.3650, 105.2950, "Sensor S-Rajabasa-04",    "Rajabasa Raya",    "Rajabasa",                "Pantau IoT",      ReportStatus.Verified,   HotspotSource.Iot,      "3.0 km", "Real-time"),
        Hotspot(6, RiskLevel.Waspada, 48, 0, 0.55f, 0.15f, -5.3750, 105.2780, "Potensi hujan sore",      "Kemiling Permai",  "Kemiling",                "Alert BMKG",      ReportStatus.Pending,    HotspotSource.Cuaca,    "0.9 km", "Prediksi 16:00"),
    )

    val nearbyTeasers: List<NearbyTeaser> = listOf(
        NearbyTeaser(1, "Pinang Jaya, Kemiling", RiskLevel.Tinggi,  72, "0.6 km", 4, "Genangan"),
        NearbyTeaser(2, "Way Halim Permai",      RiskLevel.Waspada, 51, "1.1 km", 2, "Sumbatan"),
        NearbyTeaser(3, "Sukabumi Indah",        RiskLevel.Kritis,  86, "1.8 km", 7, "Genangan"),
    )
}
