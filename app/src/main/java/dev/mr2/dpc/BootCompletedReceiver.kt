package dev.mr2.dpc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("MDPC-boot", "received")
        val tcpService = Intent(context, TCPService::class.java)
        if (SP.apiTcpEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(tcpService)
            else context.startService(tcpService)
        }
    }
}
