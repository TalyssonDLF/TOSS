package com.taly.toss.control

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.concurrent.Executors

class MainActivity:Activity(){
    private val purple=Color.rgb(151,91,255);private val bg=Color.rgb(9,7,14);private val panel=Color.rgb(28,23,39);private val executor=Executors.newSingleThreadExecutor();private val client=RemoteClient();private lateinit var ip:EditText;private lateinit var pin:EditText;private lateinit var status:TextView
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.statusBarColor=bg;window.navigationBarColor=bg;val prefs=getSharedPreferences("remote",MODE_PRIVATE);val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(32,28,32,28);setBackgroundColor(bg)}
        root.addView(text("TOSS Control",30,true));root.addView(text("Controle do projetor pela rede Wi-Fi local",15,false));val pair=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(0,18,0,12)};ip=input("IP do projetor",prefs.getString("ip","")?:"");pin=input("PIN",prefs.getString("pin","")?:"").apply{inputType=InputType.TYPE_CLASS_NUMBER};pair.addView(ip,LinearLayout.LayoutParams(0,58,2f));pair.addView(pin,LinearLayout.LayoutParams(0,58,1f).apply{marginStart=12});pair.addView(button("Conectar","PING"),LinearLayout.LayoutParams(0,58,1f).apply{marginStart=12});root.addView(pair)
        status=text("Informe o IP e PIN exibidos em Ajustes > Controle remoto",14,false);root.addView(status)
        root.addView(spacer(12));root.addView(row(button("HOME","HOME"),button("MENU","MENU"),button("VOLTAR","BACK")));root.addView(spacer(10));root.addView(row(spacer(1),button("▲","UP"),spacer(1)));root.addView(row(button("◀","LEFT"),button("OK","OK"),button("▶","RIGHT")));root.addView(row(spacer(1),button("▼","DOWN"),spacer(1)));root.addView(spacer(14));root.addView(row(button("YouTube","YOUTUBE"),button("HDMI","HDMI"),button("Apps","APPS"),button("Ajustes","SETTINGS")));root.addView(spacer(10));root.addView(row(button("Projetor","PROJECTOR"),button("Cinema","CINEMA"),button("Arquivos","FILES"),button("Espelhar","CAST")));root.addView(spacer(10));root.addView(row(button("Foco −","FOCUS_MINUS"),button("Foco +","FOCUS_PLUS"),button("Vol −","VOLUME_DOWN"),button("Vol +","VOLUME_UP")));setContentView(root)}
    private fun input(hint:String,value:String)=EditText(this).apply{this.hint=hint;setText(value);setTextColor(Color.WHITE);setHintTextColor(Color.GRAY);setBackgroundColor(panel);setPadding(18,0,18,0);inputType=InputType.TYPE_CLASS_TEXT}
    private fun text(value:String,size:Int,bold:Boolean)=TextView(this).apply{text=value;textSize=size.toFloat();setTextColor(if(bold)Color.WHITE else Color.LTGRAY);if(bold)setTypeface(typeface,1)}
    private fun button(label:String,command:String)=Button(this).apply{text=label;setTextColor(Color.WHITE);setBackgroundColor(purple);setOnClickListener{send(command)}}
    private fun row(vararg views:View)=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;views.forEachIndexed{i,v->addView(v,LinearLayout.LayoutParams(0,64,1f).apply{if(i>0)marginStart=10})}}
    private fun spacer(h:Int)=Space(this).apply{minimumHeight=h}
    private fun send(command:String){val host=ip.text.toString().trim();val code=pin.text.toString().trim();getSharedPreferences("remote",MODE_PRIVATE).edit().putString("ip",host).putString("pin",code).apply();status.text="Enviando…";executor.execute{val r=client.send(host,code,command);runOnUiThread{status.text=r.fold({"✓ $it"},{"Erro: ${it.message}"})}}}
    override fun onDestroy(){executor.shutdownNow();super.onDestroy()}
}
