package com.taly.toss.home

import android.content.Context
import android.graphics.*
import android.view.KeyEvent
import android.view.View
import kotlin.math.min

class RemoteInfoView(context: Context, private val server: RemoteControlServer, private val pwa: PwaRemoteServer, private val onClose: () -> Unit) : View(context) {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG); private val t=Paint(Paint.ANTI_ALIAS_FLAG); private val b=Paint(Paint.ANTI_ALIAS_FLAG).apply{typeface=Typeface.DEFAULT_BOLD}
    init { isFocusable=true; isFocusableInTouchMode=true }
    override fun onDraw(c:Canvas){
        val s=min(width/1280f,height/720f);c.save();c.translate((width-1280*s)/2,(height-720*s)/2);c.scale(s,s)
        p.shader=LinearGradient(0f,0f,1280f,720f,Color.rgb(20,10,34),Color.rgb(5,5,9),Shader.TileMode.CLAMP);c.drawRect(0f,0f,1280f,720f,p);p.shader=null
        b.color=Color.WHITE;b.textSize=36f;c.drawText("TOSS Control",72f,86f,b);t.color=Color.rgb(180,173,194);t.textSize=18f;c.drawText("Controle o projetor pelo tablet, iPhone ou navegador na mesma rede Wi-Fi",72f,120f,t)
        p.color=Color.rgb(28,23,39);c.drawRoundRect(RectF(72f,166f,1208f,568f),30f,30f,p)
        label(c,"IPHONE / SAFARI",112f,222f);value(c,pwa.url(),112f,266f,29f)
        t.color=Color.rgb(180,173,194);t.textSize=16f;c.drawText("Abra este endereço no Safari e use Compartilhar → Adicionar à Tela de Início.",112f,302f,t)
        label(c,"APP ANDROID",112f,374f);value(c,server.address()+":"+server.port,112f,418f,28f)
        label(c,"PIN DE PAREAMENTO",650f,374f);b.color=Color.rgb(193,153,255);b.textSize=52f;c.drawText(server.pin,650f,432f,b)
        t.color=Color.rgb(180,173,194);t.textSize=16f;c.drawText("O controle funciona localmente e aceita apenas conexões da sua rede.",650f,478f,t)
        t.textSize=15f;c.drawText("BACK  Voltar",72f,660f,t);c.restore()
    }
    private fun label(c:Canvas,s:String,x:Float,y:Float){b.color=Color.rgb(193,153,255);b.textSize=16f;c.drawText(s,x,y,b)}
    private fun value(c:Canvas,s:String,x:Float,y:Float,size:Float){b.color=Color.WHITE;b.textSize=size;c.drawText(s,x,y,b)}
    override fun onKeyDown(k:Int,e:KeyEvent?):Boolean{if(k==KeyEvent.KEYCODE_BACK){onClose();return true};return super.onKeyDown(k,e)}
}
