package com.taly.toss.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/** Phase-1 capability entry points. Native TOSS implementations replace these fallbacks in Phase 2. */
class FeatureLauncher(private val context: Context) {
    private fun start(intent: Intent): Result<Unit> = runCatching {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        require(intent.resolveActivity(context.packageManager) != null) { "nenhum app compatível encontrado" }
        context.startActivity(intent)
    }

    private fun document(type: String) = start(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        this.type = type
    })

    fun cinema() = document("video/*")
    fun files() = document("*/*")
    fun music() = document("audio/*")
    fun cast() = start(Intent(Settings.ACTION_CAST_SETTINGS))
    fun browser() = start(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")))
}
