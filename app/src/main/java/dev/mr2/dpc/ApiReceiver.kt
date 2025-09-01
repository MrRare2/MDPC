package dev.mr2.dpc

import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.app.admin.FactoryResetProtectionPolicy
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.util.Log

class ApiReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val requestKey = intent.getStringExtra("key")
        var log = "MDPC API request received. action: ${intent.action}"
        if(!SP.isApiEnabled) return
        val key = SP.apiKey
        if(!key.isNullOrEmpty() && key != requestKey) {
	    log += "Unauthorized"
	    Log.d(TAG, log)
	    return
        }
	val dpm = Privilege.DPM
	val pm = Privilege.PIM
	val wm = Privilege.WM
	val hwm = Privilege.HWM
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
        if (!app.isNullOrEmpty()) log += "\npackage: $app"
        if (!permission.isNullOrEmpty()) log += "\npermission: $permission"
        try {
            @SuppressWarnings("NewApi")
            val ok = when(intent.action?.removePrefix("dev.mr2.dpc.api.")) {
		"SYSTEM_DISABLE_CAMERA" -> { dpm.setCameraDisabled(receiver, true); true }
                "SYSTEM_ENABLE_CAMERA" -> { dpm.setCameraDisabled(receiver, false); true }
                "SYSTEM_DISABLE_SCRCAP" -> { dpm.setScreenCaptureDisabled(receiver, true); true }
                "SYSTEM_ENABLE_SCRCAP" -> { dpm.setScreenCaptureDisabled(receiver, false); true }
                "SYSTEM_DISABLE_STATBAR" -> dpm.setStatusBarDisabled(receiver, true)
                "SYSTEM_ENABLE_STATBAR" -> dpm.setStatusBarDisabled(receiver, false)
                "SYSTEM_DISABLE_AUTO_TIME" -> { dpm.setAutoTimeEnabled(receiver, false); true }
                "SYSTEM_ENABLE_AUTO_TIME" -> { dpm.setAutoTimeEnabled(receiver, true); true }
                "SYSTEM_DISABLE_AUTO_TZ" -> { dpm.setAutoTimeZoneEnabled(receiver, false); true }
                "SYSTEM_ENABLE_AUTO_TZ" -> { dpm.setAutoTimeZoneEnabled(receiver, true); true }
                "SYSTEM_DISABLE_AUTO_TIME_OLD" -> { dpm.setAutoTimeRequired(receiver, false); true }
                "SYSTEM_ENABLE_AUTO_TIME_OLD" -> { dpm.setAutoTimeRequired(receiver, true); true }
                "SYSTEM_DISABLE_MASTER_VOLUME_MUTED" -> { dpm.setMasterVolumeMuted(receiver, false); true }
                "SYSTEM_ENABLE_MASTER_VOLUME_MUTED" -> { dpm.setMasterVolumeMuted(receiver, true); true }
                "SYSTEM_DISABLE_BACKUP_SERVICE" -> { dpm.setBackupServiceEnabled(receiver, false); true }
                "SYSTEM_ENABLE_BACKUP_SERVICE" -> { dpm.setBackupServiceEnabled(receiver, true); true }
                "SYSTEM_DISABLE_BT_SHARE" -> { dpm.setBluetoothContactSharingDisabled(receiver, true); true }
                "SYSTEM_ENABLE_BT_SHARE" -> { dpm.setBluetoothContactSharingDisabled(receiver, false); true }
                "SYSTEM_DISABLE_COMMON_CRITERIA" -> { dpm.setCommonCriteriaModeEnabled(receiver, false); true }
                "SYSTEM_ENABLE_COMMON_CRITERIA" -> { dpm.setCommonCriteriaModeEnabled(receiver, true); true }
                "SYSTEM_DISABLE_USB_SIGNAL" -> { dpm.isUsbDataSignalingEnabled = false; true }
                "SYSTEM_ENABLE_USB_SIGNAL" -> { dpm.isUsbDataSignalingEnabled = true; true }
		"SYSTEM_DISABLE_KEYGUARD" -> dpm.setKeyguardDisabled(receiver, true)
                "SYSTEM_ENABLE_KEYGUARD" -> dpm.setKeyguardDisabled(receiver, false)
                "SYSTEM_LOCK_NOW_EVICT_CREDENTIAL_ENCRYPTION_KEY" -> dpm.lockNow(DevicePolicyManager.FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY)
                "SYSTEM_LOCK_NOW" -> { dpm.lockNow(); true }
		"SYSTEM_SET_TIME" -> dpm.setTime(receiver, time_ts)
		"SYSTEM_SET_TZ" -> dpm.setTimeZone(receiver, time_tz)
		"SYSTEM_SET_AUTO_TIME_POLICY" -> { dpm.setAutoTimePolicy(flags); true }
		"SYSTEM_SET_AUTO_TZ_POLICY" -> { dpm.setAutoTimePolicy(flags); true }
		"SYSTEM_SET_CONTENT_PROTECTION_POLICY" -> { dpm.setContentProtectionPolicy(receiver, flags); true }
		"SYSTEM_SET_PERMISSION_POLICY" -> { dpm.setPermissionPolicy(receiver, flags); true }
                "SYSTEM_SET_MTE_POLICY" -> { dpm.setMtePolicy(flags); true }
                "SYSTEM_SET_NEARBY_APP_STREAMING_POLICY" -> { dpm.setNearbyAppStreamingPolicy(flags); true }
		"SYSTEM_DISABLE_ACCOUNTS_MANAGEMENT" -> { dpm.setAccountManagementDisabled(receiver, text, true); true }
                "SYSTEM_ENABLE_ACCOUNTS_MANAGEMENT" -> { dpm.setAccountManagementDisabled(receiver, text, false); true }
                "SYSTEM_DISABLE_FRP_POLICY" -> {
                    val policy = FactoryResetProtectionPolicy.Builder().setFactoryResetProtectionEnabled(false).setFactoryResetProtectionAccounts(arrayOf(account).filterNotNull().toMutableList()).build();
                    dpm.setFactoryResetProtectionPolicy(receiver, policy);
		    true
                }
                "SYSTEM_ENABLE_FRP_POLICY" -> {
                    val policy = FactoryResetProtectionPolicy.Builder().setFactoryResetProtectionEnabled(true).setFactoryResetProtectionAccounts(arrayOf(account).filterNotNull().toMutableList()).build();
                    dpm.setFactoryResetProtectionPolicy(receiver, policy);
		    true
                }
                "SYSTEM_SET_ORGANIZATION_NAME" -> { dpm.setOrganizationName(receiver, text); true }
                "SYSTEM_SET_ORGANIZATION_ID" -> { dpm.setOrganizationId(text!!); true }
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
                    val intent = Intent("dev.mr2.NULL");
                    intent.setPackage(context.packageName);
                    val pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE).intentSender;
                    pm?.uninstall(app!!, pi);
		    true
                }
		"SYSTEM_REBOOT" -> { dpm.reboot(receiver); true }
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
		    true
                }
                else -> {
                    log += "\nInvalid action"
                    false
		}
            }
            log += "\nsuccess: $ok"
        } catch(e: Exception) {
            e.printStackTrace()
            val message = (e::class.qualifiedName ?: "Exception") + ": " + (e.message ?: "")
            log += "\n$message"
        }
	context.reply("LOG_$TAG", log)
        Log.d(TAG, log)
    }

    companion object {
        private const val TAG = "API"
    }
}
