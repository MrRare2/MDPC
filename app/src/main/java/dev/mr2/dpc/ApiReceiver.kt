package dev.mr2.dpc

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.app.admin.FactoryResetProtectionPolicy
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.Session
import android.content.pm.IPackageInstallerSession
import android.graphics.BitmapFactory
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.HardwarePropertiesManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.util.Pair
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuBinderWrapper
import java.io.File
import java.io.FileInputStream
import java.util.UUID

class ApiReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val requestKey = intent.getStringExtra("key")
        var log = "MDPC API request received action: ${intent.action}"
        if (SP.apiKeyHash.isNullOrEmpty()) return
        val key = SP.apiKeyHash
        val action = intent.action?.removePrefix("dev.mr2.dpc.api.")
        if (!key.isNullOrEmpty() && (requestKey?.hash() != key) && action?.startsWith("SPECIAL") != true) {
	        log += "\nUnauthorized"
	        Log.d(TAG, log)
	        return
        }
        val dpm = Privilege.DPM
        val wm = Privilege.WM
        val pm = Privilege.PIM
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val receiver = Privilege.DAR
        val app = intent.getStringExtra("package")
        val permission = intent.getStringExtra("permission")
        val restriction = intent.getStringExtra("restriction")
        val account = intent.getStringExtra("account")
        val flags = intent.getIntExtra("flags", 0)
        val text = intent.getStringExtra("text")
        val time_ts = intent.getLongExtra("ts", 0L)
        val time_tz = intent.getStringExtra("tz")
        val ssid = intent.getStringExtra("ssid")
        val bssid = intent.getStringExtra("bssid")
        val sharedKey = intent.getStringExtra("sharedKey")
        val wifiEnabled = intent.getBooleanExtra("wifiEnabled", true)
        val wifiHidden = intent.getBooleanExtra("wifiHidden", false)
        val wifiNetId = intent.getIntExtra("wifiNetId", -1)
        // notif channel / notif builder
        val notifChannelId = intent.getStringExtra("notifChannelId")
        val notifId = intent.getStringExtra("notifId")
        val notifChannelName = intent.getStringExtra("notifChannelName")
        val delete = intent.getBooleanExtra("delete", false)
        val notifPriority = intent.getStringExtra("notifPriority")
        val notifTitle = intent.getStringExtra("title")
        val notifContent = intent.getStringExtra("content")
        val notifIcon = intent.getStringExtra("icon")
        val notifImage = intent.getStringExtra("image")
        val notifVibratePattern = intent.getLongArrayExtra("vibratePattern")
        val notifAlertOnce = intent.getBooleanExtra("alertOnce", false)
        val notifActionOnClick = intent.getStringExtra("actionOnClick")
        val notifActionOnDelete = intent.getStringExtra("actionOnDelete")
        val notifGroup = intent.getStringExtra("notifGroup")
        val notifOngoing = intent.getBooleanExtra("notifOngoing", false)
        try {
            @SuppressWarnings("NewApi")
            val reply = when (action) {
                "SYSTEM_DISABLE_CAMERA" -> dpm.setCameraDisabled(receiver, true)
                "SYSTEM_ENABLE_CAMERA" -> dpm.setCameraDisabled(receiver, false) 
                "SYSTEM_DISABLE_SCRCAP" -> dpm.setScreenCaptureDisabled(receiver, true)
                "SYSTEM_ENABLE_SCRCAP" -> dpm.setScreenCaptureDisabled(receiver, false)
                "SYSTEM_DISABLE_STATBAR" -> dpm.setStatusBarDisabled(receiver, true)
                "SYSTEM_ENABLE_STATBAR" -> dpm.setStatusBarDisabled(receiver, false)
                "SYSTEM_DISABLE_AUTO_TIME" -> dpm.setAutoTimeEnabled(receiver, false)
                "SYSTEM_ENABLE_AUTO_TIME" -> dpm.setAutoTimeEnabled(receiver, true)
                "SYSTEM_DISABLE_AUTO_TZ" -> dpm.setAutoTimeZoneEnabled(receiver, false)
                "SYSTEM_ENABLE_AUTO_TZ" -> dpm.setAutoTimeZoneEnabled(receiver, true)
                "SYSTEM_DISABLE_AUTO_TIME_OLD" -> dpm.setAutoTimeRequired(receiver, false)
                "SYSTEM_ENABLE_AUTO_TIME_OLD" -> dpm.setAutoTimeRequired(receiver, true)
                "SYSTEM_MASTER_VOLUME_MUTE" -> dpm.setMasterVolumeMuted(receiver, true)
                "SYSTEM_MASTER_VOLUME_UNMUTE" -> dpm.setMasterVolumeMuted(receiver, false)
                "SYSTEM_DISABLE_BACKUP_SERVICE" -> dpm.setBackupServiceEnabled(receiver, false)
                "SYSTEM_ENABLE_BACKUP_SERVICE" -> dpm.setBackupServiceEnabled(receiver, true)
                "SYSTEM_DISABLE_BT_SHARE" -> dpm.setBluetoothContactSharingDisabled(receiver, true)
                "SYSTEM_ENABLE_BT_SHARE" -> dpm.setBluetoothContactSharingDisabled(receiver, false)
                "SYSTEM_DISABLE_COMMON_CRITERIA" -> dpm.setCommonCriteriaModeEnabled(receiver, false)
                "SYSTEM_ENABLE_COMMON_CRITERIA" -> dpm.setCommonCriteriaModeEnabled(receiver, true)
                "SYSTEM_DISABLE_USB_SIGNAL" -> dpm.isUsbDataSignalingEnabled = false
                "SYSTEM_ENABLE_USB_SIGNAL" -> dpm.isUsbDataSignalingEnabled = true
                "SYSTEM_DISABLE_KEYGUARD" -> dpm.setKeyguardDisabled(receiver, true)
                "SYSTEM_ENABLE_KEYGUARD" -> dpm.setKeyguardDisabled(receiver, false)
                "SYSTEM_LOCK_NOW_EVICT_CREDENTIAL_ENCRYPTION_KEY" -> dpm.lockNow(DevicePolicyManager.FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY)
                "SYSTEM_LOCK_NOW" -> dpm.lockNow()
                "SYSTEM_SET_TIME" -> dpm.setTime(receiver, time_ts)
                "SYSTEM_SET_TZ" -> dpm.setTimeZone(receiver, time_tz)
                "SYSTEM_SET_AUTO_TIME_POLICY" -> dpm.setAutoTimePolicy(flags)
                "SYSTEM_SET_AUTO_TZ_POLICY" -> dpm.setAutoTimePolicy(flags)
                "SYSTEM_SET_CONTENT_PROTECTION_POLICY" -> dpm.setContentProtectionPolicy(receiver, flags)
                "SYSTEM_SET_PERMISSION_POLICY" -> dpm.setPermissionPolicy(receiver, flags)
                "SYSTEM_SET_MTE_POLICY" -> dpm.setMtePolicy(flags)
                "SYSTEM_SET_NEARBY_APP_STREAMING_POLICY" -> dpm.setNearbyAppStreamingPolicy(flags)
                "SYSTEM_DISABLE_ACCOUNTS_MANAGEMENT" -> dpm.setAccountManagementDisabled(receiver, text, true)
                "SYSTEM_ENABLE_ACCOUNTS_MANAGEMENT" -> dpm.setAccountManagementDisabled(receiver, text, false)
                "SYSTEM_DISABLE_FRP_POLICY" -> {
                    val policy = FactoryResetProtectionPolicy.Builder().setFactoryResetProtectionEnabled(false).setFactoryResetProtectionAccounts(arrayOf(account).filterNotNull().toMutableList()).build()
                    dpm.setFactoryResetProtectionPolicy(receiver, policy)
                }
                "SYSTEM_ENABLE_FRP_POLICY" -> {
                    val policy = FactoryResetProtectionPolicy.Builder().setFactoryResetProtectionEnabled(true).setFactoryResetProtectionAccounts(arrayOf(account).filterNotNull().toMutableList()).build()
                    dpm.setFactoryResetProtectionPolicy(receiver, policy)
                }
                "SYSTEM_SET_ORGANIZATION_NAME" -> dpm.setOrganizationName(receiver, text)
                "SYSTEM_SET_ORGANIZATION_ID" -> dpm.setOrganizationId(text!!)
                "APP_SET_PERMISSION_DEFAULT" -> {
                    dpm.setPermissionGrantState(
                        receiver, app!!, permission!!,
                        DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT
                    )
                }
                "APP_SET_PERMISSION_GRANTED" -> {
                    dpm.setPermissionGrantState(
                        receiver, app!!, permission!!,
                        DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
                    )
                }
                "APP_SET_PERMISSION_DENIED" -> {
                    dpm.setPermissionGrantState(
                        receiver, app!!, permission!!,
                        DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED
                    )
                }
                "APP_HIDE" -> dpm.setApplicationHidden(receiver, app, true)
                "APP_UNHIDE" -> dpm.setApplicationHidden(receiver, app, false)
                "APP_SUSPEND" -> dpm.setPackagesSuspended(receiver, arrayOf(app), true).isEmpty()
                "APP_UNSUSPEND" -> dpm.setPackagesSuspended(receiver, arrayOf(app), false).isEmpty()
                "APP_ADD_UNINSTALL_BLOCK" -> { dpm.setUninstallBlocked(receiver, app!!, true); true }
                "APP_REMOVE_UNINSTALL_BLOCK" -> { dpm.setUninstallBlocked(receiver, app!!, false); true }
                "APP_UNINSTALL" -> {
                    val intent = Intent(PACKAGE_STATUS)
                    intent.setPackage(context.packageName)
                    val pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE).intentSender
                    pm?.uninstall(app!!, pi)
                }
                "SYSTEM_REBOOT" -> dpm.reboot(receiver)
                "USER_SET_LOCK_SCREEN_INFO" -> dpm.setDeviceOwnerLockScreenInfo(receiver, text)
                "USER_SET_SHORT_SUPPORT_MESSAGE" -> dpm.setShortSupportMessage(receiver, text)
                "USER_SET_LONG_SUPPORT_MESSAGE" -> dpm.setLongSupportMessage(receiver, text)
                "USER_SET_START_USER_SESSION_MESSAGE" -> dpm.setStartUserSessionMessage(receiver, text)
                "USER_SET_END_USER_SESSION_MESSAGE" -> dpm.setEndUserSessionMessage(receiver, text)
                "USER_ADD_RESTRICTION" -> dpm.addUserRestriction(receiver, restriction)
                "USER_REMOVE_RESTRICTION" -> dpm.clearUserRestriction(receiver, restriction)
                "SYSTEM_ENABLE_WIFI" -> wm?.setWifiEnabled(true)
                "SYSTEM_DISABLE_WIFI" -> wm?.setWifiEnabled(false)
                "SYSTEM_WIFI_RECONNECT" -> wm?.reconnect()
                "SYSTEM_WIFI_DISCONNECT" -> wm?.disconnect()
                "SYSTEM_WIFI_DISABLE_NETWORK" -> wm?.disableNetwork(wifiNetId)
                "SYSTEM_WIFI_ENABLE_NETWORK" -> wm?.enableNetwork(wifiNetId, wifiEnabled)
                "SYSTEM_REMOVE_WIFI_NETWORK" -> wm?.removeNetwork(wifiNetId)
                "SYSTEM_ADD_WIFI_NETWORK" -> {
                    val wc = WifiConfiguration().apply {
                        SSID = ssid!!.replace("\"", "\\\"")
                    }

                    if (!sharedKey.isNullOrEmpty()) wc.preSharedKey = sharedKey.replace("\"", "\\\"")
                    if (!bssid.isNullOrEmpty()) wc.BSSID = bssid
                    wc.hiddenSSID = wifiHidden
                    val netId = wm?.addNetwork(wc) ?: -1
                    wm?.enableNetwork(netId!!, wifiEnabled)
                    context.reply("WIFI_NET_ID", netId)
                }
		        "EMERGENCY_TRANSFER_DHIZUKU" -> {
		            val newAdmin = ComponentName("com.rosan.dhizuku", "com.rosan.dhizuku.server.DhizukuDAReceiver")
		            dpm.transferOwnership(receiver, newAdmin, null)
		        }
                "NOTIFY" -> {
                    val priority = when (notifPriority) {
                        "high", "max" -> NotificationManager.IMPORTANCE_HIGH
                        "low" -> NotificationManager.IMPORTANCE_LOW
                        "min" -> NotificationManager.IMPORTANCE_MIN
                        else -> NotificationManager.IMPORTANCE_DEFAULT
                    }

                    var channelId = notifChannelId ?: CHANNEL_ID
                    val existingChannels = nm.notificationChannels.map { it.id }
                    if (channelId !in existingChannels) {
                        if (CHANNEL_ID !in existingChannels) {
                            val defaultChannel = NotificationChannel(CHANNEL_ID, "API Notification Channel", priority)
                            nm.createNotificationChannel(defaultChannel)
                        }
                        channelId = CHANNEL_ID
                    }

                    val notification = NotificationCompat.Builder(context, channelId)
                    notification.setSmallIcon(R.drawable.info_fill0)
                    notification.color = 0xFF000000.toInt()
                    notification.setContentTitle(notifTitle)
                    if (notifContent?.contains("\n") ?: false) {
                        notification.setStyle(
                            NotificationCompat.BigTextStyle().bigText(notifContent)
                        )
                    } else notification.setContentText(notifContent)
                    notification.priority = priority
                    notification.setOngoing(notifOngoing)
                    notification.setOnlyAlertOnce(notifAlertOnce)
                    notification.setWhen(System.currentTimeMillis())
                    notification.setShowWhen(true)
                    var smallIconResourceId: Int? = null
                    if (notifIcon != null) {
                        val smallIconResourceName = String.format("%1s_fill0", notifIcon)
                        smallIconResourceId = context?.resources?.getIdentifier(
                            smallIconResourceName,
                            "drawable",
                            context.packageName
                        ).takeIf { it != 0 }
                    }

                    smallIconResourceId?.let { notification.setSmallIcon(it) }

                    notifGroup?.let { notification.setGroup(it) }
                    notification.setAutoCancel(true)

                    for (button in 1..3) {
                        intent.getStringExtra("actionOnButton${button}Text")?.let { buttonText ->
                            intent.getStringExtra("actionOnButton${button}Click")?.let { buttonAction ->
                                if (buttonAction.contains("\$REPLY")) {
                                    val action = createReplyAction(context, intent, button, buttonText, buttonAction, notifId!!)
                                    notification.addAction(action)
                                } else {
                                    val pi = createAction(context, buttonAction)
                                    notification.addAction(NotificationCompat.Action(android.R.drawable.ic_input_add, buttonText, pi))
                                }
                            }
                        }
                    }

                    if (!notifImage.isNullOrEmpty()) {
                        val img = File(notifImage)
                        if (img.exists()) {
                            val bmp = BitmapFactory.decodeFile(img.getAbsolutePath())
                            notification.setLargeIcon(bmp).setStyle(
                                NotificationCompat.BigPictureStyle().bigPicture(bmp)
                            )
                        }
                    }

                    notifActionOnClick?.let {
                        val pi = createAction(context, it)
                        notification.setContentIntent(pi)
                    }

                    notifActionOnDelete?.let {
                        val pi = createAction(context, it)
                        notification.setDeleteIntent(pi)
                    }

                    nm.notify(notifId ?: UUID.randomUUID().toString(), 6458376, notification.build())
                }
                "NOTIF_REMOVE" -> nm.cancel(notifId, 6458376)
                "SPECIAL_NOTIF_REPLY" -> {
                    val remoteInput = RemoteInput.getResultsFromIntent(intent)
                    val reply = remoteInput?.getCharSequence(KEY_TEXT_REPLY)
                    if (reply.isNullOrEmpty()) return
                    val replyIntent = Intent("dev.mr2.dpc.notif.ACTION_${intent.getStringExtra("action")?.replace("\$REPLY", "")}").apply {
                        putExtra("dev.mr2.extra.EXTRA_REPLY", reply)
                        putExtra("dev.mr2.extra.EXTRA_NOTIFID", notifId)
                    }
                    nm.cancel(notifId, 6458376)
                    context.sendBroadcast(replyIntent)
                }
                else -> log += "\nInvalid action -> ${action}"
            }
            context.sendBroadcast(reply as? Intent ?: context.reply("NULL", ""))
        } catch (e: Exception) {
            e.printStackTrace()
            val message = (e::class.qualifiedName ?: "Exception") + ": " + (e.message ?: "")
            log += "\n$message"
        }
	    context.sendBroadcast(context.reply("LOG_$TAG", log, true))
        Log.d(TAG, log)
    }

    private fun wrapSession(session: Session) {
        val field = session.javaClass.getDeclaredField("mSession")
        field.isAccessible = true
        val oldInterface = field.get(session) as IPackageInstallerSession
        val oldBinder = oldInterface.asBinder()
        val newBinder = Dhizuku.binderWrapper(oldBinder)
        val newInterface = IPackageInstallerSession.Stub.asInterface(newBinder)
        if (newInterface != null) field.set(session, newInterface)
    }

    private fun createAction(context: Context, action: String): PendingIntent {
        val broadcastIntent = Intent("dev.mr2.dpc.notif.ACTION_$action").apply { putExtra("dev.mr2.extra.EXTRA_CLICKED", true) }

        return PendingIntent.getBroadcast(
            context,
            (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            broadcastIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
    }

    fun createReplyAction(context: Context, intent: Intent, buttonNum: Int, buttonText: String, buttonAction: String, notificationId: String): NotificationCompat.Action {
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel(buttonText)
            .build()

        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            buttonNum,
            getMessageReplyIntent(intent, buttonAction, notificationId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        return NotificationCompat.Action.Builder(
            R.drawable.info_fill0,
            buttonText,
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()
    }

    private fun getMessageReplyIntent(intent: Intent, buttonAction: String, notificationId: String): Intent {
        return Intent("dev.mr2.dpc.api.SPECIAL_NOTIF_REPLY").apply {
            setComponent(ComponentName("dev.mr2.dpc", "dev.mr2.dpc.ApiReceiver"))
            putExtra("action", buttonAction)
            putExtra("notifId", notificationId)
        }
    }

    companion object {
        private const val TAG = "API"
        private const val PACKAGE_STATUS = "dev.mr2.temp.PKG_STATUS"
        private const val CHANNEL_ID = "api-notification"
        private const val KEY_TEXT_REPLY = "mdpc-reply"
    }
}
