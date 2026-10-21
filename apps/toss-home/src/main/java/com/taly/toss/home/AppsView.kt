package com.taly.toss.home

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
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
    private val white = Color.rgb(247, 246, 250)
    private val muted = Color.rgb(164, 159, 177)
    private val purple = Color.rgb(132, 72, 255)
    private val bright = Color.rgb(188, 145, 255)
    private val columns = 5
    private val rows = 2
    private val pageSize = columns * rows

    private var apps = catalog.load()
    private var selected = 0
    private var focusProgress = 1f
    private var enterProgress = 0f
    private var focusAnimator: ValueAnimator? = null
    private var enterAnimator: ValueAnimator? = null
    private val iconCache = linkedMapOf<String, Drawable?>()

    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        enterAnimator?.cancel()
        enterAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 320L
            interpolator = DecelerateInterpolator(1.6f)
            addUpdateListener {
                enterProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        focusAnimator?.cancel()
        enterAnimator?.cancel()
        super.onDetachedFromWindow()
    }

    fun refresh() {
        apps = catalog.load()
        selected = selected.coerceIn(0, (apps.size - 1).coerceAtLeast(0))
        iconCache.clear()
        invalidate()
    }

    override fun onDraw(c: Canvas) {
        val scale = min(width / 1280f, height / 720f)
        c.save()
        c.translate((width - 1280f * scale) / 2f, (height - 720f * scale) / 2f)
        c.scale(scale, scale)
        drawBackground(c)

        c.save()
        c.translate(0f, 10f * (1f - enterProgress))
        drawHeader(c)

        if (apps.isEmpty()) {
            text.color = muted
            text.textSize = 22f
            c.drawText("Nenhum aplicativo disponível.", 64f, 180f, text)
            c.restore(); c.restore(); return
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
            val base = RectF(left, top, left + 210f, top + 205f)
            val focused = index == selected
            val r = if (focused) {
                val grow = 2f + 4f * focusProgress
                RectF(base.left - grow, base.top - grow, base.right + grow, base.bottom + grow)
            } else base

            card(c, r, focused)
            val entry = apps[index]
            val key = "${entry.packageName}/${entry.activityName}"
            val icon = if (iconCache.containsKey(key)) iconCache[key] else catalog.icon(entry).also { iconCache[key] = it }
            icon?.let {
                val size = if (focused) 86 else 82
                val cx = r.centerX().toInt()
                val iconTop = (r.top + 28f).toInt()
                it.setBounds(cx - size / 2, iconTop, cx + size / 2, iconTop + size)
                it.draw(c)
            }

            text.textAlign = Paint.Align.CENTER
            text.color = white
            text.textSize = if (focused) 19f else 18f
            c.drawText(ellipsize(entry.label, 19), r.centerX(), r.top + 145f, text)
            text.color = muted
            text.textSize = 12f
            c.drawText(ellipsize(entry.packageName, 27), r.centerX(), r.top + 171f, text)
            text.textAlign = Paint.Align.LEFT
        }

        val pages = ceil(apps.size / pageSize.toDouble()).toInt().coerceAtLeast(1)
        drawPageIndicator(c, page, pages)
        c.restore()
        c.restore()
    }

    private fun drawBackground(c: Canvas) {
        paint.shader = LinearGradient(
            0f, 0f, 1280f, 720f,
            intArrayOf(Color.rgb(22, 10, 39), Color.rgb(9, 8, 15), Color.rgb(7, 7, 12)),
            floatArrayOf(0f, .45f, 1f), Shader.TileMode.CLAMP
        )
        c.drawRect(0f, 0f, 1280f, 720f, paint)
        paint.shader = null
        paint.shader = RadialGradient(1110f, 80f, 390f, Color.argb(55, 116, 55, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP)
        c.drawCircle(1110f, 80f, 390f, paint)
        paint.shader = null
    }

    private fun drawHeader(c: Canvas) {
        bold.color = white
        bold.textSize = 34f
        bold.textAlign = Paint.Align.LEFT
        c.drawText("Apps", 64f, 70f, bold)
        text.color = muted
        text.textSize = 16f
        c.drawText("Tudo que está instalado no projetor", 64f, 100f, text)

        val back = RectF(1074f, 35f, 1216f, 75f)
        paint.color = Color.argb(110, 28, 24, 38)
        c.drawRoundRect(back, 20f, 20f, paint)
        text.textAlign = Paint.Align.CENTER
        text.color = Color.rgb(205, 199, 218)
        text.textSize = 14f
        c.drawText("BACK  Voltar", back.centerX(), 60f, text)
        text.textAlign = Paint.Align.LEFT
    }

    private fun drawPageIndicator(c: Canvas, page: Int, pages: Int) {
        text.color = muted
        text.textSize = 14f
        text.textAlign = Paint.Align.RIGHT
        c.drawText("${apps.size} apps", 1216f, 677f, text)
        text.textAlign = Paint.Align.CENTER
        val totalWidth = pages * 18f + (pages - 1).coerceAtLeast(0) * 8f
        var x = 640f - totalWidth / 2f
        repeat(pages) { i ->
            paint.color = if (i == page) purple else Color.rgb(52, 47, 63)
            val width = if (i == page) 24f else 18f
            c.drawRoundRect(RectF(x, 670f, x + width, 675f), 3f, 3f, paint)
            x += width + 8f
        }
        text.textAlign = Paint.Align.LEFT
    }

    private fun card(c: Canvas, r: RectF, focused: Boolean) {
        paint.color = if (focused) Color.rgb(35, 27, 51) else Color.rgb(20, 18, 29)
        c.drawRoundRect(r, 24f, 24f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (focused) 2.5f else 1f
        paint.color = if (focused) Color.argb((170 + 70 * focusProgress).toInt().coerceAtMost(240), 188, 145, 255) else Color.argb(28, 255, 255, 255)
        c.drawRoundRect(r, 24f, 24f, paint)
        paint.style = Paint.Style.FILL
    }

    private fun animateFocus() {
        focusAnimator?.cancel()
        focusProgress = 0f
        focusAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 165L
            interpolator = DecelerateInterpolator(1.8f)
            addUpdateListener {
                focusProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun ellipsize(value: String, max: Int): String = if (value.length <= max) value else value.take(max - 1) + "…"

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onClose(); return true
        }
        if (apps.isEmpty()) return super.onKeyDown(keyCode, event)
        val old = selected
        selected = when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> (selected - 1).coerceAtLeast(0)
            KeyEvent.KEYCODE_DPAD_RIGHT -> (selected + 1).coerceAtMost(apps.lastIndex)
            KeyEvent.KEYCODE_DPAD_UP -> (selected - columns).coerceAtLeast(0)
            KeyEvent.KEYCODE_DPAD_DOWN -> (selected + columns).coerceAtMost(apps.lastIndex)
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                catalog.launch(apps[selected]).onFailure {
                    Toast.makeText(context, "Não foi possível abrir ${apps[selected].label}: ${it.message}", Toast.LENGTH_LONG).show()
                }
                return true
            }
            else -> return super.onKeyDown(keyCode, event)
        }
        if (old != selected) animateFocus()
        return true
    }
}
