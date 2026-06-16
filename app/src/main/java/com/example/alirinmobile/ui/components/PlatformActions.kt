package com.example.alirinmobile.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast

/** Fire the Android share sheet with plain text. */
fun Context.shareText(text: String, chooserTitle: String = "Bagikan") {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, chooserTitle))
}

/** Short toast helper — used for stub actions so nothing is a dead button. */
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
