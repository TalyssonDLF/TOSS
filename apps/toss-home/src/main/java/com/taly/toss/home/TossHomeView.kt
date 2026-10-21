package com.taly.toss.home

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import kotlin.math.min

/** Lightweight TV-first TOSS Home. Real actions are delegated to MainActivity. */
class TossHomeView(
    context: Context,
    private val youtubeIcon: Drawable? = null,
    private val onYouTube: () -> Result<Unit> = { Result.failure(IllegalStateException("YouTube não encontrado")) },
    private val onHdmi: () -> Result<Unit> = { Result.failure(UnsupportedOperationException("HDMI indisponível")) },
    private val onApps: () -> Unit = {},
    private val onProjector: () -> Unit = {},
    private val onSettings: () -> Unit = {}
) : View(context) {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG); private val t=Paint(Paint.ANTI_ALIAS_FLAG).apply{typeface=Typeface.create("sans",Typeface.NORMAL)}; private val b=Paint(Paint.ANTI_ALIAS_FLAG).apply{typeface=Typeface.create("sans",Typeface.BOLD)}
    private val purple=Color.rgb(151,91,255); private val bright=Color.rgb(190,148,255); private val white=Color.rgb(246,244,251); private val muted=Color.rgb(177,171,191)
    private val shortcuts=listOf("Cinema","YouTube","HDMI","Espelhar","Arquivos","Apps","Música","Navegador"); private val recommendations=listOf("Filmes","Séries","Sua biblioteca","Recentes")
    private var selected=1
    init{isFocusable=true;isFocusableInTouchMode=true;requestFocus()}

    override fun onDraw(c:Canvas){super.onDraw(c);val s=min(width/1280f,height/720f);c.save();c.translate((width-1280*s)/2,(height-720*s)/2);c.scale(s,s);background(c);status(c);hero(c);shortcuts(c);recommendations(c);bottom(c);c.restore()}
    private fun background(c:Canvas){p.shader=LinearGradient(0f,0f,1280f,720f,intArrayOf(Color.rgb(22,11,37),Color.rgb(10,8,16),Color.rgb(6,6,10)),floatArrayOf(0f,.48f,1f),Shader.TileMode.CLAMP);c.drawRect(0f,0f,1280f,720f,p);p.shader=null;p.shader=RadialGradient(905f,92f,430f,intArrayOf(Color.argb(100,119,45,219),Color.argb(30,85,35,155),Color.TRANSPARENT),null,Shader.TileMode.CLAMP);c.drawCircle(905f,92f,430f,p);p.shader=null}
    private fun status(c:Canvas){t.color=muted;t.textSize=18f;t.textAlign=Paint.Align.RIGHT;c.drawText("Wi-Fi   •   TOSS 0.1",1220f,48f,t)}
    private fun hero(c:Canvas){val r=RectF(64f,70f,1216f,292f);card(c,r,selected==0,true);t.textAlign=Paint.Align.LEFT;t.color=bright;t.textSize=17f;t.typeface=Typeface.DEFAULT_BOLD;c.drawText("CONTINUAR ASSISTINDO",98f,112f,t);b.color=white;b.textSize=38f;c.drawText("Continue de onde parou",98f,164f,b);t.color=muted;t.textSize=20f;t.typeface=Typeface.DEFAULT;c.drawText("Seu conteúdo recente aparecerá aqui",98f,200f,t);p.color=Color.argb(100,255,255,255);c.drawRoundRect(RectF(98f,242f,520f,248f),3f,3f,p);p.color=purple;c.drawRoundRect(RectF(98f,242f,286f,248f),3f,3f,p);p.color=Color.argb(185,151,91,255);c.drawCircle(1110f,181f,38f,p);p.color=white;c.drawPath(Path().apply{moveTo(1101f,163f);lineTo(1101f,199f);lineTo(1128f,181f);close()},p)}
    private fun shortcuts(c:Canvas){b.color=white;b.textSize=21f;b.textAlign=Paint.Align.LEFT;c.drawText("Acesso rápido",64f,335f,b);val gap=12f;val w=(1152f-gap*7)/8f;shortcuts.forEachIndexed{i,label->val left=64f+i*(w+gap);val r=RectF(left,354f,left+w,444f);card(c,r,selected==i+1);if(i==1&&youtubeIcon!=null)drawAppIcon(c,youtubeIcon,r.centerX(),383f) else glyph(c,r.centerX(),383f,i,selected==i+1);t.textAlign=Paint.Align.CENTER;t.textSize=16f;t.color=if(selected==i+1)white else Color.rgb(221,217,229);c.drawText(label,r.centerX(),425f,t)}}
    private fun drawAppIcon(c:Canvas,d:Drawable,x:Float,y:Float){val size=42;d.setBounds((x-size/2).toInt(),(y-size/2).toInt(),(x+size/2).toInt(),(y+size/2).toInt());d.draw(c)}
    private fun glyph(c:Canvas,x:Float,y:Float,i:Int,f:Boolean){p.style=Paint.Style.STROKE;p.strokeWidth=2.4f;p.strokeCap=Paint.Cap.ROUND;p.color=if(f)bright else Color.rgb(193,183,211);when(i){0->{c.drawRoundRect(RectF(x-15,y-11,x+15,y+11),5f,5f,p);c.drawPath(Path().apply{moveTo(x-3,y-6);lineTo(x-3,y+6);lineTo(x+7,y);close()},p)};2->{c.drawRect(x-15,y-10,x+15,y+10,p);c.drawLine(x-8,y+16,x+8,y+16,p);c.drawLine(x,y+10,x,y+16,p)};3->{c.drawArc(RectF(x-15,y-15,x+15,y+15),215f,110f,false,p);c.drawArc(RectF(x-9,y-9,x+9,y+9),215f,110f,false,p)};4->{c.drawRect(x-15,y-8,x+15,y+11,p);c.drawLine(x-13,y-8,x-5,y-15,p);c.drawLine(x-5,y-15,x+3,y-15,p)};5->for(xx in listOf(-9f,9f))for(yy in listOf(-9f,9f))c.drawRoundRect(RectF(x+xx-5,y+yy-5,x+xx+5,y+yy+5),2f,2f,p);6->{c.drawCircle(x-7,y+8,6f,p);c.drawCircle(x+10,y+4,6f,p);c.drawLine(x-1,y+8,x-1,y-12,p);c.drawLine(x-1,y-12,x+16,y-16,p);c.drawLine(x+16,y-16,x+16,y+4,p)};else->{c.drawCircle(x,y,14f,p);c.drawLine(x-14,y,x+14,y,p);c.drawArc(RectF(x-7,y-14,x+7,y+14),90f,180f,false,p)}};p.style=Paint.Style.FILL}
    private fun recommendations(c:Canvas){b.color=white;b.textSize=21f;b.textAlign=Paint.Align.LEFT;c.drawText("Para você",64f,492f,b);recommendations.forEachIndexed{i,label->val left=64f+i*288f;val r=RectF(left,511f,left+270f,624f);card(c,r,selected==i+9);p.shader=LinearGradient(r.left,r.top,r.right,r.bottom,Color.argb(80,151,91,255),Color.argb(5,151,91,255),Shader.TileMode.CLAMP);c.drawRoundRect(r,18f,18f,p);p.shader=null;t.color=white;t.textSize=18f;t.textAlign=Paint.Align.LEFT;c.drawText(label,r.left+18f,r.bottom-20f,t)}}
    private fun bottom(c:Canvas){pill(c,RectF(64f,654f,207f,700f),"Projetor",selected==13);pill(c,RectF(1052f,654f,1216f,700f),"Ajustes",selected==14)}
    private fun pill(c:Canvas,r:RectF,label:String,f:Boolean){p.color=if(f)Color.argb(235,91,46,160)else Color.argb(150,25,21,34);c.drawRoundRect(r,23f,23f,p);if(f){p.style=Paint.Style.STROKE;p.strokeWidth=2f;p.color=bright;c.drawRoundRect(r,23f,23f,p);p.style=Paint.Style.FILL};t.textAlign=Paint.Align.CENTER;t.color=white;t.textSize=17f;c.drawText(label,r.centerX(),r.centerY()+6f,t)}
    private fun card(c:Canvas,r:RectF,f:Boolean,hero:Boolean=false){p.color=if(f)Color.argb(225,36,25,54)else if(hero)Color.argb(205,18,15,27)else Color.argb(175,27,22,39);c.drawRoundRect(r,if(hero)28f else 18f,if(hero)28f else 18f,p);p.style=Paint.Style.STROKE;p.strokeWidth=if(f)2.5f else 1f;p.color=if(f)bright else Color.argb(32,255,255,255);c.drawRoundRect(r,if(hero)28f else 18f,if(hero)28f else 18f,p);p.style=Paint.Style.FILL}

    override fun onKeyDown(k:Int,e:KeyEvent?):Boolean{val old=selected;selected=when(k){KeyEvent.KEYCODE_DPAD_UP->up(selected);KeyEvent.KEYCODE_DPAD_DOWN->down(selected);KeyEvent.KEYCODE_DPAD_LEFT->left(selected);KeyEvent.KEYCODE_DPAD_RIGHT->right(selected);KeyEvent.KEYCODE_DPAD_CENTER,KeyEvent.KEYCODE_ENTER->{activate(selected);return true};else->return super.onKeyDown(k,e)};if(old!=selected)invalidate();return true}
    private fun up(i:Int)=when(i){in 1..8->0;in 9..12->((i-9)*2+1).coerceAtMost(8);13,14->9;else->i};private fun down(i:Int)=when(i){0->1;in 1..8->9+((i-1)/2).coerceAtMost(3);in 9..12->if(i<=10)13 else 14;else->i};private fun left(i:Int)=when(i){in 2..8->i-1;in 10..12->i-1;14->13;else->i};private fun right(i:Int)=when(i){in 1..7->i+1;in 9..11->i+1;13->14;else->i}
    private fun activate(i:Int){when(i){2->onYouTube().onFailure{toast("YouTube: ${it.message ?: "não foi possível abrir"}")};3->onHdmi().onFailure{toast("HDMI: ${it.message ?: "não foi possível abrir"}")};6->onApps();13->onProjector();14->onSettings();else->{val label=when(i){0->"Continuar assistindo";in 1..8->shortcuts[i-1];in 9..12->recommendations[i-9];else->"TOSS"};toast("$label — integração em desenvolvimento")}}}
    private fun toast(message:String)=Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
}
