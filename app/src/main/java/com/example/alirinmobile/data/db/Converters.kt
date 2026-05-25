package com.example.alirinmobile.data.db

import androidx.room.TypeConverter
import com.example.alirinmobile.data.HistoryEntry
import com.example.alirinmobile.data.Photo
import com.example.alirinmobile.data.PhotoKind
import com.example.alirinmobile.data.ReportStatus

/**
 * Minimal text-encoded type converters. We avoid kotlinx.serialization to keep the
 * dependency surface small. Field separators are characters we don't allow in inputs.
 */
class Converters {
    // ── HistoryEntry list ────────────────────────────────────────
    @TypeConverter
    fun fromHistoryList(list: List<HistoryEntry>): String =
        list.joinToString("\n") { e ->
            // status|when|live|note
            val n = (e.note ?: "").replace("\n", " ").replace("|", "/")
            "${e.status.name}|${e.when_.replace("|","/")}|${e.live}|$n"
        }

    @TypeConverter
    fun toHistoryList(text: String): List<HistoryEntry> {
        if (text.isBlank()) return emptyList()
        return text.split("\n").mapNotNull { line ->
            val parts = line.split("|", limit = 4)
            if (parts.size < 4) return@mapNotNull null
            val status = runCatching { ReportStatus.valueOf(parts[0]) }.getOrNull() ?: return@mapNotNull null
            HistoryEntry(
                status = status,
                when_ = parts[1],
                live = parts[2].toBooleanStrictOrNull() ?: false,
                note = parts[3].takeIf { it.isNotEmpty() },
            )
        }
    }

    // ── Photo list ───────────────────────────────────────────────
    @TypeConverter
    fun fromPhotoList(list: List<Photo>): String =
        list.joinToString("\n") { "${it.id}|${it.kind.name}|${it.ts}" }

    @TypeConverter
    fun toPhotoList(text: String): List<Photo> {
        if (text.isBlank()) return emptyList()
        return text.split("\n").mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size < 3) return@mapNotNull null
            val id = parts[0].toLongOrNull() ?: return@mapNotNull null
            val kind = runCatching { PhotoKind.valueOf(parts[1]) }.getOrNull() ?: return@mapNotNull null
            val ts = parts[2].toLongOrNull() ?: return@mapNotNull null
            Photo(id, kind, ts)
        }
    }
}
