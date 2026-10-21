package com.taly.toss.home

import android.content.Context
import android.content.Intent
import android.provider.Settings

/** Opens stock Android settings only when TOSS does not own that control yet. */
class AndroidSettingsLauncher(private val context: Context) {
    fun open(action: String): Result<Unit> = runCatching {
        val intent = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    fun wifi() = open(Settings.ACTION_WIFI_SETTINGS)
    fun bluetooth() = open(Settings.ACTION_BLUETOOTH_SETTINGS)
    fun sound() = open(Settings.ACTION_SOUND_SETTINGS)
    fun display() = open(Settings.ACTION_DISPLAY_SETTINGS)
    fun applications() = open(Settings.ACTION_APPLICATION_SETTINGS)
    fun storage() = open(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
    fun dateTime() = open(Settings.ACTION_DATE_SETTINGS)
    fun language() = open(Settings.ACTION_LOCALE_SETTINGS)
    fun all() = open(Settings.ACTION_SETTINGS)
}
