package com.taly.toss.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable

/** Lightweight index of launchable apps already installed on the projector. */
class AppCatalog(private val context: Context) {
    data class Entry(
        val label: String,
        val packageName: String,
        val activityName: String,
        val resolveInfo: ResolveInfo
    )

    private val pm: PackageManager = context.packageManager

    fun load(): List<Entry> {
        val intents = listOf(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER),
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        )
        val unique = linkedMapOf<String, Entry>()
        intents.forEach { intent ->
            pm.queryIntentActivities(intent, PackageManager.MATCH_ALL).forEach { info ->
                val activity = info.activityInfo ?: return@forEach
                if (activity.packageName == context.packageName) return@forEach
                val key = "${activity.packageName}/${activity.name}"
                unique.putIfAbsent(
                    key,
                    Entry(
                        label = info.loadLabel(pm)?.toString()?.trim().orEmpty().ifBlank { activity.packageName },
                        packageName = activity.packageName,
                        activityName = activity.name,
                        resolveInfo = info
                    )
                )
            }
        }
        return unique.values.sortedBy { it.label.lowercase() }
    }

    fun icon(entry: Entry): Drawable? = runCatching { entry.resolveInfo.loadIcon(pm) }.getOrNull()

    fun launch(entry: Entry): Result<Unit> = runCatching {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            component = ComponentName(entry.packageName, entry.activityName)
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }
        context.startActivity(intent)
    }

    fun findYouTube(entries: List<Entry> = load()): Entry? {
        val preferred = listOf("com.google.android.youtube.tv", "com.google.android.youtube")
        preferred.forEach { pkg -> entries.firstOrNull { it.packageName == pkg }?.let { return it } }
        return entries.firstOrNull { it.label.contains("youtube", ignoreCase = true) }
    }
}
