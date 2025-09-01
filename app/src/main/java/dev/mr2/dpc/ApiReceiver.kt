package dev.mr2.dpc

import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.app.admin.FactoryResetProtectionPolicy
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.HardwarePropertiesManager
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
	val wifiNetId = intent.getIntExtra("wifiNetId", -1)
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
		// get data (only retrivable if you listen on the sender)
		"GET_CPU_TEMPERATURES" -> {
		    val cpuTemps = hwm?.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_CPU, flags)
		    context.reply("CPU_TEMPERATURES", cpuTemps!!.joinToString(":"))
		}
		"GET_GPU_TEMPERATURES" -> {
		    val gpuTemps = hwm?.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_GPU, flags)
		    context.reply("GPU_TEMPERATURES", gpuTemps!!.joinToString(":"))
		}
		"GET_BATTERY_TEMPERATURES" -> {
                    val batteryTemps = hwm?.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_BATTERY, flags)
                    context.reply("BATTERY_TEMPERATURES", batteryTemps!!.joinToString(":"))
	        }
		"GET_SKIN_TEMPERATURES" -> {
                    val skinTemps = hwm?.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_SKIN, flags)
                    context.reply("SKIN_TEMPERATURES", skinTemps!!.joinToString(":"))
		}
		"GET_ORGANIZATION_NAME" -> context.reply("ORGANIZATION_NAME", dpm.getOrganizationName(receiver) ?: "")
		"GET_SHORT_SUPPORT_MESSAGE" -> context.reply("SHORT_SUPPORT_MESSAGE", dpm.getShortSupportMessage(receiver) ?: "")
		"GET_LONG_SUPPORT_MESSAGE"  -> context.reply("LONG_SUPPORT_MESSAGE", dpm.getLongSupportMessage(receiver) ?: "")
		"GET_LOCK_SCREEN_SCREEN_INFO_MESSAGE" -> context.reply("LOCK_SCREEN_MESSAGE", dpm.getDeviceOwnerLockScreenInfo() ?: "")
		"GET_START_SESSION_MESSAGE" -> {
		    context.reply("START_SESSION_MESSAGE", dpm.getStartUserSessionMessage(receiver) ?: "")
		    true
		}
		"GET_END_SESSION_MESSAGE" -> context.reply("END_SESSION_MESSAGE", dpm.getEndUserSessionMessage(receiver) ?: "")
		"GET_DEVICE_OWNER_PACKAGE" -> context.reply("DEVICE_OWNER", receiver.packageName)
		"GET_DEVICE_OWNER_COMPONENT" -> context.reply("DEVICE_OWNER_COMPONENT", receiver.flattenToString())
		"GET_AUTO_TIME_STATE" -> context.reply("AUTO_TIME", dpm.getAutoTimeEnabled(receiver))
		"GET_AUTO_TIME_POLICY" -> context.reply("AUTO_TIME_POLICY", dpm.getAutoTimePolicy())
		"GET_AUTO_TIME_ZONE_STATE" -> context.reply("AUTO_TIME_ZONE", dpm.getAutoTimeZoneEnabled(receiver))
		"GET_AUTO_TIME_ZONE_POLICY" -> context.reply("AUTO_TIME_POLICY", dpm.getAutoTimeZonePolicy())
		"GET_BLUETOOTH_CONTACT_SHARING_STATE" -> context.reply("BLUETOOTH_CONTACT_SHARING", !dpm.getBluetoothContactSharingDisabled(receiver))
		"GET_CAMERA_STATE" -> context.reply("CAMERA", !dpm.getCameraDisabled(receiver))
		"GET_CONTENT_PROTECTION_POLICY" -> context.reply("CONTENT_PROTECTION_POLICY", dpm.getContentProtectionPolicy(receiver))
		"GET_FAILED_PASSWORD_ATTEMPTS" -> context.reply("FAILED_PASSWORD_ATTEMPT", dpm.getCurrentFailedPasswordAttempts())
		"GET_DPM_ROLE_HOLDER_PACKAGE" -> context.reply("DPM_ROLE_HOLDER_PACKAGE", dpm.getDevicePolicyManagementRoleHolderPackage() ?: "")
		"GET_ENROLLMENT_SPECIFIC_ID" -> context.reply("ENROLLMENR_SPECIFIC_ID", dpm.getEnrollmentSpecificId())
		"GET_GLOBAL_PRIVATE_DNS" -> context.reply("GLOBAL_PRIVATE_DNS", dpm.getGlobalPrivateDnsHost(receiver) ?: "")
		"GET_GLOBAL_PRIVATE_DNS_MODE" -> context.reply("GLOBAL_PRIVATE_DNS_MODE", dpm.getGlobalPrivateDnsMode(receiver))
		"GET_KEEP_UNINSTALL_PACKAGES" -> context.reply("KEEP_UNINSTALL_PACKAGES", dpm.getKeepUninstalledPackages(receiver) ?: mutableListOf<String>())
		"GET_KEYGUARD_DISABLED_FEATURES" -> context.reply("KEYGUARD_DISABLED_FEATURES", dpm.getKeyguardDisabledFeatures(receiver))
		"GET_LOCK_TASK_FEATURES" -> context.reply("LOCK_TASK_FEATURES", dpm.getLockTaskFeatures(receiver))
		"GET_LOCK_TASK_PACKAGES" -> context.reply("LOCK_TASK_PACKAGES", dpm.getLockTaskPackages(receiver))
		"GET_MAXIMUM_FAILED_PASSWORD_ATTEMPTS_FOR_WIPE" -> context.reply("MAXIMUM_FAILED_PASSWORD_ATTEMPTS_FOR_WIPE", dpm.getMaximumFailedPasswordsForWipe(receiver))
		"GET_MAXIMUN_TIME_TO_LOCK" -> context.reply("MAXIMUM_TIME_TO_LOCK", dpm.getMaximumTimeToLock(receiver))
		"GET_METERED_DATA_DISABLED_PACKAGES" -> context.reply("METERED_DATA_DISABLED_PACKAGES", dpm.getMeteredDataDisabledPackages(receiver))
		"GET_MINIMUM_WIFI_SECURITY_LEVEL" -> context.reply("MINIMUM_WIFI_SECURITY_LEVEL", dpm.getMinimumRequiredWifiSecurityLevel())
		"GET_MTE_POLICY" -> context.reply("MTE_POLICY", dpm.getMtePolicy())
		// TODO: not done, but this is where it will end
		"HAS_LOCKDOWN_ADMIN_CONFIGURED_NETWORKS" -> context.reply("LOCKDOWN_ADMIN_CONFIGURED_NETWORKS", dpm.hasLockdownAdminConfiguredNetworks(receiver))
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
