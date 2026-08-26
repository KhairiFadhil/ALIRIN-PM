# Changelog — ALIRIN Mobile

Semua perubahan penting pada **ALIRIN Mobile** didokumentasikan di berkas ini.
Format mengikuti [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) dan
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.2] - 2026-08-26

### Fixed
- Notifikasi menghitung laporan **milik pengguna** (bukan seluruh laporan kota),
  sehingga tidak lagi muncul "N laporan kamu" saat pengguna belum pernah melapor.
- Headline Beranda "titik kritis" dihitung untuk area yang ditampilkan.

### Changed
- Layar Statistik petugas kini sepenuhnya dari data nyata: grafik 14 hari,
  rata-rata harian, label tanggal, waktu respons, dan bulan berjalan.

## [1.0.1] - 2026-08-26

### Fixed
- Area cuaca & prediksi mengikuti lokasi GPS pengguna, bukan tetap Kemiling.
- Pilihan kelurahan saat lapor menyesuaikan lokasi; alur pilih lokasi dikeraskan
  agar tidak crash walau data wilayah gagal muat.

### Changed
- Label cuaca menyebut jam prakiraan (mis. "prakiraan 18:00").

## [1.0.0] - 2026-08-26

Rilis perdana aplikasi warga & petugas ALIRIN Mobile.

### Added
- **Pelaporan** — Lapor Cepat & Lengkap, lokasi GPS/peta, master 126 kelurahan
  Kota Bandar Lampung dengan kode wilayah BMKG.
- **Risk Engine** — cermin rumus di basis data (bobot 35/25/25/15), rincian skor
  per faktor, dan konteks hulu–hilir.
- **AI-assisted analysis** — kartu prakiraan & penilaian risiko via Edge Function
  Supabase; kunci Groq tidak tertanam di APK.
- **Identitas per perangkat** — sesi anonim, "Laporan Saya" tanpa token, rate
  limit, dan sinyal gotong-royong (pelapor unik).
- **Alert** — spanduk saat skor melewati ambang dan saat hujan deras di hulu.
- **Portal petugas** — verifikasi lapangan berfoto, pembaruan status, statistik.
- **Offline-first** — cache & outbox Room; sinkron menyusul saat online.

---
*Versi debug untuk pengujian dan demo; lihat halaman Releases untuk APK.*
