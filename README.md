<div align="center">
  <h1>🌊 ALIRIN Mobile</h1>
  <p><strong>Aplikasi Warga & Petugas — Pelaporan dan Pemetaan Risiko Drainase Mikro</strong></p>

  [![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![Supabase](https://img.shields.io/badge/Supabase-3ECF8E?style=for-the-badge&logo=supabase&logoColor=white)](https://supabase.com/)
  [![Android](https://img.shields.io/badge/Android%207.0+-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#)
  [![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

</div>

---

**ALIRIN Mobile** adalah aplikasi Android untuk **warga** dan **petugas lapangan**
dalam ekosistem ALIRIN — sistem *civic-tech* pemetaan risiko drainase mikro Kota
Bandar Lampung. Warga melapor genangan/sumbatan dengan cepat dan presisi; petugas
memverifikasi, menangani, dan menutup laporan di lapangan dengan bukti foto.

Aplikasi ini berbagi satu backend Supabase dengan [**ALIRIN Web**](https://github.com/odlaver/alirin)
(dashboard admin). Risk score, status, dan data laporan **selalu identik** di
kedua sisi karena dihitung di basis data, bukan di masing-masing klien.

## ✨ Fitur Utama

1. **Pelaporan Warga (`feature/lapor`)**
   - **Lapor Cepat** untuk kondisi mendesak dan **Lapor Lengkap** dengan foto &
     deskripsi, sesuai Proposal 4.3.1.
   - Lokasi otomatis dari GPS atau pilih titik di peta (osmdroid). Kecamatan &
     kelurahan terisi dari kelurahan terdekat; bisa dipilih manual.
   - Koordinat di luar Kota Bandar Lampung ditolak; wilayah wajib sah sebelum
     lanjut.

2. **Beranda Dinamis (`feature/beranda`)**
   - Kartu prakiraan cuaca 3 jam BMKG **mengikuti lokasi pengguna**.
   - Analisis AI (via Edge Function) dengan baseline berbasis aturan sebagai
     cadangan; label sumber ditampilkan apa adanya.
   - Titik rawan terdekat, alert risiko, dan notifikasi laporan milik sendiri.

3. **Peta Risiko (`feature/peta`)**
   - Marker berwarna sesuai level risiko (Kritis/Tinggi/Waspada/Normal).
   - Konteks hulu–hilir: hujan di wilayah atas ikut menaikkan risiko hilir.

4. **Pelacakan Status (`feature/status`)**
   - Lacak laporan lewat token, atau lihat **"Laporan Saya"** tanpa token lewat
     identitas anonim per perangkat.
   - Rincian skor per faktor dan penilaian AI ditampilkan berdampingan.

5. **Portal Petugas (`feature/staff`)**
   - Inbox tugas, verifikasi lapangan berfoto, pembaruan status, dan statistik
     dari data nyata.
   - Penutupan pekerjaan wajib foto bukti — aturannya ditegakkan di basis data.

6. **Offline-first**
   - Room sebagai cache lokal + outbox. Laporan tetap tersimpan dan menyusul
     sinkron saat jaringan kembali.

## 🛠️ Tech Stack

- **Bahasa:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Arsitektur:** MVVM (ViewModel + StateFlow), repository pattern
- **Backend:** Supabase (PostgREST, Auth, Storage, Realtime) via `supabase-kt`
- **Lokal:** Room (offline cache & outbox)
- **Jaringan:** Retrofit + kotlinx.serialization (BMKG & Edge Function)
- **Peta:** osmdroid + OpenStreetMap
- **Lokasi:** FusedLocationProvider (lokasi berkelanjutan)

## 🚀 Build & Menjalankan

### Prasyarat
- [Android Studio](https://developer.android.com/studio) (versi terbaru)
- JDK 11+
- Android SDK — **minSdk 24 (Android 7.0)**, targetSdk 36
- Project Supabase (lihat [ALIRIN Web → supabase/](https://github.com/odlaver/alirin/tree/main/supabase)
  untuk menyiapkan skema backend)

### Langkah

1. **Kloning**
   ```bash
   git clone https://github.com/KhairiFadhil/ALIRIN-PM.git
   cd ALIRIN-PM
   ```

2. **Konfigurasi `local.properties`** (berkas ini di-*gitignore*, tidak ikut
   ter-commit):
   ```properties
   sdk.dir=/path/ke/Android/Sdk
   SUPABASE_URL=https://<project-ref>.supabase.co
   SUPABASE_PUBLISHABLE_KEY=sb_publishable_xxx
   ```
   > Kunci Groq **tidak** dipasang di sini. Analisis AI berjalan di Edge
   > Function sisi server, jadi tidak ada kunci AI di dalam APK.
   > Lihat [`docs/MENGAKTIFKAN-AI.md`](docs/MENGAKTIFKAN-AI.md).

3. **Build**
   ```bash
   ./gradlew assembleDebug          # APK debug di app/build/outputs/apk/debug/
   ./gradlew testDebugUnitTest      # uji unit
   ```

   Atau buka di Android Studio → Run.

## 📁 Struktur Direktori

```text
app/src/main/java/com/example/alirinmobile/
├── data/
│   ├── auth/           # Sesi & peran (warga anonim, petugas, admin)
│   ├── local/          # Room: entity, DAO, mapper
│   ├── network/        # Retrofit: DTO & service (BMKG, Edge Function)
│   ├── repository/     # Report, Weather, Location, Upstream, Alerts, dll.
│   ├── scoring/        # RiskEngine — cermin rumus di basis data
│   └── sync/           # Sinkronisasi & outbox
├── feature/            # Layar per fitur (beranda, lapor, peta, status, staff)
├── nav/                # AlirinNavHost — routing & izin lokasi saat buka
└── ui/                 # Komponen & tema (design token)
```

## 🔗 Ekosistem ALIRIN

| Repositori | Peran |
|---|---|
| [**ALIRIN Web**](https://github.com/odlaver/alirin) | Dashboard admin/pemerintah + skema Supabase (backend) |
| **ALIRIN Mobile** (repo ini) | Aplikasi warga & petugas |

Backend, RLS, dan Risk Engine hidup di repo Web pada folder
[`supabase/`](https://github.com/odlaver/alirin/tree/main/supabase). Siapkan
backend di sana lebih dulu sebelum menjalankan aplikasi ini.

## 🤝 Kontribusi
Silakan baca [CONTRIBUTING.md](CONTRIBUTING.md) untuk alur *fork*, konvensi
*commit*, dan pengiriman *Pull Request*.

## 🔒 Keamanan
Menemukan celah? Lihat [SECURITY.md](SECURITY.md).

## 📄 Lisensi
Didistribusikan di bawah lisensi MIT. Lihat [LICENSE](LICENSE).
