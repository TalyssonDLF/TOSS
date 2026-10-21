package com.taly.toss.home

import android.content.Context
import android.graphics.*
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import kotlin.math.min

/** Lightweight TV-first TOSS Home. Hardware actions are delegated to MainActivity. */
class TossHomeView(
    context: Context,
    private val onProjector: () -> Unit = {},
    private val onHdmi: () -> Result<Unit> = { Result.failure(UnsupportedOperationException("HDMI unavailable")) }
) : View(context) {
    private val bg = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("sans", Typeface.NORMAL) }
    private val bold = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("sans", Typeface.BOLD) }
    private val purple = Color.rgb(151, 91, 255)
    private val purpleBright = Color.rgb(184, 137, 255)
    private val white = Color.rgb(246, 244, 251)
    private val muted = Color.rgb(177, 171, 191)
    private val surface = Color.argb(205, 18, 15, 27)
    private val surfaceSoft = Color.argb(175, 27, 22, 39)
    private val shortcuts = listOf("Cinema", "YouTube", "HDMI", "Espelhar", "Arquivos", "Apps", "Música", "Navegador")
    private val recommendations = listOf("Filmes", "Séries", "Sua biblioteca", "Recentes")
    // 0 hero, 1..8 shortcuts, 9..12 recommendations, 13 projector, 14 settings
    private var selected = 1

    init { isFocusable = true; isFocusableInTouchMode = true; requestFocus() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val scale = min(width / 1280f, height / 720f)
        val dx = (width - 1280f * scale) / 2f
        val dy = (height - 720f * scale) / 2f
        canvas.save(); canvas.translate(dx, dy); canvas.scale(scale, scale)
        drawBackground(canvas); drawStatus(canvas); drawHero(canvas); drawShortcuts(canvas); drawRecommendations(canvas); drawBottomActions(canvas)
        canvas.restore()
    }

    private fun drawBackground(c: Canvas) {
        bg.shader = LinearGradient(0f, 0f, 1280f, 720f, intArrayOf(Color.rgb(22,11,37), Color.rgb(10,8,16), Color.rgb(6,6,10)), floatArrayOf(0f,.48f,1f), Shader.TileMode.CLAMP)
        c.drawRect(0f,0f,1280f,720f,bg); bg.shader = null
        paint.shader = RadialGradient(905f,92f,430f,intArrayOf(Color.argb(100,119,45,219),Color.argb(30,85,35,155),Color.TRANSPARENT),null,Shader.TileMode.CLAMP)
        c.drawCircle(905f,92f,430f,paint); paint.shader = null
        paint.shader = LinearGradient(0f,250f,0f,620f,Color.TRANSPARENT,Color.argb(235,7,6,11),Shader.TileMode.CLAMP)
        c.drawRect(0f,250f,1280f,620f,paint); paint.shader = null
    }

    private fun drawStatus(c: Canvas) { text.color=muted; text.textSize=18f; text.textAlign=Paint.Align.RIGHT; c.drawText("Wi-Fi   •   TOSS 0.1",1220f,48f,text) }

    private fun drawHero(c: Canvas) {
        val r=RectF(64f,70f,1216f,292f); drawCard(c,r,selected==0,true)
        text.textAlign=Paint.Align.LEFT; text.color=purpleBright; text.textSize=17f; text.typeface=Typeface.create("sans",Typeface.BOLD); c.drawText("CONTINUAR ASSISTINDO",98f,112f,text)
        bold.color=white; bold.textSize=38f; bold.textAlign=Paint.Align.LEFT; c.drawText("Continue de onde parou",98f,164f,bold)
        text.color=muted; text.textSize=20f; text.typeface=Typeface.DEFAULT; c.drawText("Seu conteúdo recente aparecerá aqui",98f,200f,text)
        paint.color=Color.argb(100,255,255,255); c.drawRoundRect(RectF(98f,242f,520f,248f),3f,3f,paint)
        paint.color=purple; c.drawRoundRect(RectF(98f,242f,286f,248f),3f,3f,paint)
        paint.color=Color.argb(185,151,91,255); c.drawCircle(1110f,181f,38f,paint); paint.color=white
        c.drawPath(Path().apply{moveTo(1101f,163f);lineTo(1101f,199f);lineTo(1128f,181f);close()},paint)
    }

    private fun drawShortcuts(c: Canvas) {
        text.textAlign=Paint.Align.LEFT; bold.color=white; bold.textSize=21f; c.drawText("Acesso rápido",64f,335f,bold)
        val gap=12f; val cardW=(1152f-gap*7)/8f
        shortcuts.forEachIndexed { i,label ->
            val left=64f+i*(cardW+gap); val r=RectF(left,354f,left+cardW,444f); drawCard(c,r,selected==i+1)
            drawShortcutGlyph(c,r.centerX(),383f,i,selected==i+1)
            text.textAlign=Paint.Align.CENTER; text.textSize=16f; text.color=if(selected==i+1) white else Color.rgb(221,217,229); c.drawText(label,r.centerX(),425f,text)
        }
    }

    private fun drawShortcutGlyph(c: Canvas,x:Float,y:Float,index:Int,focused:Boolean) {
        paint.style=Paint.Style.STROKE; paint.strokeWidth=2.4f; paint.strokeCap=Paint.Cap.ROUND; paint.color=if(focused) purpleBright else Color.rgb(193,183,211)
        when(index) {
            0 -> { c.drawRoundRect(RectF(x-15,y-11,x+15,y+11),5f,5f,paint); c.drawPath(Path().apply{moveTo(x-3,y-6);lineTo(x-3,y+6);lineTo(x+7,y);close()},paint) }
            1 -> { c.drawRoundRect(RectF(x-16,y-11,x+16,y+11),6f,6f,paint); c.drawLine(x-3,y-6,x+7,y,paint); c.drawLine(x+7,y,x-3,y+6,paint) }
            2 -> { c.drawRect(x-15,y-10,x+15,y+10,paint); c.drawLine(x-8,y+16,x+8,y+16,paint); c.drawLine(x,y+10,x,y+16,paint) }
            3 -> { c.drawArc(RectF(x-15,y-15,x+15,y+15),215f,110f,false,paint); c.drawArc(RectF(x-9,y-9,x+9,y+9),215f,110f,false,paint); c.drawCircle(x-6,y+7,2f,paint) }
            4 -> { c.drawRect(x-15,y-8,x+15,y+11,paint); c.drawLine(x-13,y-8,x-5,y-15,paint); c.drawLine(x-5,y-15,x+3,y-15,paint) }
            5 -> for(xx in listOf(-9f,9f)) for(yy in listOf(-9f,9f)) c.drawRoundRect(RectF(x+xx-5,y+yy-5,x+xx+5,y+yy+5),2f,2f,paint)
            6 -> { c.drawCircle(x-7,y+8,6f,paint); c.drawCircle(x+10,y+4,6f,paint); c.drawLine(x-1,y+8,x-1,y-12,paint); c.drawLine(x-1,y-12,x+16,y-16,paint); c.drawLine(x+16,y-16,x+16,y+4,paint) }
            else -> { c.drawCircle(x,y,14f,paint); c.drawLine(x-14,y,x+14,y,paint); c.drawArc(RectF(x-7,y-14,x+7,y+14),90f,180f,false,paint); c.drawArc(RectF(x-7,y-14,x+7,y+14),270f,180f,false,paint) }
        }; paint.style=Paint.Style.FILL
    }

    private fun drawRecommendations(c: Canvas) {
        bold.color=white; bold.textSize=21f; bold.textAlign=Paint.Align.LEFT; c.drawText("Para você",64f,492f,bold)
        recommendations.forEachIndexed { i,label ->
            val left=64f+i*288f; val r=RectF(left,511f,left+270f,624f); drawCard(c,r,selected==i+9)
            paint.shader=LinearGradient(r.left,r.top,r.right,r.bottom,Color.argb(80,151,91,255),Color.argb(5,151,91,255),Shader.TileMode.CLAMP); c.drawRoundRect(r,18f,18f,paint); paint.shader=null
            text.color=white; text.textSize=18f; text.textAlign=Paint.Align.LEFT; c.drawText(label,r.left+18f,r.bottom-20f,text)
        }
    }

    private fun drawBottomActions(c: Canvas) { drawPill(c,RectF(64f,654f,207f,700f),"Projetor",selected==13); drawPill(c,RectF(1052f,654f,1216f,700f),"Ajustes",selected==14) }
    private fun drawPill(c:Canvas,r:RectF,label:String,focused:Boolean) {
        paint.color=if(focused) Color.argb(235,91,46,160) else Color.argb(150,25,21,34); c.drawRoundRect(r,23f,23f,paint)
        if(focused){paint.style=Paint.Style.STROKE;paint.strokeWidth=2f;paint.color=purpleBright;c.drawRoundRect(r,23f,23f,paint);paint.style=Paint.Style.FILL}
        text.textAlign=Paint.Align.CENTER;text.color=white;text.textSize=17f;c.drawText(label,r.centerX(),r.centerY()+6f,text)
    }
    private fun drawCard(c:Canvas,r:RectF,focused:Boolean,hero:Boolean=false) {
        if(focused){paint.setShadowLayer(if(hero)30f else 22f,0f,0f,Color.argb(180,139,73,255));setLayerType(LAYER_TYPE_SOFTWARE,paint)} else paint.clearShadowLayer()
        paint.color=if(focused) Color.argb(225,36,25,54) else if(hero) surface else surfaceSoft; c.drawRoundRect(r,if(hero)28f else 18f,if(hero)28f else 18f,paint); paint.clearShadowLayer()
        paint.style=Paint.Style.STROKE;paint.strokeWidth=if(focused)2.5f else 1f;paint.color=if(focused)purpleBright else Color.argb(32,255,255,255);c.drawRoundRect(r,if(hero)28f else 18f,if(hero)28f else 18f,paint);paint.style=Paint.Style.FILL
    }

    override fun onKeyDown(keyCode:Int,event:KeyEvent?):Boolean {
        val old=selected
        selected=when(keyCode){
            KeyEvent.KEYCODE_DPAD_UP->moveUp(selected); KeyEvent.KEYCODE_DPAD_DOWN->moveDown(selected); KeyEvent.KEYCODE_DPAD_LEFT->moveLeft(selected); KeyEvent.KEYCODE_DPAD_RIGHT->moveRight(selected)
            KeyEvent.KEYCODE_DPAD_CENTER,KeyEvent.KEYCODE_ENTER->{activate(selected);return true}
            else->return super.onKeyDown(keyCode,event)
        }
        if(old!=selected) invalidate(); return true
    }
    private fun moveUp(i:Int)=when(i){in 1..8->0;in 9..12->((i-9)*2+1).coerceAtMost(8);13,14->9;else->i}
    private fun moveDown(i:Int)=when(i){0->1;in 1..8->9+((i-1)/2).coerceAtMost(3);in 9..12->if(i<=10)13 else 14;else->i}
    private fun moveLeft(i:Int)=when(i){in 2..8->i-1;in 10..12->i-1;14->13;else->i}
    private fun moveRight(i:Int)=when(i){in 1..7->i+1;in 9..11->i+1;13->14;else->i}

    private fun activate(i:Int) {
        when(i) {
            3 -> onHdmi().onFailure { error -> Toast.makeText(context,"HDMI: ${error.message ?: "falha ao abrir"}",Toast.LENGTH_LONG).show() }
            13 -> onProjector()
            else -> {
                val label=when(i){0->"Continuar assistindo";in 1..8->shortcuts[i-1];in 9..12->recommendations[i-9];14->"Ajustes";else->"TOSS"}
                Toast.makeText(context,"$label — integração em desenvolvimento",Toast.LENGTH_SHORT).show()
            }
        }
    }
}
