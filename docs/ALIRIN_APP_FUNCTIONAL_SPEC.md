# ALIRIN Mobile — Functional Spec (Citizen App)

Dokumen ini menjelaskan **cara kerja** ALIRIN Mobile (Android, sisi warga). Semua urusan UI/visual/layout/komponen tidak dibahas di sini — itu jatahnya tahap desain terpisah (Claude Design).

Versi: v1 · Tanggal: 2026-05-19 · Platform: Android

---

## 1. Ringkasan Produk

ALIRIN = sistem kota cerdas Bandar Lampung untuk monitoring & prioritas preventif drainase mikro yang berpotensi menyebabkan genangan.

Versi web (sudah ada di github.com/odlaver/alirin) punya 2 sisi: **warga** (lapor) + **admin** (validasi & tindak lanjut). App mobile ini **hanya sisi warga** — admin tetap di web, datanya nyambung lewat backend bersama.

Tujuan utama: warga bisa lapor drainase langsung dari HP dalam **< 2 menit**, plus monitor titik rawan di sekitar lokasinya.

Konteks lomba: GEMASTIK Smart City — produk harus bertindak sebagai *smart city tool*, bukan app lapor generik.

---

## 2. Target User

- **Warga umum** Bandar Lampung (mahasiswa, pekerja, pemilik usaha kecil, ibu rumah tangga, dll).
- Akses tanpa daftar (anonymous); tiap device punya identitas server-side (Supabase anonymous auth).
- Konteks penggunaan: di lokasi laporan, sering pas hujan atau setelah hujan, koneksi bisa jelek.

---

## 3. Scope v1

### ✅ In scope

1. 5 area fungsional: Landing/home, Lapor, Peta Risiko, Status Laporan, Tentang.
2. **Lapor Cepat** (tanpa foto) **vs Lapor Lengkap** (dengan foto, skor lebih tinggi).
3. Watermark foto otomatis (lat/long + timestamp) untuk anti-laporan palsu.
4. Peta risiko dengan fitur **"Area kamu"** — fokus titik dalam 1 km dari lokasi pengguna.
5. Sumber data peta gabungan: laporan warga + data historis kelurahan + data cuaca BMKG + IoT placeholder.
6. **Verifikasi gotong-royong**: 3+ laporan dalam radius 100 m / 24 jam → auto "Diverifikasi Warga".
7. Offline draft + queued submit (WorkManager).
8. Anonymous Supabase auth.
9. Status tracking dengan timeline (Menunggu → Diverifikasi → Dijadwalkan → Ditangani → Selesai / Ditolak).
10. History laporan tersimpan otomatis per device.

### ❌ Out of scope v1

- Admin web (sudah ada terpisah).
- Push notification status (v1.1).
- ML auto-deteksi kategori dari foto (v2).
- Sensor IoT fisik (cuma placeholder data).
- Akun real (phone/email login) — sementara anonymous.
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
| Bobot skor risiko | Komponen "bukti foto" = 0 | Komponen "bukti foto" = full |
| Auto-tag | "Tanpa verifikasi visual" | "Dengan bukti foto" |

Alasan: warga sering mager foto. Daripada tidak lapor sama sekali, mending lapor cepat — kalau ada 3+ laporan cepat di area yang sama, sistem auto-verifikasi gotong-royong.

### 4.2 Verifikasi Gotong-Royong

Aturan: jika dalam **100 m** dan **24 jam** ada **≥ 3 laporan** (baik Cepat maupun Lengkap, dari user_id berbeda), sistem otomatis:

- Set status semua laporan tsb. → **"Diverifikasi Warga"**.
- Bump skor risiko +15 (komponen "frekuensi laporan sekitar").
- Cluster di peta diberi label "3 warga melaporkan".

Implementasi: trigger Postgres pada `INSERT reports` yang query laporan terdekat via PostGIS `ST_DWithin`.

### 4.3 "Area Kamu"

Saat user buka layar Peta:

1. Minta permission lokasi sekali, dengan alasan jelas.
2. Tampilkan ringkasan: nama kecamatan saat ini, level risiko area, jumlah titik kritis dalam radius 1 km.
3. Peta auto-zoom & center ke lokasi user.
4. Marker yang muncul = gabungan dari 3 sumber (lihat 4.4).

Kalau permission ditolak: fallback ke center Bandar Lampung (lat -5.3971, lng 105.2668), ringkasan diganti ke "Bandar Lampung umum".

### 4.4 Sumber Data Peta (multi-source)

Marker di peta = union dari:

1. **Laporan warga** (`reports` table, status `verified`/`in_progress` atau hasil verifikasi gotong-royong).
2. **Titik historis** — di-seed dari data tahun lalu per kelurahan (CSV import sekali, stored di `historical_hotspots`).
3. **Alert cuaca BMKG** — fetch dari API BMKG/Open-Meteo per kecamatan; kalau curah hujan > threshold, tampilkan marker "Potensi Rawan Hari Ini".
4. **IoT placeholder** — beberapa marker dummy bertanda IoT buat menunjukkan roadmap v2.

User bisa filter sumber: Laporan / Historis / Cuaca / Sensor.

### 4.5 Watermark Foto Otomatis

Saat user ambil foto via kamera dari Lapor Lengkap:

1. Capture full-res image dari `ActivityResultContracts.TakePicture`.
2. Resize ke max 1280px sisi terpanjang, JPEG quality 80.
3. Overlay text di pojok kanan bawah: **lat/long (5 desimal) · timestamp ISO**.
4. Auto-blur wajah orang lewat (face detection via ML Kit on-device).
5. Hasil upload ke Supabase Storage path `report-photos/{user_id}/{report_id}/{n}.jpg`.

Foto dari **galeri** (bukan kamera) dapat metadata flag "Foto dari galeri — tidak diverifikasi lokasi".

### 4.6 Anonymous Auth

- First launch: `supabase.auth.signInAnonymously()` → dapat `user_id` UUID.
- Cache `user_id` di DataStore.
- Semua laporan terikat `user_id` ini.
- History laporan = `SELECT * FROM reports WHERE user_id = current_user_id`.
- Tidak ada form login/register di v1.

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
| id | UUID | PK |
| report_code | text unique | `ALR-YYYY-NNNNN` |
| user_id | UUID | FK auth.users (anonymous) |
| category | enum | sumbatan / genangan / aliran-lambat / drainase-rusak / bau / lainnya |
| severity | enum | ringan / sedang / parah / kritis |
| description | text | nullable kalau Lapor Cepat |
| latitude | double | |
| longitude | double | |
| kecamatan | text | |
| kelurahan | text | |
| alamat | text | nullable |
| photo_urls | text[] | empty kalau Lapor Cepat |
| report_type | enum | `cepat` / `lengkap` |
| status | enum | pending / verified / scheduled / in_progress / completed / rejected |
| risk_score | int | 0–100 |
| risk_level | enum | normal / waspada / tinggi / kritis |
| reporter_name | text | nullable |
| reporter_contact | text | nullable |
| created_at | timestamptz | |
| updated_at | timestamptz | |

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
