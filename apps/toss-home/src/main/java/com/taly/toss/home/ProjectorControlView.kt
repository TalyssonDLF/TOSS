package com.taly.toss.home

import android.content.Context
import android.graphics.*
import android.view.KeyEvent
import android.view.View
import kotlin.math.min

/** Full projector controls UI. Values are local preview state until a device adapter is connected. */
class ProjectorControlView(context: Context, private val onClose: () -> Unit) : View(context) {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG); private val t=Paint(Paint.ANTI_ALIAS_FLAG); private val b=Paint(Paint.ANTI_ALIAS_FLAG).apply{typeface=Typeface.DEFAULT_BOLD}
    private val purple=Color.rgb(151,91,255); private val bright=Color.rgb(193,153,255); private val white=Color.rgb(247,245,251); private val muted=Color.rgb(171,165,184)
    private var selected=0; private var focus=50; private var zoom=100; private var brightness=72; private var keystone=0
    private var orientation=0; private var aspect=0; private var source=0; private var profile=0
    private val orientationNames=listOf("Normal","Invertido","Espelhado"); private val aspectNames=listOf("Auto","16:9","4:3"); private val sourceNames=listOf("TOSS","HDMI"); private val profileNames=listOf("Cinema","Jogo","Eco","Padrão")
    init{isFocusable=true;isFocusableInTouchMode=true;requestFocus()}
    override fun onDraw(c:Canvas){val s=min(width/1280f,height/720f);c.save();c.translate((width-1280*s)/2,(height-720*s)/2);c.scale(s,s)
        p.shader=LinearGradient(0f,0f,1280f,720f,intArrayOf(Color.rgb(20,10,34),Color.rgb(9,7,14),Color.rgb(5,5,9)),null,Shader.TileMode.CLAMP);c.drawRect(0f,0f,1280f,720f,p);p.shader=null
        b.color=white;b.textSize=34f;c.drawText("Projetor",64f,70f,b);t.color=muted;t.textSize=17f;c.drawText("Imagem, geometria e fonte",64f,99f,t);t.textAlign=Paint.Align.RIGHT;c.drawText("BACK  Voltar",1216f,70f,t);t.textAlign=Paint.Align.LEFT
        panel(c,64f,130f,560f,626f,"Imagem"); slider(c,0,"Foco",focus,96f,194f); slider(c,1,"Zoom",zoom,96f,282f); slider(c,2,"Brilho",brightness,96f,370f)
        b.color=white;b.textSize=18f;c.drawText("Keystone",96f,469f,b); val kr=RectF(96f,492f,528f,582f);card(c,kr,selected==3);p.style=Paint.Style.STROKE;p.strokeWidth=3f;p.color=if(selected==3)bright else Color.rgb(100,87,120);val inset=20f+keystone*0.5f;val path=Path().apply{moveTo(kr.left+inset,kr.top+18);lineTo(kr.right-inset,kr.top+18);lineTo(kr.right-20,kr.bottom-18);lineTo(kr.left+20,kr.bottom-18);close()};c.drawPath(path,p);p.style=Paint.Style.FILL;t.color=muted;t.textSize=14f;c.drawText("${keystone}°   ← / →",112f,610f,t)
        panel(c,592f,130f,624f,626f,"Configuração"); choice(c,4,"Orientação",orientationNames,624f,194f,orientation);choice(c,5,"Proporção",aspectNames,624f,294f,aspect);choice(c,6,"Fonte",sourceNames,624f,394f,source);choice(c,7,"Perfil",profileNames,624f,494f,profile)
        t.color=muted;t.textSize=14f;c.drawText("Controles são prévia local até o ProjectorControl detectar as capacidades reais do dispositivo.",64f,684f,t);c.restore()}
    private fun panel(c:Canvas,x:Float,y:Float,w:Float,h:Float,title:String){p.color=Color.argb(205,19,15,28);c.drawRoundRect(RectF(x,y,x+w,y+h),26f,26f,p);b.color=bright;b.textSize=17f;c.drawText(title.uppercase(),x+32,y+42,b)}
    private fun card(c:Canvas,r:RectF,f:Boolean){p.color=if(f)Color.rgb(55,34,81) else Color.rgb(29,24,38);c.drawRoundRect(r,17f,17f,p);if(f){p.style=Paint.Style.STROKE;p.strokeWidth=2.5f;p.color=bright;c.drawRoundRect(r,17f,17f,p);p.style=Paint.Style.FILL}}
    private fun slider(c:Canvas,id:Int,label:String,value:Int,x:Float,y:Float){val f=selected==id;b.color=if(f)bright else white;b.textSize=18f;c.drawText(label,x,y,b);t.textAlign=Paint.Align.RIGHT;t.color=muted;t.textSize=16f;c.drawText(if(id==1)"$value%" else "$value",528f,y,t);t.textAlign=Paint.Align.LEFT;p.color=Color.rgb(54,48,64);c.drawRoundRect(RectF(x,y+25,528f,y+33),4f,4f,p);val normalized=if(id==1)(value-50)/100f else value/100f;val end=x+(528-x)*normalized.coerceIn(0f,1f);p.color=purple;c.drawRoundRect(RectF(x,y+25,end,y+33),4f,4f,p);p.color=if(f)bright else purple;c.drawCircle(end,y+29,if(f)9f else 7f,p)}
    private fun choice(c:Canvas,id:Int,label:String,items:List<String>,x:Float,y:Float,current:Int){b.color=white;b.textSize=18f;c.drawText(label,x,y,b);var xx=x;items.forEachIndexed{i,v->val w=if(items.size>3)128f else 150f;val r=RectF(xx,y+20,xx+w,y+68);card(c,r,selected==id&&i==current);if(i==current&&! (selected==id)){p.style=Paint.Style.STROKE;p.strokeWidth=1.5f;p.color=purple;c.drawRoundRect(r,17f,17f,p);p.style=Paint.Style.FILL};t.textAlign=Paint.Align.CENTER;t.color=white;t.textSize=15f;c.drawText(v,r.centerX(),r.centerY()+5,t);t.textAlign=Paint.Align.LEFT;xx+=w+10}}
    override fun onKeyDown(k:Int,e:KeyEvent?):Boolean{when(k){KeyEvent.KEYCODE_BACK->{onClose();return true};KeyEvent.KEYCODE_DPAD_UP->selected=(selected-1).coerceAtLeast(0);KeyEvent.KEYCODE_DPAD_DOWN->selected=(selected+1).coerceAtMost(7);KeyEvent.KEYCODE_DPAD_LEFT->adjust(-1);KeyEvent.KEYCODE_DPAD_RIGHT->adjust(1);else->return super.onKeyDown(k,e)};invalidate();return true}
    private fun adjust(d:Int){when(selected){0->focus=(focus+d*2).coerceIn(0,100);1->zoom=(zoom+d*5).coerceIn(50,150);2->brightness=(brightness+d*5).coerceIn(0,100);3->keystone=(keystone+d).coerceIn(-20,20);4->orientation=(orientation+d).coerceIn(0,orientationNames.lastIndex);5->aspect=(aspect+d).coerceIn(0,aspectNames.lastIndex);6->source=(source+d).coerceIn(0,sourceNames.lastIndex);7->profile=(profile+d).coerceIn(0,profileNames.lastIndex)}}
}
