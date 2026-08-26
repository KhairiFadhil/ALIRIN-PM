# Kebijakan Keamanan (Security Policy) — ALIRIN Mobile

Kami serius menjaga keamanan data pelaporan warga dan integritas sistem ALIRIN.
Jika Anda menemukan celah keamanan, ikuti panduan di bawah ini.

---

## ⚠️ Catatan Prototipe

> [!IMPORTANT]
> ALIRIN Mobile saat ini berstatus **prototipe/purwarupa** yang terhubung ke
> backend Supabase. Akses data dijaga oleh **Row Level Security (RLS)**: warga
> hanya melihat laporannya sendiri, koordinat presisi hanya untuk staf, dan
> peran ditentukan dari `app_metadata` yang tidak bisa ditulis pengguna.
>
> Meski demikian, sistem ini **belum untuk produksi skala penuh**. APK debug
> membawa *publishable key* Supabase — kunci ini memang dirancang untuk sisi
> klien dan dijaga RLS, bukan rahasia. Kunci AI (Groq) **tidak** ada di dalam
> APK; analisis AI berjalan di Edge Function sisi server.

---

## 🛡️ Versi yang Didukung

| Versi | Didukung |
| --- | --- |
| v1.0.x | Didukung (rilis saat ini) |
| < v1.0.0 | Tidak didukung |

---

## 🔒 Melaporkan Celah Keamanan

Harap **jangan membuka GitHub Issue publik** untuk celah kritis atau sensitif,
guna mencegah penyalahgunaan sebelum perbaikan tersedia.

Langkah pelaporan:

1. Kirim laporan detail melalui kontak pemilik repositori GitHub (atau email
   pengembang utama).
2. Sertakan:
   - Deskripsi jenis kerentanan (mis. bypass RLS, kebocoran data lintas
     pengguna, eskalasi peran, kebocoran kredensial).
   - Langkah reproduksi (*Proof of Concept*).
   - Potensi dampak terhadap pengguna atau integritas sistem.
   - Usulan perbaikan bila ada.

### Waktu Respons
Kami berupaya meninjau laporan dalam **48 jam** dan memberi kabar progres secara
berkala kepada pelapor.

Terima kasih telah membantu menjaga keamanan ekosistem civic-tech ALIRIN!
