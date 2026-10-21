package com.taly.toss.home

import android.content.Context
import android.graphics.*
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import kotlin.math.ceil
import kotlin.math.min

/** TV-first app library backed by the launchable apps already installed on Android. */
class AppsView(
    context: Context,
    private val catalog: AppCatalog,
    private val onClose: () -> Unit
) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("sans", Typeface.NORMAL) }
    private val bold = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("sans", Typeface.BOLD) }
    private val white = Color.rgb(246, 244, 251)
    private val muted = Color.rgb(177, 171, 191)
    private val purple = Color.rgb(151, 91, 255)
    private val bright = Color.rgb(190, 148, 255)
    private val columns = 5
    private val rows = 2
    private val pageSize = columns * rows
    private var apps = catalog.load()
    private var selected = 0

    init { isFocusable = true; isFocusableInTouchMode = true }

    fun refresh() {
        apps = catalog.load()
        selected = selected.coerceIn(0, (apps.size - 1).coerceAtLeast(0))
        invalidate()
    }

    override fun onDraw(c: Canvas) {
        val s = min(width / 1280f, height / 720f)
        c.save(); c.translate((width - 1280f * s) / 2f, (height - 720f * s) / 2f); c.scale(s, s)
        paint.shader = LinearGradient(0f, 0f, 1280f, 720f, intArrayOf(Color.rgb(20,10,34), Color.rgb(9,7,14), Color.rgb(5,5,9)), null, Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, 1280f, 720f, paint); paint.shader = null

        bold.color = white; bold.textSize = 34f; c.drawText("Apps", 64f, 70f, bold)
        text.color = muted; text.textSize = 17f; c.drawText("Aplicativos instalados no projetor", 64f, 100f, text)
        text.textAlign = Paint.Align.RIGHT; c.drawText("BACK  Voltar", 1216f, 70f, text); text.textAlign = Paint.Align.LEFT

        if (apps.isEmpty()) {
            text.color = muted; text.textSize = 22f; c.drawText("Nenhum aplicativo disponível.", 64f, 180f, text)
            c.restore(); return
        }

        val page = selected / pageSize
        val start = page * pageSize
        val end = min(start + pageSize, apps.size)
        for (index in start until end) {
            val local = index - start
            val row = local / columns
            val col = local % columns
            val left = 64f + col * 230f
            val top = 145f + row * 235f
            val r = RectF(left, top, left + 210f, top + 205f)
            card(c, r, index == selected)
            val entry = apps[index]
            catalog.icon(entry)?.let { icon ->
                val size = 82
                val cx = r.centerX().toInt()
                icon.setBounds(cx - size/2, (top + 28f).toInt(), cx + size/2, (top + 28f).toInt() + size)
                icon.draw(c)
            }
            text.textAlign = Paint.Align.CENTER; text.color = white; text.textSize = 18f
            c.drawText(ellipsize(entry.label, 19), r.centerX(), top + 145f, text)
            text.color = muted; text.textSize = 12f
            c.drawText(ellipsize(entry.packageName, 27), r.centerX(), top + 171f, text)
            text.textAlign = Paint.Align.LEFT
        }

        val pages = ceil(apps.size / pageSize.toDouble()).toInt().coerceAtLeast(1)
        text.color = muted; text.textSize = 15f; text.textAlign = Paint.Align.RIGHT
        c.drawText("${apps.size} apps   •   ${page + 1}/$pages", 1216f, 676f, text); text.textAlign = Paint.Align.LEFT
        c.restore()
    }

    private fun card(c: Canvas, r: RectF, focused: Boolean) {
        paint.color = if (focused) Color.rgb(52, 31, 77) else Color.rgb(25, 21, 34)
        c.drawRoundRect(r, 24f, 24f, paint)
        paint.style = Paint.Style.STROKE; paint.strokeWidth = if (focused) 3f else 1f
        paint.color = if (focused) bright else Color.argb(35,255,255,255)
        c.drawRoundRect(r, 24f, 24f, paint); paint.style = Paint.Style.FILL
    }

    private fun ellipsize(value: String, max: Int): String = if (value.length <= max) value else value.take(max - 1) + "…"

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) { onClose(); return true }
        if (apps.isEmpty()) return super.onKeyDown(keyCode, event)
        val old = selected
        selected = when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> (selected - 1).coerceAtLeast(0)
            KeyEvent.KEYCODE_DPAD_RIGHT -> (selected + 1).coerceAtMost(apps.lastIndex)
            KeyEvent.KEYCODE_DPAD_UP -> (selected - columns).coerceAtLeast(0)
            KeyEvent.KEYCODE_DPAD_DOWN -> (selected + columns).coerceAtMost(apps.lastIndex)
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                catalog.launch(apps[selected]).onFailure { Toast.makeText(context, "Não foi possível abrir ${apps[selected].label}: ${it.message}", Toast.LENGTH_LONG).show() }
                return true
            }
            else -> return super.onKeyDown(keyCode, event)
        }
        if (old != selected) invalidate()
        return true
    }
}
