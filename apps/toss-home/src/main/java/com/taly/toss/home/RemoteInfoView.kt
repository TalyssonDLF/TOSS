package com.taly.toss.home

import android.content.Context
import android.graphics.*
import android.view.KeyEvent
import android.view.View
import kotlin.math.min

class RemoteInfoView(context: Context, private val server: RemoteControlServer, private val onClose: () -> Unit) : View(context) {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG); private val t=Paint(Paint.ANTI_ALIAS_FLAG); private val b=Paint(Paint.ANTI_ALIAS_FLAG).apply{typeface=Typeface.DEFAULT_BOLD}
    init { isFocusable=true; isFocusableInTouchMode=true }
    override fun onDraw(c:Canvas){
        val s=min(width/1280f,height/720f);c.save();c.translate((width-1280*s)/2,(height-720*s)/2);c.scale(s,s)
        p.shader=LinearGradient(0f,0f,1280f,720f,Color.rgb(20,10,34),Color.rgb(5,5,9),Shader.TileMode.CLAMP);c.drawRect(0f,0f,1280f,720f,p);p.shader=null
        b.color=Color.WHITE;b.textSize=36f;c.drawText("TOSS Control",72f,86f,b);t.color=Color.rgb(180,173,194);t.textSize=18f;c.drawText("Controle o projetor pelo tablet na mesma rede Wi-Fi",72f,120f,t)
        p.color=Color.rgb(28,23,39);c.drawRoundRect(RectF(72f,174f,1208f,550f),30f,30f,p)
        label(c,"ENDEREÇO DO PROJETOR",112f,235f);value(c,server.address(),112f,285f)
        label(c,"PORTA",112f,355f);value(c,server.port.toString(),112f,405f)
        label(c,"PIN DE PAREAMENTO",650f,235f);b.color=Color.rgb(193,153,255);b.textSize=58f;c.drawText(server.pin,650f,305f,b)
        t.color=Color.rgb(180,173,194);t.textSize=17f;c.drawText("No tablet, informe o IP e este PIN. O servidor aceita apenas conexões da rede local.",650f,370f,t)
        t.textSize=15f;c.drawText("BACK  Voltar",72f,660f,t);c.restore()
    }
    private fun label(c:Canvas,s:String,x:Float,y:Float){b.color=Color.rgb(193,153,255);b.textSize=16f;c.drawText(s,x,y,b)}
    private fun value(c:Canvas,s:String,x:Float,y:Float){b.color=Color.WHITE;b.textSize=34f;c.drawText(s,x,y,b)}
    override fun onKeyDown(k:Int,e:KeyEvent?):Boolean{if(k==KeyEvent.KEYCODE_BACK){onClose();return true};return super.onKeyDown(k,e)}
}
