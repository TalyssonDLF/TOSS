package com.taly.toss.home

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.min

/** Lightweight, asset-free system boot mark. No permanent animation loop. */
class TossBootView(context:Context,private val onFinished:()->Unit):View(context){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG);private val t=Paint(Paint.ANTI_ALIAS_FLAG).apply{typeface=Typeface.create("sans",Typeface.BOLD)}
    private var progress=0f;private var animator:ValueAnimator?=null
    override fun onAttachedToWindow(){super.onAttachedToWindow();animator=ValueAnimator.ofFloat(0f,1f).apply{duration=1450L;interpolator=DecelerateInterpolator(1.25f);addUpdateListener{progress=it.animatedValue as Float;invalidate()};doOnEnd{onFinished()};start()}}
    private fun ValueAnimator.doOnEnd(block:()->Unit){addListener(object:android.animation.AnimatorListenerAdapter(){override fun onAnimationEnd(animation:android.animation.Animator){block()}})}
    override fun onDetachedFromWindow(){animator?.cancel();super.onDetachedFromWindow()}
    override fun onDraw(c:Canvas){
        val s=min(width/1280f,height/720f);c.save();c.translate((width-1280*s)/2,(height-720*s)/2);c.scale(s,s);c.drawColor(Color.rgb(5,5,9))
        p.shader=RadialGradient(640f,350f,360f,Color.argb((100*progress).toInt(),113,49,220),Color.TRANSPARENT,Shader.TileMode.CLAMP);c.drawCircle(640f,350f,360f,p);p.shader=null
        val reveal=((progress-.08f)/.55f).coerceIn(0f,1f);val pulse=(1f-kotlin.math.abs(progress-.62f)*1.8f).coerceIn(.72f,1f)
        c.save();c.scale(.86f+.14f*pulse,.86f+.14f*pulse,640f,338f)
        p.style=Paint.Style.STROKE;p.strokeCap=Paint.Cap.ROUND;p.strokeWidth=22f;p.color=Color.argb((255*reveal).toInt(),190,145,255)
        val path=Path().apply{moveTo(566f,292f);lineTo(714f,292f);moveTo(640f,292f);lineTo(640f,397f);moveTo(590f,397f);lineTo(690f,397f)}
        val measure=PathMeasure(path,false);c.drawPath(path,p);p.style=Paint.Style.FILL;c.restore()
        t.textAlign=Paint.Align.CENTER;t.textSize=24f;t.letterSpacing=.24f;t.color=Color.argb((230*((progress-.38f)/.35f).coerceIn(0f,1f)).toInt(),247,246,250);c.drawText("TOSS",640f,465f,t);t.letterSpacing=0f
        val fade=((progress-.55f)/.3f).coerceIn(0f,1f);t.typeface=Typeface.create("sans",Typeface.NORMAL);t.textSize=13f;t.color=Color.argb((130*fade).toInt(),200,193,215);c.drawText("TALY OPEN SMART SYSTEM",640f,494f,t);c.restore()
    }
}
