# Mengaktifkan AI-Assisted Analysis (Groq)

Status per 26 Agustus 2026: **aktif dan terverifikasi ujung ke ujung**.

| Komponen | Status | Bukti |
|---|---|---|
| BMKG prakiraan cuaca | **Berfungsi** | Endpoint dites langsung untuk 4 kelurahan, semuanya menjawab |
| BMKG → risk score | **Berfungsi** | Kolom `rainfall_mm` terisi saat laporan dikirim, jadi faktor Cuaca 25% |
| Kunci Groq | **Valid** | Berhasil membaca daftar model akun (13 model) |
| Model `openai/gpt-oss-20b` | **Tersedia** | Ada di daftar model akun; prompt ALIRIN menghasilkan JSON lengkap |
| Model `llama-3.1-8b-instant` | **Tidak tersedia** | HTTP 404, dan tidak ada di daftar model akun |

Bila panggilan AI gagal, sistem tetap beralih ke baseline berbasis aturan sesuai
Proposal §4.3.4, dan alasannya kini tercetak di logcat.

## Langkah mengaktifkan

### 1. Ambil kunci

Buat API key di <https://console.groq.com/keys>. Bentuknya `gsk_...`.

### 2. Uji kunci dan model SEBELUM dipasang

```bash
curl https://api.groq.com/openai/v1/chat/completions \
  -H "Authorization: Bearer gsk_KUNCI_ANDA" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "openai/gpt-oss-20b",
    "response_format": { "type": "json_object" },
    "messages": [{ "role": "user", "content": "Balas JSON {\"ok\":true}" }]
  }'
```

Harus menjawab `200` dengan isi JSON. Bila menjawab `model_not_found` atau
`model_decommissioned`, ganti model — lihat bagian berikutnya.

### 3. Pasang di `local.properties`

```properties
GROQ_API_KEY=gsk_kunci_anda
GROQ_MODEL=openai/gpt-oss-20b
GROQ_REASONING_EFFORT=low
```

Berkas ini sudah masuk `.gitignore`, jadi kuncinya tidak ikut ter-commit.

### 4. Bangun ulang dan cek

```bash
gradlew assembleDebug
```

Di aplikasi, kartu Beranda berubah judul menjadi **"ANALISIS AI · 3 JAM KE
DEPAN"** dengan label sumber **"Analisis AI"**. Selama masih berbunyi
"Aturan baseline", AI belum benar-benar terpakai.

Bila gagal, alasannya tercetak di logcat:

```bash
adb logcat -s AlirinPrediction
```

## Soal pilihan model

Proposal menyebut **Llama 3.1 8B** (`llama-3.1-8b-instant`). Model itu
**terkonfirmasi tidak tersedia** untuk akun ini: permintaan dijawab HTTP 404,
dan namanya tidak muncul di daftar model akun. Halaman deprecations Groq
mencantumkan tanggal shutdown 16 Agustus 2026 dengan pengganti
`openai/gpt-oss-20b`.

Model yang dipakai sekarang: **`openai/gpt-oss-20b`**, tercepat dan termurah di
tier standar. Bisa diganti lewat `GROQ_MODEL` tanpa menyentuh kode.

Konsekuensi untuk berkas lomba: kalimat proposal yang menyebut "Llama 3.1 8B"
perlu disesuaikan dengan model yang benar-benar dipakai.

## Soal reasoning_effort dan max_tokens

Model gpt-oss memancarkan bidang `reasoning` yang ikut memakan anggaran token
**sebelum** JSON-nya selesai ditulis. Hasil pengujian pada prompt ALIRIN yang
sebenarnya:

| reasoning_effort | max_tokens | Hasil | Token keluaran |
|---|---|---|---|
| bawaan | 512 | **Gagal** — `Failed to validate JSON` (terpotong) | — |
| bawaan | 2048 | Berhasil | ~1051 |
| `low` | 512 | Berhasil | ~239 |
| `low` | 1024 | Berhasil | ~269 |

Konfigurasi yang dipakai: `reasoning_effort=low` dengan `max_tokens=1024`.
Empat kali lebih hemat token daripada effort bawaan, dan punya kelonggaran yang
cukup. Nilai `512` yang dipakai versi sebelumnya gagal berulang pada skenario
hujan — dan gagalnya tidak terlihat karena semua error ditelan diam-diam.

Kosongkan `GROQ_REASONING_EFFORT=` bila mengganti ke model yang menolak
parameter ini.

## Peringatan keamanan

`buildConfigField` menaruh kunci sebagai string biasa di dalam DEX. Siapa pun
yang mengunduh APK bisa mengekstraknya — metode yang sama dipakai untuk
memverifikasi bahwa kunci sekarang benar-benar tertanam.

**Rotasi kunci sebelum APK dibagikan ke luar tim.** Kunci yang pernah dikirim
lewat kanal percakapan, ditempel di dokumen, atau ikut dalam APK yang beredar
harus dianggap sudah bocor. Batasi juga kuotanya di dashboard Groq supaya
penyalahgunaan tidak berujung tagihan.

Ini dapat diterima selama masih prototipe dan kuncinya berkuota kecil. Untuk
pemakaian sungguhan, panggilan Groq harus pindah ke Supabase Edge Function
sehingga kuncinya tidak pernah ada di perangkat. Lihat rekomendasi P-1 pada
laporan audit.

## Yang berubah bila AI aktif

Yang **tidak** berubah: risk score. AI saat ini hanya mengisi kartu prakiraan
(ringkasan + rekomendasi), bukan menilai risiko laporan. Menjadikan AI sebagai
penilai risiko adalah rekomendasi P-1 (roadmap Tahap 3), dan rancangannya
menyimpan skor AI berdampingan dengan skor baseline supaya keduanya bisa
dibandingkan dan diaudit.
