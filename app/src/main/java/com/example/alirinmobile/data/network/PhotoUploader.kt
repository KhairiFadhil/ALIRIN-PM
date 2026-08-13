package com.example.alirinmobile.data.network

import com.example.alirinmobile.data.PhotoRef
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import java.io.File
import java.util.UUID

// Convention path matches web C:\ALIRIN\app\src\services\storageService.js:25-29
// (report-photos/<timestamp>-<uuid>.<ext>). Bucket "reports" bersifat public,
// jadi hasil publicUrl bisa langsung disimpan di kolom report_photos.url.
class PhotoUploader(private val supabase: SupabaseClient) {

    suspend fun upload(local: File): PhotoRef {
        val bucket = supabase.storage.from(BUCKET)
        val ext = local.extension.ifBlank { "jpg" }.lowercase()
        val path = "report-photos/${System.currentTimeMillis()}-${UUID.randomUUID()}.$ext"
        bucket.upload(path, local.readBytes()) {
            upsert = false
        }
        val url = bucket.publicUrl(path)
        require(url.isNotBlank()) { "Supabase gagal mengembalikan public URL untuk $path" }
        return PhotoRef(
            id = UUID.randomUUID().toString(),
            url = url,
            localUri = local.absolutePath,
            name = local.name,
            type = "image/${if (ext == "jpg") "jpeg" else ext}",
            size = local.length().toInt(),
            kind = "report",
        )
    }

    suspend fun deleteMany(paths: List<String>) {
        if (paths.isEmpty()) return
        runCatching { supabase.storage.from(BUCKET).delete(paths) }
    }

    private companion object {
        const val BUCKET = "reports"
    }
}
