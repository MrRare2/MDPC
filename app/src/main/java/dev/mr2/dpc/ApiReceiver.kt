package dev.mr2.dpc

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
import android.net.wifi.WifiManager
import android.os.Build
import android.os.HardwarePropertiesManager
import android.util.Log
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuBinderWrapper

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
        val receiver = Privilege.DAR
        val app = intent.getStringExtra("package")
        val permission = intent.getStringExtra("permission")
        val restriction = intent.getStringExtra("restriction")
        val account = intent.getStringExtra("account")
        val flags = intent.getIntExtra("flags", 0)
        val text = intent.getStringExtra("text")
        val time_ts = intent.getLongExtra("ts", 0L)
        val time_tz = intent.getStringExtra("tz")
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
		        "EMERGENCY_TRANSFER_DHIZUKU" -> {
		            val newAdmin = ComponentName("com.rosan.dhizuku", "com.rosan.dhizuku.server.DhizukuDAReceiver")
		            dpm.transferOwnership(receiver, newAdmin, null)
		        }
                else -> log += "\nInvalid action -> ${action}"
            }
            context.sendBroadcast(reply as? Intent ?: context.reply("NULL", ""))
        } catch (e: Exception) {
            e.printStackTrace()
            val message = (e::class.qualifiedName ?: "Exception") + ": " + (e.message ?: "")
            log += "\n$message"
        }
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

    companion object {
        private const val TAG = "API"
        private const val PACKAGE_STATUS = "dev.mr2.temp.PKG_STATUS"
    }
}
