package com.example.alirinmobile.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast

fun Context.shareText(text: String, chooserTitle: String = "Bagikan") {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, chooserTitle))
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
