package dev.mr2.dpc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val tcpService = Intent(context, TCPService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && SP.apiTcpEnabled) {
                context.startForegroundService(tcpService)
            } else {
                context.startService(tcpService)
            }
        }
    }
}
