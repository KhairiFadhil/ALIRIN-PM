# Contributing to ALIRIN Mobile

Terima kasih telah tertarik berkontribusi pada **ALIRIN Mobile**! Sebagai proyek
*civic-tech*, setiap kontribusi Anda membantu menjaga penanganan drainase mikro
di Bandar Lampung tetap solid dan andal.

---

## 🛠️ Alur Kontribusi

1. **Fork Repositori** ke akun GitHub Anda.

2. **Clone & Buat Branch Baru**
   ```bash
   git clone https://github.com/USERNAME/ALIRIN-PM.git
   cd ALIRIN-PM
   git checkout -b feature/nama-fitur-anda
   # atau
   git checkout -b bugfix/nama-bugfix-anda
   ```

3. **Siapkan Lingkungan**
   - Buka di Android Studio terbaru (JDK 11+, Android SDK minSdk 24).
   - Isi `local.properties` (lihat [README](README.md)); berkas ini
     di-*gitignore* dan tidak boleh ikut ter-commit.

4. **Lakukan Perubahan & Uji**
   - Pastikan build dan uji lulus:
     ```bash
     ./gradlew assembleDebug
     ./gradlew testDebugUnitTest
     ```
   - Logika bisnis (skoring, pencocokan wilayah, sinyal) ditempatkan di
     `data/` dan diberi uji unit di `app/src/test/`, terpisah dari UI Compose.
   - **RiskEngine** di aplikasi harus tetap menjadi cermin rumus di basis data.
     Bila mengubah rumus, ubah juga migrasi Supabase dan
     [`docs/RISK-ENGINE.md`](https://github.com/odlaver/alirin/blob/main/docs/RISK-ENGINE.md)
     agar tiga implementasi (web, mobile, SQL) tetap sama.

5. **Commit & Push**
   - Gunakan [Conventional Commits](https://www.conventionalcommits.org/):
     ```bash
     git commit -m "feat(lapor): tambah pratinjau titik di peta"
     git push origin feature/nama-fitur-anda
     ```

6. **Buat Pull Request** ke branch `main` repositori asli, dengan deskripsi
   jelas: apa yang diubah, mengapa, dan apa yang diuji.

---

## 💻 Aturan Penulisan Kode

- **Arsitektur:** MVVM. UI (Compose) di `feature/`, data & logika di `data/`.
  Jangan menaruh logika bisnis di dalam composable.
- **Tema & warna:** pakai design token di `ui/theme/` (mis. `Primary`, `Ink`,
  `RiskKritisDot`). Jangan menaruh warna heksadesimal langsung di layar.
- **Data nyata, bukan patokan:** angka yang ditampilkan (statistik, notifikasi,
  cuaca) harus dihitung dari data, bukan nilai tetap.
- **Format & lint:** ikuti gaya Kotlin resmi; pastikan build bersih tanpa
  peringatan baru.

---

## 💬 Melaporkan Bug / Mengusulkan Fitur

Buka **GitHub Issue** baru dengan langkah reproduksi yang jelas (untuk bug) atau
konteks kebutuhan (untuk fitur). Untuk celah keamanan, ikuti
[SECURITY.md](SECURITY.md) — jangan buka di Issue publik.

*Semoga kontribusi Anda berdampak nyata untuk penanganan banjir tingkat mikro!*
