package com.example.alirinmobile.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.alirinmobile.data.Photo
import com.example.alirinmobile.feature.lapor.LaporForm

/** Single-row table holding the current in-progress Lapor draft. */
@Entity(tableName = "drafts")
data class DraftEntity(
    @PrimaryKey val id: Int = 0,        // we keep only one draft
    val kategori: String = "",
    val severity: String = "",
    val deskripsi: String = "",
    val alamat: String = "",
    val photos: List<Photo> = emptyList(),
    val nama: String = "",
    val kontak: String = "",
    val mode: String? = null,           // ReportMode name (nullable until chosen)
    val updatedAt: Long = System.currentTimeMillis(),
)

fun DraftEntity.toForm() = LaporForm(
    kategori = kategori, severity = severity, deskripsi = deskripsi,
    alamat = alamat, photos = photos, nama = nama, kontak = kontak,
)
