package com.taly.toss.control

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

class RemoteClient(private val port:Int=47990){
    fun send(ip:String,pin:String,command:String):Result<String> = runCatching {
        require(ip.isNotBlank() && pin.length==6){"Informe IP e PIN de 6 dígitos"}
        Socket().use{s->s.connect(InetSocketAddress(ip.trim(),port),1800);s.soTimeout=2500;PrintWriter(s.getOutputStream(),true).println("TOSS1|${pin.trim()}|$command");val reply=BufferedReader(InputStreamReader(s.getInputStream())).readLine()?:error("sem resposta");if(reply.startsWith("ERR|"))error(reply.removePrefix("ERR|"));reply.removePrefix("OK|")}
    }
}
