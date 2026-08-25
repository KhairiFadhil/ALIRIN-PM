# Mengaktifkan AI-Assisted Analysis (Groq)

Status per 26 Agustus 2026: **belum aktif**. Aplikasi berjalan dengan baseline
berbasis aturan, dan kartu prakiraan menyebut sumbernya apa adanya
("PRAKIRAAN BMKG · Aturan baseline"). Ini sejalan dengan Proposal §4.3.4:
*"Jika AI gagal, sistem beralih ke rule-based baseline yang dapat diaudit."*

| Komponen | Status | Bukti |
|---|---|---|
| BMKG prakiraan cuaca | **Berfungsi** | Endpoint dites langsung untuk 4 kelurahan, semuanya menjawab |
| BMKG → risk score | **Berfungsi** | Kolom `rainfall_mm` terisi saat laporan dikirim, jadi faktor Cuaca 25% |
| Groq AI | **Tidak aktif** | `GROQ_API_KEY` kosong; tidak ada kunci di APK |

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

Proposal menyebut **Llama 3.1 8B** (`llama-3.1-8b-instant`). Per 26 Agustus
2026, halaman deprecations Groq mencantumkan model itu dengan tanggal shutdown
**16 Agustus 2026** dan menyarankan `openai/gpt-oss-20b` sebagai pengganti;
halaman models masih menampilkannya tetapi bertanda **Enterprise**.

Artinya, akun Groq biasa kemungkinan besar **tidak bisa lagi** memakai model
yang tertulis di proposal. Default kode karena itu `openai/gpt-oss-20b`, yang
paling cepat dan paling murah di tier standar.

Konsekuensi untuk berkas lomba: bila AI diaktifkan, kalimat proposal yang
menyebut "Llama 3.1 8B" perlu disesuaikan dengan model yang benar-benar
dipakai. Nama model bisa diganti lewat `GROQ_MODEL` tanpa menyentuh kode.

## Peringatan keamanan

`buildConfigField` menaruh kunci sebagai string biasa di dalam DEX. Siapa pun
yang mengunduh APK bisa mengekstraknya — metode yang sama dipakai untuk
memverifikasi bahwa kunci saat ini memang belum ada.

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
