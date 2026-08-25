# ALIRIN Mobile — Functional Spec (Citizen App)

Dokumen ini menjelaskan **cara kerja** ALIRIN Mobile (Android, sisi warga). Semua urusan UI/visual/layout/komponen tidak dibahas di sini — itu jatahnya tahap desain terpisah (Claude Design).

Versi: v2 · Tanggal: 2026-08-26 · Platform: Android

> **Status dokumen.** v1 menjanjikan beberapa hal yang tidak pernah terbangun
> (anonymous Supabase auth, verifikasi gotong-royong, sumber data peta gabungan,
> tabel `historical_hotspots`). Bagian-bagian itu dipindahkan ke "Belum
> terbangun" agar dokumen ini menggambarkan aplikasi yang benar-benar ada.

---

## 1. Ringkasan Produk

ALIRIN = sistem kota cerdas Bandar Lampung untuk monitoring & prioritas preventif drainase mikro yang berpotensi menyebabkan genangan.

Versi web punya 3 sisi: **warga** (lapor), **petugas** (tugas lapangan), dan **admin** (validasi & penugasan). App mobile melayani **warga dan petugas**; admin tetap eksklusif di web, dan akun admin yang login di mobile diarahkan ke halaman penjelasan. Datanya menyatu lewat Supabase.

Tujuan utama: warga bisa lapor drainase langsung dari HP dalam **< 2 menit**, plus monitor titik rawan di sekitar lokasinya.

Konteks lomba: GEMASTIK Smart City — produk harus bertindak sebagai *smart city tool*, bukan app lapor generik.

---

## 2. Target User

- **Warga umum** Bandar Lampung (mahasiswa, pekerja, pemilik usaha kecil, ibu rumah tangga, dll).
- Akses tanpa daftar. Belum ada identitas server-side per perangkat: kepemilikan laporan dilacak secara lokal (kolom `created_locally` di Room) plus tracking token untuk lintas perangkat.
- Konteks penggunaan: di lokasi laporan, sering pas hujan atau setelah hujan, koneksi bisa jelek.

---

## 3. Scope v1

### ✅ In scope

1. 5 area fungsional warga: Beranda, Lapor, Peta Risiko, Status Laporan, Tentang.
2. 5 area fungsional petugas: Inbox verifikasi, Tindak lanjut, Peta, Statistik, Profil.
3. **Lapor Cepat** (foto & deskripsi opsional) **vs Lapor Lengkap** (foto & deskripsi wajib).
   Mode **tidak** memengaruhi risk score — lihat catatan di §4.1.
4. Watermark foto otomatis (lat/long + timestamp) untuk anti-laporan palsu.
   Tanpa koordinat, watermark hanya memuat waktu.
5. Peta risiko dengan fitur **"Area kamu"** — titik dalam 1 km dari lokasi pengguna.
   Posisi pengguna **dilacak berkelanjutan** (tiap 5 detik atau 10 meter), bukan
   sekali ambil, dan izin lokasi diminta saat aplikasi dibuka.
6. Offline draft + queued submit (WorkManager + outbox Room).
7. Status tracking dengan timeline (Menunggu → Diverifikasi → Dijadwalkan → Ditangani → Selesai / Ditolak),
   urutannya ditegakkan `StatusMachine` di klien dan trigger di Supabase.
8. Tracking token ditampilkan setelah kirim dan bisa dimasukkan kembali untuk
   melacak laporan dari perangkat lain.
9. Risk score memakai `RiskEngine` bersama (bobot Proposal 4.4), dengan curah
   hujan BMKG sebagai masukan faktor Cuaca.

### ❌ Belum terbangun

- **Anonymous Supabase auth.** Warga memakai kunci anon biasa; tidak ada
  `reporter_id` per perangkat. Konsekuensinya rate limiting per perangkat dan
  verifikasi gotong-royong belum bisa dibuat dengan benar.
- **Verifikasi gotong-royong** (3+ laporan dalam radius 100 m / 24 jam).
  Pengelompokan titik belum ada; setiap marker peta mewakili satu laporan.
- **Sumber data peta gabungan.** Hanya laporan warga. `HotspotSource.Historis`,
  `Cuaca`, dan `Iot` masih enum tanpa pemasok data.
- **Tabel `historical_hotspots`.** Tidak ada di basis data.
- Push notification status.
- ML auto-deteksi kategori dari foto.
- Sensor IoT fisik (roadmap Tahap 4).
- Akun warga dengan nomor telepon/email.
- iOS, tablet-optimized.

---

## 4. Daftar Fitur Detail

### 4.1 Lapor Cepat vs Lapor Lengkap

Saat user mulai melapor, ada 2 jalur pilihan:

| Aspek | Lapor Cepat | Lapor Lengkap |
|---|---|---|
| Waktu isi | ~10 detik | ~60–90 detik |
| Foto | Tidak perlu | Wajib (1–3 foto) |
| Field wajib | Lokasi + Kategori + Severity | + Deskripsi (min 10 char) |
| Field opsional | Deskripsi singkat | Nama, kontak |
| Pengaruh ke risk score | **Tidak ada** | **Tidak ada** |
| Kelengkapan bukti | Tercatat, bobot 0 | Tercatat, bobot 0 |

Alasan: warga sering tidak sempat memotret. Daripada tidak melapor sama sekali,
lebih baik melapor cepat.

**Mode laporan tidak memengaruhi skor.** Versi lama mengalikan skor 0,7 untuk
mode Cepat, sehingga laporan banjir kritis yang dikirim cepat justru turun dua
kelas menjadi Waspada — kebalikan dari maksud Proposal 4.3.1 yang menempatkan
Lapor Cepat justru untuk kondisi mendesak. Kelengkapan bukti tetap dicatat pada
rincian skor dengan bobot 0, siap dinaikkan setelah evaluasi lapangan.

### 4.2 Verifikasi Gotong-Royong — belum terbangun

Rancangan: jika dalam **100 m** dan **24 jam** ada **≥ 3 laporan dari pelapor
berbeda**, sistem menandai titik itu sebagai terverifikasi warga.

Prasyaratnya belum ada: tanpa identitas per perangkat, "pelapor berbeda" tidak
bisa dibedakan dari satu orang yang mengirim tiga kali. Fitur ini menunggu
anonymous auth (lihat "Belum terbangun" di §3), dan sampai saat itu peta tidak
mengklaim verifikasi kolektif atas laporan tunggal.

### 4.3 "Area Kamu" dan posisi berkelanjutan

Izin lokasi diminta **saat aplikasi dibuka** (`MainShell`), bukan menunggu
pengguna membuka alur pelaporan. Setelah diberikan, `LocationRepository.locationUpdates()`
mengalirkan posisi tiap 5 detik atau setiap perpindahan 10 meter, mana yang
lebih dulu tercapai. Aliran berhenti sendiri saat tidak ada layar yang
mengamatinya, jadi GPS tidak terus menyala.

Sebelumnya aplikasi hanya memanggil `getCurrentLocation()` sekali, sehingga
titik pengguna membeku di posisi pertama dan tidak pernah menyusul.

Saat user buka layar Peta:

1. Posisi terkini sudah tersedia dari aliran; tombol lokasi hanya untuk
   memusatkan peta.
2. Tampilkan ringkasan: nama kecamatan saat ini, level risiko area, jumlah titik kritis dalam radius 1 km.
3. Peta auto-zoom & center ke lokasi user.
4. Marker yang muncul berasal dari laporan warga (lihat 4.4).

Kalau permission ditolak: peta tetap bisa digeser manual, ringkasan diganti ke
"Bandar Lampung umum". Titik pengguna tidak ditampilkan sama sekali — tidak
dipalsukan ke pusat kota.

### 4.4 Sumber Data Peta

Saat ini marker di peta **hanya** berasal dari laporan warga (`reports`, kecuali
yang sudah diarsipkan). Enum `HotspotSource` sudah menyediakan `Historis`,
`Cuaca`, dan `Iot`, tetapi ketiganya belum punya pemasok data, sehingga filter
sumber belum bermakna.

Rencana pengisiannya:

1. **Titik historis** — tabel titik berulang hasil pengelompokan laporan
   (rekomendasi P-4 pada laporan audit).
2. **Alert cuaca BMKG** — memakai `rainfall_mm` yang kini sudah tersimpan di
   setiap laporan, plus ambang alert (rekomendasi P-6).
3. **Sensor IoT** — roadmap Tahap 4.

### 4.4b Pemilihan Wilayah Laporan

Master wilayah memuat **20 kecamatan / 126 kelurahan** Kota Bandar Lampung.

Alur pengisian:

1. Setelah pin digeser, `Geocoder` dijalankan untuk menebak wilayah.
2. Tebakan itu **dicocokkan ke master** lewat `AreaMatcher`, yang membuang
   awalan "Kelurahan "/"Kel. "/"Kec. ", menyeragamkan kapitalisasi dan spasi,
   lalu mencari kecocokan. Bila kecamatan tidak dikenali, kelurahan dicari di
   seluruh kota dan hanya diterima bila kecocokannya tunggal.
3. Tebakan yang tidak cocok **tidak dipakai**. Kolom dibiarkan kosong dan
   pengguna memilih sendiri lewat pemilih kecamatan lalu kelurahan.
4. Tombol Lanjut tidak aktif sampai pasangan wilayahnya sah.

Yang diperbaiki dari versi lama:

- `reverseGeocode` mencadangkan kelurahan ke `thoroughfare` (nama jalan) dan
  `featureName`, yang praktis tidak pernah berupa nama kelurahan. Nilai itu
  ditulis apa adanya ke laporan, lalu ditolak validasi **saat pengiriman** —
  dan karena kolomnya read-only, pengguna tidak punya cara memperbaikinya.
- Master wilayah hanya memuat 122 kelurahan; lima kelurahan resmi hilang
  (Tanjung Gading, Gedong Meneng, Gedong Meneng Baru, Pasir Gintung, Gulak
  Galik), satu entri bukan kelurahan resmi ("Nyunyai"), dan dua salah eja
  ("Gedong Pakuon", "Tukik").
- Wilayah ikut terisi dari kelurahan default widget cuaca ketika masih kosong,
  yang sama sekali tidak berhubungan dengan titik yang dipilih pengguna.

### 4.5 Watermark Foto Otomatis

Saat user ambil foto via kamera dari Lapor Lengkap:

1. Capture full-res image dari `ActivityResultContracts.TakePicture`.
2. Resize ke max 1280px sisi terpanjang, JPEG quality 80.
3. Overlay text di pojok kanan bawah: **lat/long (5 desimal) · timestamp**.
   Bila koordinat tidak tersedia, barisnya berbunyi "Lokasi tidak terekam" —
   versi lama mencetak titik cadangan yang dikarang, justru merusak tujuan
   anti-laporan palsu.
4. *(Belum terbangun)* Auto-blur wajah orang lewat via ML Kit on-device.
5. Hasil upload ke Supabase Storage path `report-photos/{timestamp}-{uuid}.jpg`.
   Unggah dilakukan **setelah** baris laporan berhasil tersimpan, supaya insert
   yang gagal tidak meninggalkan berkas yatim di Storage.

Foto dari **galeri** (bukan kamera) dapat metadata flag "Foto dari galeri — tidak diverifikasi lokasi".

### 4.6 Identitas Warga — anonymous auth belum terbangun

Kondisi sekarang:

- Warga memakai kunci anon Supabase biasa; **tidak ada** `user_id` per perangkat.
- Kepemilikan laporan dilacak lokal lewat kolom Room `created_locally`, yang
  tetap bernilai true seumur baris (`local_only` berubah false setelah sync,
  jadi tidak bisa dipakai).
- Laporan dari perangkat lain diklaim lewat **tracking token**: token
  ditampilkan di layar sukses dan bisa dimasukkan kembali di layar Status.
- Login email/password hanya untuk petugas.

Rancangan berikutnya: `supabase.auth.signInAnonymously()` agar tiap perangkat
punya `reporter_id` server-side. Itu prasyarat rate limiting dan verifikasi
gotong-royong.

### 4.7 Offline Draft + Queued Submit

- Tiap kali user berpindah step di Lapor, draft auto-save ke Room `draft_reports`.
- Saat user tekan "Kirim":
  - **Online**: upload foto → insert row → return code → success.
  - **Offline**: simpan ke `pending_submissions`, enqueue `SubmitReportWorker`, tampilkan konfirmasi "Akan dikirim saat online".
- Worker drain queue saat ada koneksi, kirim local notification saat sukses.
- Kalau user kill app sebelum submit, draft tetap ada dan bisa dilanjutin.

### 4.8 Status Tracking

Status flow (sama dengan web):

1. **Menunggu Verifikasi** — default saat baru submit.
2. **Sudah Diverifikasi** — admin sudah cek, valid.
3. **Dijadwalkan** — petugas akan turun.
4. **Sedang Ditangani** — petugas di lokasi.
5. **Selesai** — admin upload foto after, tutup laporan.
6. **Ditolak / Duplikat** — bukan masalah valid / sudah ada laporan sama.

User dapat lihat:
- Status saat ini.
- Timeline tiap step yang sudah dilewati + tanggal/waktu + catatan admin (kalau ada) + foto before/after (kalau ada).
- Step depan ditampilkan sebagai "belum".

Update status dari admin web → Supabase Realtime subscription → UI auto-refresh. Fallback polling 30s saat realtime tidak tersedia.

---

## 5. User Flow Utama

### 5.1 First-Time User (Landing → Lapor → Success)

1. Buka app pertama kali.
2. Splash 1s, background `signInAnonymously()`.
3. Landing area muncul.
4. User memilih aksi "Laporkan Drainase".
5. User memilih jalur **Lapor Cepat** atau **Lapor Lengkap**.
6. Step 1 — Lokasi: pakai "Gunakan lokasi saya" (permission prompt) atau pin manual di peta. Kecamatan/Kelurahan auto-isi dari reverse geocoding.
7. Step 2 — Detail: pilih kategori (1 dari 6), pilih severity (1 dari 4), tulis deskripsi (opsional di Cepat, wajib di Lengkap).
8. Step 3 (Lengkap only) — Foto: ambil kamera atau pilih galeri (1–3 foto). Foto kamera otomatis di-watermark. Nama & kontak opsional.
9. User submit.
10. Success: nomor laporan `ALR-2026-NNNNN`, status awal, opsi lanjut cek status.
11. Balik ke Landing — laporan baru muncul di history.

### 5.2 Returning User Cek Status

1. Buka app → Landing.
2. Buka history laporan (auto dari `user_id`).
3. Pilih salah satu → Status screen langsung load.
4. Atau dari Landing → cari berdasarkan kode manual.

### 5.3 Lihat Peta Risiko

1. Dari Landing → buka Peta Risiko.
2. Permission lokasi (kalau belum granted).
3. Ringkasan "Area kamu" muncul + peta auto-center.
4. Tap marker → detail (lokasi, kategori, level, skor, status, foto kalau ada, link ke detail).
5. Filter berdasarkan sumber data atau level risiko.

### 5.4 Lapor Saat Offline

1. User isi form di area sinyal jelek.
2. Submit.
3. App detect offline → simpan ke queue, tampilkan konfirmasi "Akan dikirim saat online".
4. User boleh tutup app.
5. Beberapa menit kemudian sinyal balik → `SubmitReportWorker` jalan otomatis.
6. Local notification: "Laporan ALR-2026-NNNNN berhasil dikirim".

---

## 6. Data Model

### `reports`

| Field | Tipe | Catatan |
|---|---|---|
Nama kolom di bawah ini adalah nama sebenarnya di Supabase.

| Field | Tipe | Catatan |
|---|---|---|
| id | text | PK, UUID dibuat klien |
| code | text unique | `ALR-YYYY-NNNN` |
| public_tracking_token | text unique | `trk_` + 32 hex. Berfungsi sebagai kredensial akses laporan |
| category | text check | sumbatan / genangan / aliran-lambat / drainase-rusak / bau / lainnya |
| severity | text check | ringan / sedang / parah / kritis |
| description | text | boleh kosong pada Lapor Cepat |
| lat / lng | double | dibatasi ke kotak Kota Bandar Lampung |
| kecamatan / kelurahan | text | wajib terisi, divalidasi terhadap master 20/122 |
| address | text | nullable |
| submission_mode | text check | `Cepat` / `Lengkap` |
| rainfall_mm | double | curah hujan 3 jam BMKG saat kirim; masukan faktor Cuaca |
| status | text check | masuk / diverifikasi / dijadwalkan / ditangani / selesai / ditolak |
| risk_score | int | 0–100, **ditulis trigger**, bukan klien |
| risk_level | text check | Normal / Waspada / Tinggi / Kritis (kapital di awal) |
| completion_photos | jsonb | bukti penyelesaian; wajib terisi sebelum status `selesai` |
| archived_at | timestamptz | diisi otomatis trigger saat status final |
| assigned_officer_id | text FK | → `public.officers(id)` |
| reporter_name / reporter_contact | text | nullable, tidak diekspos ke publik |
| created_at / updated_at | timestamptz | |

Foto laporan ada di tabel `report_photos`, rincian skor di `risk_breakdowns`
(diisi trigger), riwayat di `report_status_history`.

### `report_status_history`

| Field | Tipe |
|---|---|
| id | bigserial PK |
| report_id | UUID FK |
| status | enum |
| note | text nullable |
| photo_url | text nullable (before/after) |
| created_at | timestamptz |

### `historical_hotspots`

Pre-seeded titik rawan dari data kelurahan tahun lalu (id, lat, lng, kecamatan, kelurahan, source, year).

### Konstanta

- **6 kategori**: Sumbatan sampah, Genangan jalan, Aliran lambat, Drainase rusak, Bau tidak sedap, Lainnya.
- **4 severity**: Ringan, Sedang, Parah, Kritis.
- **20 kecamatan Bandar Lampung** + kelurahan masing-masing (data dari `LaporPage.jsx` web repo, constant `KECAMATAN_DATA`).
- **Risk score formula**: 30% severity + 25% kategori + 20% frekuensi laporan sekitar + 15% kedekatan fasilitas publik + 10% umur laporan. Komponen "bukti foto" sebagai multiplier ×1.0 (Lengkap) atau ×0.7 (Cepat).
- **Risk levels**: 0–39 Normal, 40–59 Waspada, 60–79 Tinggi, 80–100 Kritis.

---

## 7. Behavior Khusus

### Permissions

- **Lokasi (FINE)**: diminta saat user pakai "Gunakan lokasi saya" atau buka Peta. Kalau ditolak: tetap bisa pin manual, fallback center Bandar Lampung.
- **Kamera**: diminta saat user ambil foto di Lapor Lengkap. Kalau ditolak: hanya galeri yang aktif.
- **Notifications** (Android 13+): diminta sekali sesudah submit pertama, untuk notif status & queued submit. Skip-able.

### Network States

- **Offline**: app tetap fungsional dalam mode draft + queue.
- **Slow network**: loading indicator state untuk peta & list.
- **Server error**: bisa retry.

### Error Handling

- Form validation realtime, submit disabled sampai field wajib valid.
- Upload foto gagal: retry 3× otomatis, baru tampil error dengan opsi "Coba lagi".
- Map gagal load: fallback ke list per kecamatan dengan info risiko.

### Localization

v1 **Bahasa Indonesia only**. String tersentral untuk persiapan English nanti.

### Aksesibilitas (behavioral, bukan visual)

- Semua aksi penting reachable via TalkBack dengan contentDescription yang jelas.
- Min touch target 44×44 dp (Material spec).
- Status & risk tidak bergantung hanya pada warna (selalu ada teks pendamping).

---

## 8. Tech Stack

- **UI**: Jetpack Compose.
- **Navigation**: Compose Navigation, single-Activity.
- **State**: ViewModel + Kotlin Flow.
- **DI**: Hilt.
- **Backend**: Supabase (Postgres + Auth + Storage + Realtime).
- **Local DB**: Room.
- **Background work**: WorkManager.
- **Map**: osmdroid (OSM tiles, sama dengan web).
- **Image**: Coil + custom watermark composer.
- **Min SDK**: 24 (Android 7.0). Target: 34.
- **Bahasa**: Kotlin.

---

## 9. Konten / Microcopy Kunci

CTA:
- "Laporkan Drainase"
- "Lihat Peta Risiko"
- "Cek Status Laporan"
- "Gunakan Lokasi Saya"
- "Kirim Laporan"

Status (label di UI):
- Menunggu Verifikasi · Sudah Diverifikasi · Dijadwalkan · Sedang Ditangani · Selesai · Ditolak/Duplikat

Empty state:
- "Belum ada laporan di wilayah ini."
- "Kamu belum membuat laporan."
- "Tidak ada titik risiko tinggi hari ini."

Error:
- "Foto belum berhasil diunggah. Coba pakai ukuran lebih kecil."
- "Lokasi belum dipilih."
- "Deskripsi minimal 10 karakter."
- "Akses lokasi ditolak. Kamu masih bisa pin lokasi di peta."

Success:
- "Laporan berhasil dikirim. Nomor: ALR-2026-NNNNN"
- "Status awal: Menunggu Verifikasi"
- "Cek status kapan saja di menu Status Laporan."

---

## 10. Hand-Off Catatan

Yang sudah fix dari spec ini (jangan diubah saat tahap desain):

- [x] 5 area fungsional (Landing, Lapor, Peta, Status, Tentang) — boleh ditambah sub-screen
- [x] Lapor Cepat vs Lapor Lengkap sebagai entry point pertama di flow Lapor
- [x] Ringkasan "Area kamu" di Peta
- [x] Timeline 6-step di Status
- [x] Risk level 4 tingkat (Normal, Waspada, Tinggi, Kritis)
- [x] 6 kategori, 4 severity, 20 kecamatan
- [x] Bahasa Indonesia, mobile portrait, anonymous auth
- [x] Microcopy di section 9 (label CTA, status, error, success)

Yang sepenuhnya bebas ditentukan saat tahap desain:

- Layout & komposisi visual tiap layar.
- Warna, font, ikon, ilustrasi, motion.
- Bottom navigation vs drawer vs back-stack murni.
- Onboarding flow (kalau perlu).
- Splash screen visual.
- Komponen UI spesifik (kartu vs list vs tabbed vs accordion, dll).

---

## 11. Referensi

- PRD lengkap: `_alirin_web/PRD_ALIRIN_v1.md`
- Repo web (sumber kebenaran data & flow): https://github.com/odlaver/alirin
