package com.taly.toss.home

import android.content.Context
import android.graphics.*
import android.view.KeyEvent
import android.view.View
import kotlin.math.min

/** Lightweight projector-first quick controls overlay.
 * Hardware actions are deliberately represented as local UI state until
 * ProjectorControl/SystemControl backends are connected after device survey.
 */
class QuickPanelView(context: Context) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val t = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("sans", Typeface.NORMAL) }
    private val b = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("sans", Typeface.BOLD) }
    private val purple = Color.rgb(151, 91, 255)
    private val bright = Color.rgb(190, 148, 255)
    private val white = Color.rgb(248, 246, 252)
    private val muted = Color.rgb(178, 172, 190)
    private var selected = 0
    private var brightness = 72
    private var volume = 48
    private var mode = 0
    private val modes = listOf("Cinema", "Jogo", "Eco", "Padrão")

    init { isFocusable = true; isFocusableInTouchMode = true; requestFocus() }

    override fun onDraw(c: Canvas) {
        val s = min(width / 1280f, height / 720f)
        c.save(); c.translate((width-1280*s)/2, (height-720*s)/2); c.scale(s,s)
        p.color = Color.argb(155,0,0,0); c.drawRect(0f,0f,1280f,720f,p)
        p.color = Color.argb(247,17,13,25); c.drawRoundRect(RectF(704f,28f,1248f,692f),30f,30f,p)
        p.style=Paint.Style.STROKE; p.strokeWidth=1.5f; p.color=Color.argb(75,190,148,255); c.drawRoundRect(RectF(704f,28f,1248f,692f),30f,30f,p); p.style=Paint.Style.FILL
        b.color=white; b.textSize=30f; c.drawText("Controles rápidos",744f,82f,b)
        t.color=muted; t.textSize=16f; c.drawText("Ajustes do projetor sem sair do conteúdo",744f,111f,t)

        slider(c,0,"Brilho",brightness,744f,145f)
        slider(c,1,"Volume",volume,744f,233f)

        b.color=white; b.textSize=18f; c.drawText("Modo de imagem",744f,334f,b)
        modes.forEachIndexed { i, label ->
            val left=744f+i*116f; val r=RectF(left,354f,left+104f,400f)
            val active=mode==i; val focused=selected==2+i
            p.color=when { focused -> Color.rgb(79,43,128); active -> Color.rgb(54,34,82); else -> Color.rgb(31,26,40) }
            c.drawRoundRect(r,16f,16f,p)
            if(focused||active){p.style=Paint.Style.STROKE;p.strokeWidth=if(focused)2.5f else 1.5f;p.color=if(focused)bright else purple;c.drawRoundRect(r,16f,16f,p);p.style=Paint.Style.FILL}
            t.textAlign=Paint.Align.CENTER;t.color=white;t.textSize=15f;c.drawText(label,r.centerX(),r.centerY()+5f,t);t.textAlign=Paint.Align.LEFT
        }

        b.color=white;b.textSize=18f;c.drawText("Projeção",744f,451f,b)
        action(c,6,"Foco",744f,470f,220f); action(c,7,"Keystone",976f,470f,220f)
        action(c,8,"HDMI",744f,534f,146f); action(c,9,"Wi-Fi",902f,534f,146f); action(c,10,"Bluetooth",1060f,534f,146f)
        t.color=muted;t.textSize=14f;c.drawText("BACK  Fechar",744f,657f,t)
        c.restore()
    }

    private fun slider(c:Canvas,id:Int,label:String,value:Int,x:Float,y:Float){
        val focus=selected==id
        b.color=if(focus)bright else white;b.textSize=18f;c.drawText(label,x,y,b)
        t.color=muted;t.textSize=16f;t.textAlign=Paint.Align.RIGHT;c.drawText("$value%",1206f,y,t);t.textAlign=Paint.Align.LEFT
        p.color=Color.rgb(52,47,61);c.drawRoundRect(RectF(x,y+24f,1206f,y+32f),4f,4f,p)
        val end=x+(1206f-x)*value/100f;p.color=purple;c.drawRoundRect(RectF(x,y+24f,end,y+32f),4f,4f,p)
        p.color=if(focus)bright else purple;c.drawCircle(end,y+28f,if(focus)9f else 7f,p)
    }

    private fun action(c:Canvas,id:Int,label:String,x:Float,y:Float,w:Float){
        val f=selected==id; val r=RectF(x,y,x+w,y+50f);p.color=if(f)Color.rgb(76,43,119) else Color.rgb(31,26,40);c.drawRoundRect(r,16f,16f,p)
        if(f){p.style=Paint.Style.STROKE;p.strokeWidth=2.5f;p.color=bright;c.drawRoundRect(r,16f,16f,p);p.style=Paint.Style.FILL}
        t.color=white;t.textSize=16f;t.textAlign=Paint.Align.CENTER;c.drawText(label,r.centerX(),r.centerY()+5f,t);t.textAlign=Paint.Align.LEFT
    }

    override fun onKeyDown(k:Int,e:KeyEvent?):Boolean{
        when(k){
            KeyEvent.KEYCODE_BACK -> { visibility=GONE; return true }
            KeyEvent.KEYCODE_DPAD_UP -> selected=when(selected){1->0;2,3,4,5->1;6,7->2;8,9,10->6;else->selected}
            KeyEvent.KEYCODE_DPAD_DOWN -> selected=when(selected){0->1;1->2;2,3,4,5->6;6,7->8;else->selected}
            KeyEvent.KEYCODE_DPAD_LEFT -> adjust(-1)
            KeyEvent.KEYCODE_DPAD_RIGHT -> adjust(1)
            KeyEvent.KEYCODE_DPAD_CENTER,KeyEvent.KEYCODE_ENTER -> if(selected in 2..5) mode=selected-2
            else -> return super.onKeyDown(k,e)
        };invalidate();return true
    }
    private fun adjust(d:Int){ when(selected){0->brightness=(brightness+d*5).coerceIn(0,100);1->volume=(volume+d*5).coerceIn(0,100);in 2..5->selected=(selected+d).coerceIn(2,5);6->if(d>0)selected=7;7->if(d<0)selected=6;8,9,10->selected=(selected+d).coerceIn(8,10)} }
}
