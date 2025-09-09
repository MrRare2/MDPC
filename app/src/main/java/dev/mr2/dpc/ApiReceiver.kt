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
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.HardwarePropertiesManager
import android.util.Log
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuBinderWrapper
import java.io.File
import java.io.FileInputStream

class ApiReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val requestKey = intent.getStringExtra("key")
        var log = "MDPC API request received action: ${intent.action}"
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
	val quality = intent.getIntExtra("quality", -1)
	val apkPath = intent.getStringExtra("apkPath")
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
                    val intent = Intent(PACKAGE_STATUS)
                    intent.setPackage(context.packageName)
                    val pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE).intentSender
                    pm?.uninstall(app!!, pi)
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
		    context.reply("CPU_TEMPERATURES", cpuTemps!!.joinToString("|=|"))
		}
		"GET_GPU_TEMPERATURES" -> {
		    val gpuTemps = hwm?.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_GPU, flags)
		    context.reply("GPU_TEMPERATURES", gpuTemps!!.joinToString("|=|"))
		}
		"GET_BATTERY_TEMPERATURES" -> {
                    val batteryTemps = hwm?.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_BATTERY, flags)
                    context.reply("BATTERY_TEMPERATURES", batteryTemps!!.joinToString("|=|"))
	        }
		"GET_SKIN_TEMPERATURES" -> {
                    val skinTemps = hwm?.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_SKIN, flags)
                    context.reply("SKIN_TEMPERATURES", skinTemps!!.joinToString("|=|"))
		}
		"GET_ORGANIZATION_NAME" -> context.reply("ORGANIZATION_NAME", dpm.getOrganizationName(receiver) ?: "")
		"GET_SHORT_SUPPORT_MESSAGE" -> context.reply("SHORT_SUPPORT_MESSAGE", dpm.getShortSupportMessage(receiver) ?: "")
		"GET_LONG_SUPPORT_MESSAGE"  -> context.reply("LONG_SUPPORT_MESSAGE", dpm.getLongSupportMessage(receiver) ?: "")
		"GET_LOCK_SCREEN_SCREEN_INFO_MESSAGE" -> context.reply("LOCK_SCREEN_MESSAGE", dpm.getDeviceOwnerLockScreenInfo() ?: "")
		"GET_START_SESSION_MESSAGE" -> context.reply("START_SESSION_MESSAGE", dpm.getStartUserSessionMessage(receiver) ?: "")
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
		"GET_PASSWORD_COMPLEXITY" -> context.reply("PASSWORD_COMPLEXITY", dpm.getPasswordComplexity())
		"HAS_LOCKDOWN_ADMIN_CONFIGURED_NETWORKS" -> context.reply("LOCKDOWN_ADMIN_CONFIGURED_NETWORKS", dpm.hasLockdownAdminConfiguredNetworks(receiver))
		"GET_PASSWORD_EXPIRATION" -> context.reply("PASSWORD_EXPIRATION", dpm.getPasswordExpiration(receiver))
		"GET_PASSWORD_HISTORY_LENGTH" -> context.reply("PASSWORD_HISTORY_LENGTH", dpm.getPasswordHistoryLength(receiver))
		"GET_PASSWORD_MAXIMUM_LENGTH" -> context.reply("PASSWORD_MAXIMUM_LENGTH", dpm.getPasswordMaximumLength(quality))
		"GET_PASSWORD_MINIMUM_LEMGTH" -> context.reply("PASSWORD_MINIMUM_LENGTH", dpm.getPasswordMinimumLength(receiver))
		"GET_PASSWORD_MINIMUM_LETTERS" -> context.reply("PASSWORD_MINIMUM_LETTERS", dpm.getPasswordMinimumLetters(receiver))
		"GET_PASSWORD_MINIMUM_LOWERCASE" -> context.reply("PASSWORD_MINIMUM_LOWERCASE", dpm.getPasswordMinimumLowerCase(receiver))
		"GET_PASSWORD_MINIMUM_NON_LETTER" -> context.reply("PASSWORD_MINIMUM_NON_LETTER", dpm.getPasswordMinimumNonLetter(receiver))
		"GET_PASSWORD_MINIMUM_NUMERIC" -> context.reply("PASSWORD_MINIMUM_NUMERIC", dpm.getPasswordMinimumNumeric(receiver))
		"GET_PASSWORD_MINIMUM_SYMBOLS" -> context.reply("PASSWORD_MINIMUM_SYMBOLS", dpm.getPasswordMinimumSymbols(receiver))
		"GET_PASSWORD_MINIMUM_UPPERCASE" -> context.reply("PASSWORD_MINIMUM_UPPERCASE", dpm.getPasswordMinimumUpperCase(receiver))
		"GET_PASSWORD_QUALITY" -> context.reply("PASSWORD_QUALITY", dpm.getPasswordQuality(receiver))
		"GET_PENDING_SYSTEM_UPDATE" -> context.reply("PENDING_SYSTEM_UPDATE", dpm.getPendingSystemUpdate(receiver).toString())
		"GET_PERMISSION_GRANT_STATE" -> context.reply("PERMISSION_GRANT_STATE", dpm.getPermissionGrantState(receiver, app!!, permission!!))
		"GET_PERMISSION_POLICY" -> context.reply("PERMISSION_POLICY", dpm.getPermissionPolicy(receiver))
		"GET_PERMITTED_ACCESSIBILITY_SERVICES" -> context.reply("PERMITTED_ACCESSIBILITY_SERVICES", dpm.getPermittedAccessibilityServices(receiver) ?: mutableListOf<String>())
		"GET_PERMITTED_CROSS_PROFILE_NOTIFICATION_LISTENERS" -> context.reply("PERMITTED_CROSS_PROFILE_NOTIFICATION_LISTENERS", dpm.getPermittedCrossProfileNotificationListeners(receiver) ?: mutableListOf<String>())
		"GET_PERMITTED_IME" -> context.reply("PERMITTED_IME", dpm.getPermittedInputMethods(receiver) ?: mutableListOf<String>())
		"GET_PERSONAL_APPS_SUSPENDED_REASONS" -> context.reply("PERSONAL_APPS_SUSPENDED_REASONS", dpm.getPersonalAppsSuspendedReasons(receiver))
		"GET_REQUIRED_PASSWORD_COMPLEXITY" -> context.reply("REQUIRED_PASSWORD_COMPLEXITY", dpm.getRequiredPasswordComplexity())
		"GET_REQUIRED_STRONG_AUTH_TIMEOUT" -> context.reply("REQUIRED_STRONG_AUTH_TIMEOUT", dpm.getRequiredStrongAuthTimeout(receiver))
		"GET_SCRCAP_STATE" -> context.reply("SCRCAP_STATE", !dpm.getScreenCaptureDisabled(receiver))
		"GET_STORAGE_ENCRYPTION_STATUS" -> context.reply("STORAGE_ENCRYPTION_STATUS", dpm.getStorageEncryptionStatus())
		"GET_USER_CONTROL_DISABLED_PACKAGES" -> context.reply("USER_CONTROL_DISABLED_PACKAGES", dpm.getUserControlDisabledPackages(receiver) ?: mutableListOf<String>())
		"GET_WIFI_MAC_ADDRESS" -> context.reply("WIFI_MAC", dpm.getWifiMacAddress(receiver) ?: "02:00:00:00:00:00")
		"IS_PASSWORD_SUFFICIENT" -> context.reply("PASSWORD_SUFFICIENT", dpm.isActivePasswordSufficient())
		"IS_AFFILIATED" -> context.reply("AFFILIATED", dpm.isAffiliatedUser())
		"IS_ALWAYS_ON_VPN_LOCKDOWN" -> context.reply("ALWAYS_ON_VPN", dpm.isAlwaysOnVpnLockdownEnabled(receiver))
		"IS_APP_HIDDEN" -> context.reply("PACKAGE_${app!!}_HIDDEN_STATE", dpm.isApplicationHidden(receiver, app!!))
		"IS_BACKUP_SERVICE_ACTIVE" -> context.reply("BACKUP_SERVICE_STATUS", dpm.isBackupServiceEnabled(receiver))
		"IS_COMMON_CRITERIA" -> context.reply("COMMON_CRITERIA_STATE", dpm.isCommonCriteriaModeEnabled(receiver))
		"IS_FINANCED" -> context.reply("FINANCED_DEVICE", dpm.isDeviceFinanced())
		"IS_DEVICE_ID_ATTESTATION_SUPPORTED" -> context.reply("DEVICE_ID_ATTESTATION_STATE", dpm.isDeviceIdAttestationSupported())
		"IS_EPHEMERAL" -> context.reply("EPHEMERAL_STATE", dpm.isEphemeralUser(receiver))
		"CAN_LOGOUT" -> context.reply("LOGOUT_STATE", dpm.isLogoutEnabled())
		"IS_MANAGED" -> context.reply("MANAGED_STATE", dpm.isManagedProfile(receiver))
		"IS_MASTER_VOLUME_MUTED" -> context.reply("MASTER_VOLUME_MUTED_STATE", dpm.isMasterVolumeMuted(receiver))
		//"IS_MTE_ENFORCED" -> context.reply("MTE_ENFORCED", dpm.isMtePolicyEnforced())
		"IS_NETWORK_LOGGING" -> context.reply("NETWORK_LOGGING_STATE", dpm.isNetworkLoggingEnabled(receiver))
		"IS_OVERRIDING_APNS" -> context.reply("OVERRIDE_APN_STATE", dpm.isOverrideApnEnabled(receiver))
		"IS_APP_SUSPENDED" -> context.reply("PACKAGE_${app!!}_SUSPENDED_STATE", dpm.isPackageSuspended(receiver, app!!))
		"IS_PREFERENTIAL_NETWORK_SERVICE" -> context.reply("PREFERENTIAL_NETWORK_SERVICE_STATE", dpm.isPreferentialNetworkServiceEnabled())
		"IS_RESET_PASSWORD_TOKEN_ACTIVE" -> context.reply("RESET_PASSWORD_TOKEN_ACTIVE_STATE", dpm.isResetPasswordTokenActive(receiver))
		"IS_SECURITY_LOGGING" -> context.reply("SECURITY_LOGGING_STATE", dpm.isSecurityLoggingEnabled(receiver))
		"ASK_STATUS_BAR_STATE" -> context.reply("STATUS_BAR_STATE", dpm.isStatusBarDisabled())
		"IS_UNINSTALL_BLOCKED" -> context.reply("PACKAGE_${app!!}_UNINSTALL_BLOCK_STATE", dpm.isUninstallBlocked(receiver, app!!))
		"IS_UNIQUE_DEVICE_ATTESTATION_SUPPORTED" -> context.reply("UNIQUE_DEVIC3_ATTESTATION_STATE", dpm.isUniqueDeviceAttestationSupported())
		"IS_USING_UNIFIED_PASSWORD" -> context.reply("UNIFIED_PASSWORD_STATE", dpm.isUsingUnifiedPassword(receiver))
		"EMERGENCY_TRANSFER_DHIZUKU" -> {
		    val newAdmin = ComponentName("com.rosan.dhizuku", "com.rosan.dhizuku.server.DhizukuDAReceiver")
		    dpm.transferOwnership(receiver, newAdmin, null)
		    true
		}
		"APP_INSTALL" -> {
		    val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
		    val sessionId = pm?.createSession(params)!!
		    val apk = File(apkPath)
		    pm?.openSession(sessionId).use { session ->
			if (SP.dhizuku) wrapSession(session!!)
			FileInputStream(apk).use { input ->
			    session?.openWrite("install", 0, apk.length()).use { output ->
				input.copyTo(output!!)
				session?.fsync(output!!)
			    }
		        }
			val callbackIntent = Intent(PACKAGE_STATUS)
			val piFlags = if (Build.VERSION.SDK_INT >= 34) PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT else PendingIntent.FLAG_MUTABLE
			val pi = PendingIntent.getBroadcast(
			    context,
			    sessionId,
			    callbackIntent,
			    piFlags
		        ).intentSender
			session?.commit(pi)
			true
		    }
		}
		"GET_USER_RESTRICTIONS" -> {
		    val restrictions = dpm.getUserRestrictions(receiver)
		    val result = restrictions.keySet().filter { restrictions.getBoolean(it, false) }.joinToString("|=|")
		    context.reply("USER_RESTRICTIONS", result)
		}
		"GET_FAN_SPEEDS" -> {
		    val fanSpeeds = hwm?.getFanSpeeds()
		    context.reply("FAN_SPEEDS", fanSpeeds!!.joinToString("|=|"))
		}
		"GET_CPU_USAGES" -> {
		    val cpuUsages = hwm?.getCpuUsages()
		    context.reply("CPU_USAGES", cpuUsages!!.joinToString("|=|") { "${it.getActive()}:${it.getTotal()}" })
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
	context.reply("LOG_$TAG", log, true)
	context.reply("NULL", "")
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
