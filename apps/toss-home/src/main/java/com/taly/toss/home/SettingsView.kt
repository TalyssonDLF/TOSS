package com.taly.toss.home

import android.content.Context
import android.graphics.*
import android.view.KeyEvent
import android.view.View
import kotlin.math.min

/** TOSS-owned settings hub. Android settings remain a fallback backend while controls migrate into TOSS. */
class SettingsView(
    context: Context,
    private val onClose: () -> Unit,
    private val onProjector: () -> Unit,
    private val android: AndroidSettingsLauncher
) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val t = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("sans", Typeface.NORMAL) }
    private val b = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("sans", Typeface.BOLD) }
    private val white = Color.rgb(247,245,251)
    private val muted = Color.rgb(174,168,187)
    private val purple = Color.rgb(151,91,255)
    private val bright = Color.rgb(193,153,255)
    private val items = listOf(
        "Projetor" to "Imagem, foco, keystone e fonte",
        "Wi-Fi" to "Redes e conexão",
        "Bluetooth" to "Controles, áudio e acessórios",
        "Som" to "Volume e saída de áudio",
        "Tela" to "Exibição do Android",
        "Aplicativos" to "Apps instalados e permissões",
        "Armazenamento" to "Espaço interno e USB",
        "Data e hora" to "Relógio e fuso horário",
        "Idioma" to "Idioma do sistema",
        "Android" to "Todos os ajustes do sistema"
    )
    private var selected = 0

    init { isFocusable = true; isFocusableInTouchMode = true }

    override fun onDraw(c: Canvas) {
        val s=min(width/1280f,height/720f); c.save(); c.translate((width-1280*s)/2,(height-720*s)/2); c.scale(s,s)
        p.shader=LinearGradient(0f,0f,1280f,720f,intArrayOf(Color.rgb(20,10,34),Color.rgb(9,7,14),Color.rgb(5,5,9)),null,Shader.TileMode.CLAMP); c.drawRect(0f,0f,1280f,720f,p); p.shader=null
        b.color=white; b.textSize=34f; c.drawText("Ajustes",64f,70f,b)
        t.color=muted; t.textSize=17f; c.drawText("TOSS primeiro. Android disponível quando necessário.",64f,99f,t)
        t.textAlign=Paint.Align.RIGHT; c.drawText("BACK  Voltar",1216f,70f,t); t.textAlign=Paint.Align.LEFT
        items.forEachIndexed { i, item ->
            val col=i%2; val row=i/2; val x=64f+col*584f; val y=136f+row*103f
            val r=RectF(x,y,x+552f,y+84f); val f=i==selected
            p.color=if(f)Color.rgb(55,34,81) else Color.argb(205,25,21,35); c.drawRoundRect(r,20f,20f,p)
            p.style=Paint.Style.STROKE; p.strokeWidth=if(f)2.5f else 1f; p.color=if(f)bright else Color.argb(35,255,255,255); c.drawRoundRect(r,20f,20f,p); p.style=Paint.Style.FILL
            p.color=if(f)bright else purple; c.drawCircle(x+35f,y+42f,7f,p)
            b.color=if(f)bright else white; b.textSize=18f; c.drawText(item.first,x+58f,y+34f,b)
            t.color=muted; t.textSize=14f; c.drawText(item.second,x+58f,y+58f,t)
        }
        t.color=muted; t.textSize=14f; c.drawText("Os atalhos Android serão substituídos gradualmente por controles nativos TOSS.",64f,684f,t)
        c.restore()
    }

    override fun onKeyDown(k:Int,e:KeyEvent?):Boolean {
        when(k) {
            KeyEvent.KEYCODE_BACK -> { onClose(); return true }
            KeyEvent.KEYCODE_DPAD_UP -> selected=(selected-2).coerceAtLeast(0)
            KeyEvent.KEYCODE_DPAD_DOWN -> selected=(selected+2).coerceAtMost(items.lastIndex)
            KeyEvent.KEYCODE_DPAD_LEFT -> if(selected%2==1) selected--
            KeyEvent.KEYCODE_DPAD_RIGHT -> if(selected%2==0 && selected+1<=items.lastIndex) selected++
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> activate()
            else -> return super.onKeyDown(k,e)
        }
        invalidate(); return true
    }

    private fun activate() {
        when(selected) {
            0 -> onProjector()
            1 -> android.wifi()
            2 -> android.bluetooth()
            3 -> android.sound()
            4 -> android.display()
            5 -> android.applications()
            6 -> android.storage()
            7 -> android.dateTime()
            8 -> android.language()
            9 -> android.all()
        }
    }
}
