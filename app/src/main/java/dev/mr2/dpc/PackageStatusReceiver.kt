package dev.mr2.dpc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller

class PackageStatusReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
        val statusMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
        val sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1)
        val otherPackageName = intent.getStringExtra(PackageInstaller.EXTRA_OTHER_PACKAGE_NAME)
        val reply = "$status|=|$statusMessage|=|$packageName|=|$sessionId|=|$otherPackageName"

        context.reply("PACKAGE_STATUS", reply)
    }
}
