package com.taly.toss.home

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import kotlin.math.min

/**
 * Lightweight TV-first TOSS home.
 * Motion is event-driven only: there are no permanent loops, blur passes or heavy effects.
 */
class TossHomeView(
    context: Context,
    private val youtubeIcon: Drawable? = null,
    private val onYouTube: () -> Result<Unit>,
    private val onHdmi: () -> Result<Unit>,
    private val onCinema: () -> Result<Unit>,
    private val onCast: () -> Result<Unit>,
    private val onFiles: () -> Result<Unit>,
    private val onApps: () -> Unit,
    private val onMusic: () -> Result<Unit>,
    private val onBrowser: () -> Result<Unit>,
    private val onProjector: () -> Unit,
    private val onSettings: () -> Unit
) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("sans", Typeface.NORMAL) }
    private val medium = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("sans", Typeface.BOLD) }

    private val bg = Color.rgb(7, 7, 12)
    private val surface = Color.rgb(20, 18, 29)
    private val surfaceHigh = Color.rgb(31, 25, 44)
    private val purple = Color.rgb(132, 72, 255)
    private val purpleBright = Color.rgb(188, 145, 255)
    private val white = Color.rgb(247, 246, 250)
    private val muted = Color.rgb(164, 159, 177)

    private val shortcuts = listOf("Cinema", "YouTube", "HDMI", "Espelhar", "Arquivos", "Apps", "Música", "Navegador")
    private val recommendations = listOf("Filmes", "Séries", "Sua biblioteca", "Recentes")

    private var selected = 1
    private var focusProgress = 1f
    private var enterProgress = 0f
    private var focusAnimator: ValueAnimator? = null
    private var enterAnimator: ValueAnimator? = null

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        enterAnimator?.cancel()
        enterAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 380L
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

    override fun onDraw(canvas: Canvas) {
        val scale = min(width / 1280f, height / 720f)
        canvas.save()
        canvas.translate((width - 1280f * scale) / 2f, (height - 720f * scale) / 2f)
        canvas.scale(scale, scale)

        drawBackground(canvas)
        canvas.save()
        canvas.translate(0f, 12f * (1f - enterProgress))
        drawTopBar(canvas)
        drawHero(canvas)
        drawShortcuts(canvas)
        drawRecommendations(canvas)
        drawBottomActions(canvas)
        canvas.restore()
        canvas.restore()
    }

    private fun drawBackground(c: Canvas) {
        c.drawColor(bg)
        paint.shader = LinearGradient(
            0f, 0f, 1280f, 720f,
            intArrayOf(Color.rgb(24, 11, 43), Color.rgb(9, 8, 15), bg),
            floatArrayOf(0f, .47f, 1f), Shader.TileMode.CLAMP
        )
        c.drawRect(0f, 0f, 1280f, 720f, paint)
        paint.shader = null

        // Cheap static ambient lighting instead of runtime blur.
        paint.shader = RadialGradient(1040f, 110f, 420f, Color.argb(70, 116, 55, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP)
        c.drawCircle(1040f, 110f, 420f, paint)
        paint.shader = null
        paint.color = Color.argb(18, 255, 255, 255)
        c.drawRect(0f, 0f, 1280f, 1f, paint)
    }

    private fun drawTopBar(c: Canvas) {
        medium.color = white
        medium.textSize = 18f
        medium.textAlign = Paint.Align.LEFT
        c.drawText("Início", 64f, 48f, medium)

        text.color = muted
        text.textSize = 15f
        c.drawText("Cinema", 128f, 48f, text)
        c.drawText("Biblioteca", 198f, 48f, text)
        c.drawText("Apps", 292f, 48f, text)

        val status = RectF(1030f, 24f, 1216f, 58f)
        paint.color = Color.argb(115, 27, 23, 37)
        c.drawRoundRect(status, 17f, 17f, paint)
        text.textAlign = Paint.Align.CENTER
        text.textSize = 14f
        text.color = Color.rgb(205, 199, 218)
        c.drawText("Wi-Fi   •   TOSS 0.1", status.centerX(), 46f, text)
    }

    private fun drawHero(c: Canvas) {
        val r = RectF(64f, 78f, 1216f, 306f)
        drawCard(c, r, selected == 0, 28f, true)

        c.save()
        c.clipRoundRectCompat(r, 28f)
        paint.shader = LinearGradient(
            r.left, r.top, r.right, r.bottom,
            intArrayOf(Color.rgb(28, 18, 47), Color.rgb(24, 15, 43), Color.rgb(12, 11, 18)),
            floatArrayOf(0f, .58f, 1f), Shader.TileMode.CLAMP
        )
        c.drawRect(r, paint)
        paint.shader = null

        // Abstract cinematic artwork, deliberately asset-free and inexpensive.
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        for (i in 0..4) {
            paint.color = Color.argb(38 - i * 5, 190, 145, 255)
            val radius = 56f + i * 30f
            c.drawCircle(1082f, 188f, radius, paint)
        }
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(1082f, 188f, 165f, Color.argb(105, 132, 72, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP)
        c.drawCircle(1082f, 188f, 165f, paint)
        paint.shader = null
        c.restore()

        if (selected == 0) drawFocusRing(c, r, 28f)

        text.textAlign = Paint.Align.LEFT
        text.color = purpleBright
        text.textSize = 14f
        medium.letterSpacing = .08f
        c.drawText("CONTINUAR ASSISTINDO", 100f, 120f, text)
        medium.letterSpacing = 0f

        medium.color = white
        medium.textSize = 42f
        c.drawText("Cinema TOSS", 98f, 174f, medium)
        text.color = Color.rgb(190, 184, 202)
        text.textSize = 18f
        c.drawText("Seus vídeos, sua biblioteca e reprodução em um só lugar.", 100f, 207f, text)

        val play = RectF(100f, 236f, 254f, 280f)
        paint.color = purple
        c.drawRoundRect(play, 22f, 22f, paint)
        paint.color = white
        c.drawPath(Path().apply {
            moveTo(122f, 249f); lineTo(122f, 267f); lineTo(136f, 258f); close()
        }, paint)
        medium.textSize = 15f
        medium.color = white
        c.drawText("Continuar", 148f, 264f, medium)

        text.color = muted
        text.textSize = 14f
        c.drawText("Biblioteca local", 276f, 263f, text)
    }

    private fun drawShortcuts(c: Canvas) {
        sectionTitle(c, "Acesso rápido", 64f, 346f)
        val gap = 12f
        val cardWidth = (1152f - gap * 7f) / 8f
        shortcuts.forEachIndexed { i, label ->
            val left = 64f + i * (cardWidth + gap)
            val base = RectF(left, 365f, left + cardWidth, 457f)
            val focused = selected == i + 1
            val r = animatedRect(base, focused)
            drawCard(c, r, focused, 18f)

            if (i == 1 && youtubeIcon != null) {
                val iconSize = if (focused) 43 else 40
                youtubeIcon.setBounds(
                    (r.centerX() - iconSize / 2f).toInt(),
                    (r.top + 15f).toInt(),
                    (r.centerX() + iconSize / 2f).toInt(),
                    (r.top + 15f + iconSize).toInt()
                )
                youtubeIcon.draw(c)
            } else {
                drawGlyph(c, r.centerX(), r.top + 35f, i, focused)
            }

            text.textAlign = Paint.Align.CENTER
            text.textSize = 15f
            text.color = if (focused) white else Color.rgb(220, 216, 228)
            c.drawText(label, r.centerX(), r.bottom - 15f, text)
        }
    }

    private fun drawRecommendations(c: Canvas) {
        sectionTitle(c, "Para você", 64f, 503f)
        recommendations.forEachIndexed { i, label ->
            val left = 64f + i * 288f
            val base = RectF(left, 522f, left + 270f, 626f)
            val focused = selected == i + 9
            val r = animatedRect(base, focused)
            drawCard(c, r, focused, 20f)

            paint.shader = LinearGradient(
                r.left, r.top, r.right, r.bottom,
                Color.argb(if (focused) 100 else 58, 132, 72, 255),
                Color.argb(4, 132, 72, 255), Shader.TileMode.CLAMP
            )
            c.drawRoundRect(r, 20f, 20f, paint)
            paint.shader = null

            paint.color = Color.argb(95, 255, 255, 255)
            c.drawRoundRect(RectF(r.left + 18f, r.top + 18f, r.left + 54f, r.top + 22f), 2f, 2f, paint)
            text.textAlign = Paint.Align.LEFT
            text.color = white
            text.textSize = 18f
            c.drawText(label, r.left + 18f, r.bottom - 20f, text)
        }
    }

    private fun drawBottomActions(c: Canvas) {
        drawPill(c, RectF(64f, 656f, 208f, 700f), "Projetor", selected == 13)
        drawPill(c, RectF(1050f, 656f, 1216f, 700f), "Ajustes", selected == 14)
        text.textAlign = Paint.Align.CENTER
        text.textSize = 13f
        text.color = Color.rgb(113, 108, 126)
        c.drawText("Use o controle para navegar", 640f, 684f, text)
    }

    private fun sectionTitle(c: Canvas, label: String, x: Float, y: Float) {
        medium.color = white
        medium.textSize = 19f
        medium.textAlign = Paint.Align.LEFT
        c.drawText(label, x, y, medium)
    }

    private fun animatedRect(base: RectF, focused: Boolean): RectF {
        if (!focused) return base
        val grow = 2f + 3f * focusProgress
        return RectF(base.left - grow, base.top - grow, base.right + grow, base.bottom + grow)
    }

    private fun drawPill(c: Canvas, base: RectF, label: String, focused: Boolean) {
        val r = animatedRect(base, focused)
        paint.color = if (focused) Color.rgb(76, 43, 132) else Color.rgb(22, 19, 30)
        c.drawRoundRect(r, 24f, 24f, paint)
        if (focused) drawFocusRing(c, r, 24f)
        text.textAlign = Paint.Align.CENTER
        text.color = white
        text.textSize = 16f
        c.drawText(label, r.centerX(), r.centerY() + 5f, text)
    }

    private fun drawCard(c: Canvas, r: RectF, focused: Boolean, radius: Float, hero: Boolean = false) {
        paint.color = if (focused) surfaceHigh else if (hero) Color.rgb(17, 15, 24) else surface
        c.drawRoundRect(r, radius, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.argb(if (focused) 80 else 24, 255, 255, 255)
        c.drawRoundRect(r, radius, radius, paint)
        paint.style = Paint.Style.FILL
        if (focused) drawFocusRing(c, r, radius)
    }

    private fun drawFocusRing(c: Canvas, r: RectF, radius: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.4f
        paint.color = Color.argb((170 + 70 * focusProgress).toInt().coerceAtMost(240), 188, 145, 255)
        c.drawRoundRect(r, radius, radius, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawGlyph(c: Canvas, x: Float, y: Float, i: Int, focused: Boolean) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (focused) 2.8f else 2.2f
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = if (focused) purpleBright else Color.rgb(185, 178, 200)
        when (i) {
            0 -> {
                c.drawRoundRect(RectF(x - 15f, y - 11f, x + 15f, y + 11f), 5f, 5f, paint)
                c.drawPath(Path().apply { moveTo(x - 3f, y - 6f); lineTo(x - 3f, y + 6f); lineTo(x + 7f, y); close() }, paint)
            }
            2 -> c.drawRoundRect(RectF(x - 15f, y - 10f, x + 15f, y + 10f), 3f, 3f, paint)
            3 -> {
                c.drawArc(RectF(x - 15f, y - 15f, x + 15f, y + 15f), 215f, 110f, false, paint)
                c.drawArc(RectF(x - 9f, y - 9f, x + 9f, y + 9f), 215f, 110f, false, paint)
            }
            4 -> c.drawRoundRect(RectF(x - 15f, y - 10f, x + 15f, y + 11f), 3f, 3f, paint)
            5 -> for (xx in listOf(-9f, 9f)) for (yy in listOf(-9f, 9f)) c.drawRoundRect(RectF(x + xx - 5f, y + yy - 5f, x + xx + 5f, y + yy + 5f), 2f, 2f, paint)
            6 -> {
                c.drawCircle(x - 7f, y + 8f, 6f, paint); c.drawCircle(x + 10f, y + 4f, 6f, paint)
                c.drawLine(x - 1f, y + 8f, x - 1f, y - 12f, paint); c.drawLine(x - 1f, y - 12f, x + 16f, y - 16f, paint)
            }
            else -> c.drawCircle(x, y, 14f, paint)
        }
        paint.strokeCap = Paint.Cap.BUTT
        paint.style = Paint.Style.FILL
    }

    private fun Canvas.clipRoundRectCompat(r: RectF, radius: Float) {
        clipPath(Path().apply { addRoundRect(r, radius, radius, Path.Direction.CW) })
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val old = selected
        selected = when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> when (selected) {
                in 1..8 -> 0
                in 9..12 -> ((selected - 9) * 2 + 1).coerceAtMost(8)
                13, 14 -> 9
                else -> selected
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> when (selected) {
                0 -> 1
                in 1..8 -> 9 + ((selected - 1) / 2).coerceAtMost(3)
                in 9..12 -> if (selected <= 10) 13 else 14
                else -> selected
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> when (selected) {
                in 2..8 -> selected - 1
                in 10..12 -> selected - 1
                14 -> 13
                else -> selected
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> when (selected) {
                in 1..7 -> selected + 1
                in 9..11 -> selected + 1
                13 -> 14
                else -> selected
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                activate(); return true
            }
            else -> return super.onKeyDown(keyCode, event)
        }
        if (old != selected) animateFocus()
        return true
    }

    private fun activate() {
        val result: Result<Unit>? = when (selected) {
            0, 1, 9, 10 -> onCinema()
            2 -> onYouTube()
            3 -> onHdmi()
            4 -> onCast()
            5, 11, 12 -> onFiles()
            6 -> { onApps(); null }
            7 -> onMusic()
            8 -> onBrowser()
            13 -> { onProjector(); null }
            14 -> { onSettings(); null }
            else -> null
        }
        result?.onFailure { Toast.makeText(context, it.message ?: "Ação indisponível", Toast.LENGTH_SHORT).show() }
    }
}
