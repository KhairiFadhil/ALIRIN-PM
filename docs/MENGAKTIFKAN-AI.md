# Mengaktifkan AI-Assisted Analysis

Status per 26 Agustus 2026: **AI berjalan di Edge Function, bukan lagi di
perangkat**.

Sebelum P-1, kunci Groq ditanam ke APK lewat `buildConfigField`. Siapa pun yang
mengunduh APK bisa mengekstraknya — metode yang sama dipakai untuk
memverifikasinya. Sekarang kuncinya hanya ada sebagai secret project Supabase,
dan aplikasi cukup memanggil fungsinya.

| Komponen | Status | Bukti |
|---|---|---|
| Kunci Groq di APK | **Sudah tidak ada** | Pemindaian seluruh 15 berkas dex dan entri lain: tidak ada `gsk_` maupun `api.groq.com` |
| Edge Function `weather-brief` | Kartu prakiraan untuk mobile **dan** web | Label sumber di kartu menyebut `ai` atau `baseline` apa adanya |
| Edge Function `assess-risk` | Penilai risiko pembanding | Menulis kolom `ai_*`, tidak menyentuh `risk_score` |
| BMKG prakiraan cuaca | Berfungsi | 126 kode `adm4` dibaca langsung dari endpoint BMKG |

## Yang tidak berubah: skor baseline

`risk_score` tetap ditulis trigger `alirin_apply_risk` dan tetap satu-satunya
angka yang dipakai mengurutkan penanganan. AI menulis kolom terpisah
(`ai_risk_score`, `ai_risk_reason`, `ai_recommendations`, `ai_model`,
`ai_assessed_at`) yang **ditampilkan berdampingan** dengan baseline.

Itu bukan setengah-setengah, melainkan janji Proposal §4.3.4 yang berbunyi AI
"dibandingkan dengan baseline serta verifikasi lapangan". Menampilkan satu angka
saja akan menghapus perbandingannya, dan sistem kehilangan kemampuan menjawab
"kenapa skornya segini" dengan pasti. Selisih keduanya justru bahan mentah untuk
evaluasi akurasi yang dijanjikan §4.4.

## Langkah memasang

### 1. Ambil kunci baru

Buat API key di <https://console.groq.com/keys>. Bentuknya `gsk_...`.

Kunci lama harus **dicabut**: ia terlanjur ikut ke setiap APK yang pernah
dibangun dengannya.

### 2. Uji kunci dan model sebelum dipasang

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

Harus menjawab `200`. Bila `model_not_found` atau `model_decommissioned`, ganti
model — lihat bagian berikutnya.

### 3. Pasang sebagai secret project

```bash
cd C:\ALIRIN
npx supabase login
npx supabase link --project-ref prfgbvepsyfjwyctgeeq
npx supabase secrets set GROQ_API_KEY=gsk_kunci_anda
npx supabase secrets set GROQ_MODEL=openai/gpt-oss-20b
npx supabase secrets set GROQ_REASONING_EFFORT=low
```

### 4. Terbitkan fungsinya

```bash
npx supabase functions deploy weather-brief
npx supabase functions deploy assess-risk
```

### 5. Periksa

```bash
cd app && npm run supabase:status
```

Di aplikasi, kartu Beranda berubah judul menjadi **"ANALISIS AI · 3 JAM KE
DEPAN"** dengan label sumber **"Analisis AI"**. Selama masih berbunyi "Aturan
baseline", fungsi berjalan tetapi Groq-nya belum menyala.

Alasan kegagalan tercetak di log fungsi:

```bash
npx supabase functions logs weather-brief
npx supabase functions logs assess-risk
```

## Soal pilihan model

Proposal menyebut **Llama 3.1 8B** (`llama-3.1-8b-instant`). Model itu
**terkonfirmasi tidak tersedia**: permintaan dijawab HTTP 404 dan namanya tidak
muncul di daftar model akun. Halaman deprecations Groq mencantumkan tanggal
shutdown 16 Agustus 2026 dengan pengganti `openai/gpt-oss-20b`.

Model yang dipakai sekarang: **`openai/gpt-oss-20b`**. Bisa diganti lewat secret
`GROQ_MODEL` tanpa menyentuh kode dan tanpa membangun ulang APK — itu keuntungan
lain dari memindahkannya ke server.

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
Kosongkan `GROQ_REASONING_EFFORT` bila mengganti ke model yang menolak parameter
ini.

## Bila kunci belum dipasang

Bukan kegagalan. Kedua fungsi mengembalikan baseline berbasis aturan sesuai
Proposal §4.3.4, dan kartu prakiraan menyebut sumbernya apa adanya.

Yang harus dihindari: kartu berlabel "Analisis AI" yang isinya sebenarnya
`if-else`. Label sumber di kartu mengikuti apa yang benar-benar terjadi di
server, bukan apa yang ingin ditampilkan.

## Batas kuota

Tetap pasang batas kuota di dashboard Groq. Fungsi `assess-risk` menolak menilai
ulang laporan yang sama dalam 10 menit, sehingga satu laporan tidak bisa dipakai
menguras kuota lewat panggilan berulang — tetapi batas kuota tetap lapisan
terakhir yang paling bisa diandalkan.
