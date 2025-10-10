package dev.mr2.dpc

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class TelCodeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val launchIntent = Intent().apply {
	        component = ComponentName("dev.mr2.dpc", "dev.mr2.dpc.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.reply("TEL_CODE_ACTIVATED", true)?.also(context::sendBroadcast)
        context.startActivity(launchIntent)
    }
}
