package com.taly.toss.control

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.concurrent.Executors

class MainActivity:Activity(){
    private val bg=Color.rgb(7,7,12);private val surface=Color.rgb(20,18,29);private val surfaceHigh=Color.rgb(31,25,44);private val purple=Color.rgb(132,72,255);private val bright=Color.rgb(188,145,255);private val muted=Color.rgb(164,159,177);private val executor=Executors.newSingleThreadExecutor();private val client=RemoteClient();private lateinit var ip:EditText;private lateinit var pin:EditText;private lateinit var status:TextView
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.statusBarColor=bg;window.navigationBarColor=bg;val prefs=getSharedPreferences("remote",MODE_PRIVATE)
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(28,24,28,28);background=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(Color.rgb(24,11,43),bg,Color.rgb(5,5,9)))}
        root.addView(text("TOSS Control",30,true));root.addView(text("Seu projetor, na sua mão.",15,false));val pair=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(0,18,0,10)};ip=input("IP do projetor",prefs.getString("ip","")?:"");pin=input("PIN",prefs.getString("pin","")?:"").apply{inputType=InputType.TYPE_CLASS_NUMBER};pair.addView(ip,LinearLayout.LayoutParams(0,58,2f));pair.addView(pin,LinearLayout.LayoutParams(0,58,1f).apply{marginStart=10});pair.addView(button("Conectar","PING",true),LinearLayout.LayoutParams(0,58,1f).apply{marginStart=10});root.addView(pair);status=text("Informe IP e PIN exibidos no TOSS",13,false);root.addView(status)
        val scroll=ScrollView(this).apply{isFillViewport=true};val content=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(0,16,0,18)}
        content.addView(section("NAVEGAÇÃO"));content.addView(row(button("Home","HOME"),button("Menu","MENU"),button("Voltar","BACK")));content.addView(spacer(10));content.addView(row(spacer(1),button("▲","UP",true),spacer(1)));content.addView(row(button("◀","LEFT",true),button("OK","OK",true),button("▶","RIGHT",true)));content.addView(row(spacer(1),button("▼","DOWN",true),spacer(1)))
        content.addView(section("ATALHOS"));content.addView(row(button("YouTube","YOUTUBE"),button("Netflix","NETFLIX"),button("Prime Video","PRIME_VIDEO")));content.addView(spacer(9));content.addView(row(button("Disney+","DISNEY"),button("Spotify","SPOTIFY"),button("Apps","APPS")));content.addView(spacer(9));content.addView(row(button("Cinema","CINEMA"),button("HDMI","HDMI"),button("Espelhar","CAST")))
        content.addView(section("PROJETOR"));content.addView(row(button("Foco −","FOCUS_MINUS"),button("Foco +","FOCUS_PLUS"),button("Projetor","PROJECTOR")));content.addView(spacer(9));content.addView(row(button("Vol −","VOLUME_DOWN"),button("Vol +","VOLUME_UP"),button("Ajustes","SETTINGS")));content.addView(spacer(9));content.addView(row(button("Arquivos","FILES"),button("Música","MUSIC"),button("Navegador","BROWSER")))
        scroll.addView(content);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f));setContentView(root)}
    private fun rounded(color:Int,stroke:Int=Color.argb(28,255,255,255),radius:Float=18f)=GradientDrawable().apply{setColor(color);cornerRadius=radius;setStroke(1,stroke)}
    private fun input(hint:String,value:String)=EditText(this).apply{this.hint=hint;setText(value);setTextColor(Color.WHITE);setHintTextColor(muted);background=rounded(Color.rgb(16,14,23));setPadding(18,0,18,0);inputType=InputType.TYPE_CLASS_TEXT}
    private fun text(value:String,size:Int,bold:Boolean)=TextView(this).apply{text=value;textSize=size.toFloat();setTextColor(if(bold)Color.WHITE else muted);if(bold)setTypeface(typeface,1)}
    private fun section(value:String)=text(value,12,true).apply{setTextColor(bright);setPadding(2,22,0,10);letterSpacing=.12f}
    private fun button(label:String,command:String,accent:Boolean=false)=Button(this).apply{text=label;textSize=13f;isAllCaps=false;setTextColor(Color.WHITE);background=rounded(if(accent)Color.rgb(67,39,105) else surface,if(accent)Color.argb(115,188,145,255) else Color.argb(28,255,255,255));setOnClickListener{send(command)}}
    private fun row(vararg views:View)=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;views.forEachIndexed{i,v->addView(v,LinearLayout.LayoutParams(0,62,1f).apply{if(i>0)marginStart=9})}}
    private fun spacer(h:Int)=Space(this).apply{minimumHeight=h}
    private fun send(command:String){val host=ip.text.toString().trim();val code=pin.text.toString().trim();getSharedPreferences("remote",MODE_PRIVATE).edit().putString("ip",host).putString("pin",code).apply();status.text="Enviando…";executor.execute{val r=client.send(host,code,command);runOnUiThread{status.text=r.fold({"✓ $it"},{"Erro: ${it.message}"})}}}
    override fun onDestroy(){executor.shutdownNow();super.onDestroy()}
}
