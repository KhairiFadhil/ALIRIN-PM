package com.example.alirinmobile.data.repository

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// P-6 - Alert saat risiko melewati ambang.
//
// Dua sumber, keduanya dibuat trigger di server:
//   - "skor": laporan yang skornya melewati 80 (Kritis).
//   - "hulu": hujan lebat terdeteksi di kecamatan hulu, memperingatkan hilir.
//
// Warga adalah penerima alert (Proposal), jadi tabelnya publik-baca; hanya baris
// active yang terlihat dan tidak ada data pribadi di dalamnya.

@Serializable
data class AlertRow(
    val id: String,
    val jenis: String,
    val kecamatan: String? = null,
    val kelurahan: String? = null,
    val pesan: String,
    val skor: Int? = null,
    @SerialName("created_at") val createdAt: String,
)

class AlertsRepository(private val supabase: SupabaseClient) {

    suspend fun active(kecamatan: String? = null): List<AlertRow> = runCatching {
        supabase.from("alerts")
            .select {
                filter {
                    eq("active", true)
                    if (!kecamatan.isNullOrBlank()) eq("kecamatan", kecamatan)
                }
                order("created_at", Order.DESCENDING)
                limit(20)
            }
            .decodeList<AlertRow>()
    }.onFailure {
        Log.i(TAG, "gagal membaca alert: ${it.message}")
    }.getOrDefault(emptyList())

    private companion object {
        const val TAG = "AlirinAlerts"
    }
}
