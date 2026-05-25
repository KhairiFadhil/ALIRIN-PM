package com.example.alirinmobile.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Loads + watermarks + saves photos for Lapor submissions. Lives in filesDir/photos/.
 * Real GPS isn't wired yet, so watermark uses a mock Bandar Lampung coordinate the
 * design already references (see screens-lapor.jsx).
 */
object PhotoStore {
    private const val FALLBACK_LAT = -5.39812
    private const val FALLBACK_LNG = 105.26012
    private const val MAX_EDGE_PX = 1280

    private fun ensureDir(ctx: Context): File =
        File(ctx.filesDir, "photos").apply { if (!exists()) mkdirs() }

    /**
     * Saves the given camera bitmap with a lat/long + timestamp watermark.
     * Coords default to a Bandar Lampung centre point when real GPS isn't available.
     */
    fun saveCameraBitmap(
        ctx: Context,
        bitmap: Bitmap,
        lat: Double? = null,
        lng: Double? = null,
    ): String {
        val resized = bitmap.resizeTo(MAX_EDGE_PX)
        val watermarked = resized.copy(Bitmap.Config.ARGB_8888, true)
        drawWatermark(watermarked, lat ?: FALLBACK_LAT, lng ?: FALLBACK_LNG)
        return write(ctx, watermarked)
    }

    /** Loads the gallery uri and stores a resized copy (no watermark; design flags this). */
    fun saveGalleryUri(ctx: Context, uri: Uri): String {
        val src = decode(ctx, uri)
        val resized = src.resizeTo(MAX_EDGE_PX)
        return write(ctx, resized)
    }

    private fun decode(ctx: Context, uri: Uri): Bitmap =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(ctx.contentResolver, uri))
                .copy(Bitmap.Config.ARGB_8888, true)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(ctx.contentResolver, uri)
        }

    private fun Bitmap.resizeTo(maxEdge: Int): Bitmap {
        val long = maxOf(width, height)
        if (long <= maxEdge) return this
        val scale = maxEdge.toFloat() / long
        val w = (width * scale).toInt().coerceAtLeast(1)
        val h = (height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(this, w, h, true)
    }

    private fun write(ctx: Context, bitmap: Bitmap): String {
        val file = File(ensureDir(ctx), "${UUID.randomUUID()}.jpg")
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out) }
        return file.absolutePath
    }

    private fun drawWatermark(bmp: Bitmap, lat: Double, lng: Double) {
        val canvas = Canvas(bmp)
        val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("id")).format(Date())
        val line1 = "%.5f, %.5f".format(lat, lng)
        val line2 = "ALIRIN · $ts"
        val textSize = (bmp.width * 0.028f).coerceIn(18f, 42f)
        val pad = textSize * 0.4f
        val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.textSize = textSize
            typeface = Typeface.DEFAULT_BOLD
            setShadowLayer(2f, 0f, 1f, Color.BLACK)
        }
        val w1 = paintText.measureText(line1)
        val w2 = paintText.measureText(line2)
        val maxW = maxOf(w1, w2)
        val boxRight = bmp.width - pad
        val boxBottom = bmp.height - pad
        val boxLeft = boxRight - maxW - pad * 2
        val boxTop = boxBottom - textSize * 2 - pad * 1.6f
        val paintBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(170, 0, 0, 0) }
        canvas.drawRoundRect(RectF(boxLeft, boxTop, boxRight, boxBottom), pad * 0.6f, pad * 0.6f, paintBg)
        val baseLine1 = boxTop + textSize * 1.05f + pad * 0.2f
        val baseLine2 = baseLine1 + textSize * 1.15f
        canvas.drawText(line1, boxLeft + pad, baseLine1, paintText)
        canvas.drawText(line2, boxLeft + pad, baseLine2, paintText)
    }
}
