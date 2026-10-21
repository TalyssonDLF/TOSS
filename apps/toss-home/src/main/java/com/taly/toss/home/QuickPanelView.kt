package com.taly.toss.home

import android.content.Context
import android.graphics.*
import android.view.KeyEvent
import android.view.View
import kotlin.math.min

/** Lightweight side menu. Every item is a real action; no fake local hardware state. */
class QuickPanelView(
    context: Context,
    private val onClose: () -> Unit,
    private val onProjector: () -> Unit,
    private val android: AndroidSettingsLauncher
) : View(context) {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val t=Paint(Paint.ANTI_ALIAS_FLAG).apply{typeface=Typeface.create("sans",Typeface.NORMAL)}
    private val b=Paint(Paint.ANTI_ALIAS_FLAG).apply{typeface=Typeface.create("sans",Typeface.BOLD)}
    private val bright=Color.rgb(190,148,255); private val white=Color.rgb(248,246,252); private val muted=Color.rgb(178,172,190)
    private val items=listOf("Projetor","Wi-Fi","Bluetooth","Som","Tela","Aplicativos","Armazenamento","Todos os ajustes")
    private val descriptions=listOf("Foco, keystone, fonte e imagem","Redes do Android","Áudio e acessórios","Saída e volume","Configurações de exibição","Apps e permissões","Memória interna e USB","Abrir configurações do Android")
    private var selected=0

    init{isFocusable=true;isFocusableInTouchMode=true}

    override fun onDraw(c:Canvas){
        val s=min(width/1280f,height/720f);c.save();c.translate((width-1280*s)/2,(height-720*s)/2);c.scale(s,s)
        p.color=Color.argb(155,0,0,0);c.drawRect(0f,0f,1280f,720f,p)
        p.color=Color.argb(248,17,13,25);c.drawRoundRect(RectF(720f,20f,1260f,700f),30f,30f,p)
        p.style=Paint.Style.STROKE;p.strokeWidth=1.5f;p.color=Color.argb(75,190,148,255);c.drawRoundRect(RectF(720f,20f,1260f,700f),30f,30f,p);p.style=Paint.Style.FILL
        b.color=white;b.textSize=30f;c.drawText("TOSS",758f,67f,b);t.color=muted;t.textSize=15f;c.drawText("Controles rápidos",758f,94f,t)
        items.forEachIndexed{i,label->
            val y=120f+i*65f;val r=RectF(750f,y,1230f,y+54f);val f=i==selected
            p.color=if(f)Color.rgb(70,40,108) else Color.rgb(28,23,37);c.drawRoundRect(r,16f,16f,p)
            if(f){p.style=Paint.Style.STROKE;p.strokeWidth=2.2f;p.color=bright;c.drawRoundRect(r,16f,16f,p);p.style=Paint.Style.FILL}
            b.color=if(f)bright else white;b.textSize=16f;c.drawText(label,772f,y+23f,b)
            t.color=muted;t.textSize=12.5f;c.drawText(descriptions[i],772f,y+42f,t)
            t.textAlign=Paint.Align.RIGHT;t.color=if(f)bright else muted;t.textSize=18f;c.drawText("›",1206f,y+34f,t);t.textAlign=Paint.Align.LEFT
        }
        t.color=muted;t.textSize=14f;c.drawText("BACK  Fechar",758f,670f,t);c.restore()
    }

    override fun onKeyDown(k:Int,e:KeyEvent?):Boolean{
        when(k){
            KeyEvent.KEYCODE_BACK->{onClose();return true}
            KeyEvent.KEYCODE_DPAD_UP->selected=(selected-1).coerceAtLeast(0)
            KeyEvent.KEYCODE_DPAD_DOWN->selected=(selected+1).coerceAtMost(items.lastIndex)
            KeyEvent.KEYCODE_DPAD_CENTER,KeyEvent.KEYCODE_ENTER->activate()
            else->return super.onKeyDown(k,e)
        }
        invalidate();return true
    }

    private fun activate(){
        when(selected){
            0->onProjector()
            1->android.wifi()
            2->android.bluetooth()
            3->android.sound()
            4->android.display()
            5->android.applications()
            6->android.storage()
            7->android.all()
        }
    }
}
