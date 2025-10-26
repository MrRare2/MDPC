package dev.mr2.dpc

import android.app.PendingIntent
import android.app.admin.IDevicePolicyManager
import android.app.admin.DevicePolicyManager
import android.app.admin.FactoryResetProtectionPolicy
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.IPackageInstaller
import android.content.pm.IPackageInstallerSession
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.Session
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.HardwarePropertiesManager
import android.os.UserManager
import android.util.Log
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuBinderWrapper
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import org.json.JSONObject
import org.json.JSONArray

fun realHandler(context: Context, req: JSONObject): JSONObject {
    val res = JSONObject()
    var err: Any? = JSONObject.NULL
    var ret: Any? = JSONObject.NULL
    val act = req.optString("action")

    val dpm = Privilege.DPM
    val receiver = Privilege.DAR
    val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager?
    val hwm = context.getSystemService(Context.HARDWARE_PROPERTIES_SERVICE) as HardwarePropertiesManager?
    val um = context.getSystemService(Context.USER_SERVICE) as UserManager?
    val pm = context.packageManager.packageInstaller
    val repo = (context.applicationContext as MyApplication).myRepo
    try {
        ret = when (act) {
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
            "SYSTEM_SET_TIME" -> dpm.setTime(receiver, getArg<Long>("time_ts", req))
            "SYSTEM_SET_TZ" -> dpm.setTimeZone(receiver, getArg<String>("time_tz", req))
            "SYSTEM_SET_AUTO_TIME_POLICY" -> dpm.setAutoTimePolicy(getArg<Int>("flags", req))
            "SYSTEM_SET_AUTO_TZ_POLICY" -> dpm.setAutoTimeZonePolicy(getArg<Int>("flags", req))
            "SYSTEM_SET_CONTENT_PROTECTION_POLICY" -> dpm.setContentProtectionPolicy(receiver, getArg<Int>("flags", req))
            "SYSTEM_SET_PERMISSION_POLICY" -> dpm.setPermissionPolicy(receiver, getArg<Int>("flags", req))
            "SYSTEM_SET_MTE_POLICY" -> dpm.setMtePolicy(getArg<Int>("flags", req))
            "SYSTEM_SET_NEARBY_APP_STREAMING_POLICY" -> dpm.setNearbyAppStreamingPolicy(getArg<Int>("flags", req))
            "SYSTEM_DISABLE_ACCOUNTS_MANAGEMENT" -> dpm.setAccountManagementDisabled(receiver, getArg<String>("account", req), true)
            "SYSTEM_ENABLE_ACCOUNTS_MANAGEMENT" -> dpm.setAccountManagementDisabled(receiver, getArg<String>("account", req), false)
            "SYSTEM_DISABLE_FRP_POLICY" -> dpm.setFactoryResetProtectionPolicy(receiver, FactoryResetProtectionPolicy.Builder()
                .setFactoryResetProtectionEnabled(false)
                .setFactoryResetProtectionAccounts(listOfNotNull(getArg<String>("account", req)))
                .build())
            "SYSTEM_ENABLE_FRP_POLICY" -> dpm.setFactoryResetProtectionPolicy(receiver, FactoryResetProtectionPolicy.Builder()
                .setFactoryResetProtectionEnabled(true)
                .setFactoryResetProtectionAccounts(listOfNotNull(getArg<String>("account", req)))
                .build())
            "SYSTEM_SET_ORGANIZATION_NAME" -> dpm.setOrganizationName(receiver, getArg<String>("text", req))
            "SYSTEM_SET_ORGANIZATION_ID" -> dpm.setOrganizationId(getArg<String>("id", req))
            "APP_SET_PERMISSION_DEFAULT" -> dpm.setPermissionGrantState(receiver, getArg<String>("package", req), getArg<String>("permission", req), DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT)
            "APP_SET_PERMISSION_GRANTED" -> dpm.setPermissionGrantState(receiver, getArg<String>("package", req), getArg<String>("permission", req), DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED)
            "APP_SET_PERMISSION_DENIED" -> dpm.setPermissionGrantState(receiver, getArg<String>("package", req), getArg<String>("permission", req), DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED)
            "APP_HIDE" -> dpm.setApplicationHidden(receiver, getArg<String>("package", req), true)
            "APP_UNHIDE" -> dpm.setApplicationHidden(receiver, getArg<String>("package", req), false)
            "APP_SUSPEND" -> dpm.setPackagesSuspended(receiver, arrayOf(getArg<String>("package", req)), true)
            "APP_UNSUSPEND" -> dpm.setPackagesSuspended(receiver, arrayOf(getArg<String>("package", req)), false).isEmpty()
            "APP_ADD_UNINSTALL_BLOCK" -> dpm.setUninstallBlocked(receiver, getArg<String>("package", req), true)
            "APP_REMOVE_UNINSTALL_BLOCK" -> dpm.setUninstallBlocked(receiver, getArg<String>("package", req), false)
            "APP_UNINSTALL" -> uninstallApp(context, getArg<String>("app", req), pm)
            "SYSTEM_REBOOT" -> dpm.reboot(receiver)
            "USER_SET_LOCK_SCREEN_INFO" -> dpm.setDeviceOwnerLockScreenInfo(receiver, getArg<String>("text", req))
            "USER_SET_SHORT_SUPPORT_MESSAGE" -> dpm.setShortSupportMessage(receiver, getArg<String>("text", req))
            "USER_SET_LONG_SUPPORT_MESSAGE" -> dpm.setLongSupportMessage(receiver, getArg<String>("text", req))
            "USER_SET_START_USER_SESSION_MESSAGE" -> dpm.setStartUserSessionMessage(receiver, getArg<String>("text", req))
            "USER_SET_END_USER_SESSION_MESSAGE" -> dpm.setEndUserSessionMessage(receiver, getArg<String>("text", req))
            "USER_ADD_RESTRICTION" -> dpm.addUserRestriction(receiver, getArg<String>("restriction", req))
            "USER_REMOVE_RESTRICTION" -> dpm.clearUserRestriction(receiver, getArg<String>("restriction", req))
            "SYSTEM_ENABLE_WIFI" -> wm?.setWifiEnabled(true)
            "SYSTEM_DISABLE_WIFI" -> wm?.setWifiEnabled(false)
            "SYSTEM_WIFI_RECONNECT" -> wm?.reconnect()
            "SYSTEM_WIFI_DISCONNECT" -> wm?.disconnect()
            "SYSTEM_WIFI_DISABLE_NETWORK" -> wm?.disableNetwork(getArg<Int>("wifiNetId", req))
            "SYSTEM_WIFI_ENABLE_NETWORK" -> wm?.enableNetwork(getArg<Int>("wifiNetId", req), getArg<Boolean>("wifiEnabled", req))
            "SYSTEM_REMOVE_WIFI_NETWORK" -> wm?.removeNetwork(getArg<Int>("wifiNetId", req))
            "SYSTEM_ADD_WIFI_NETWORK" -> addWifiNetwork(wm, getArg<String>("ssid", req), req.optString("sharedKey"), req.optString("bssid"), req.optBoolean("wifiHidden", false), req.optBoolean("wifiEnabled", true))
            "GET_CPU_TEMPERATURES" -> JSONArray(hwm?.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_CPU, req.optInt("flags")))
            "GET_GPU_TEMPERATURES" -> JSONArray(hwm?.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_GPU, req.optInt("flags")))
            "GET_BATTERY_TEMPERATURES" -> JSONArray(hwm?.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_BATTERY, req.optInt("flags")))
            "GET_SKIN_TEMPERATURES" -> JSONArray(hwm?.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_SKIN, req.optInt("flags")))
            "GET_ORGANIZATION_NAME" -> {
                try { dpm.getOrganizationName(receiver) }
                catch (_: Exception) {
                    val method = DevicePolicyManager::class.java.getDeclaredMethod("getDeviceOwnerOrganizationName")
                    method.isAccessible = true
                    (method.invoke(dpm) as CharSequence)
                }
            }
            "GET_SHORT_SUPPORT_MESSAGE" -> dpm.getShortSupportMessage(receiver)
            "GET_LONG_SUPPORT_MESSAGE" -> dpm.getLongSupportMessage(receiver)
            "GET_LOCK_SCREEN_INFO_MESSAGE" -> dpm.getDeviceOwnerLockScreenInfo()
            "GET_START_SESSION_MESSAGE" -> dpm.getStartUserSessionMessage(receiver)
            "GET_END_SESSION_MESSAGE" -> dpm.getEndUserSessionMessage(receiver)
            "GET_DEVICE_OWNER_PACKAGE" -> context.packageName
            "GET_DEVICE_OWNER_COMPONENT" -> receiver.flattenToString()
            "GET_AUTO_TIME_STATE" -> dpm.getAutoTimeEnabled(receiver)
            "GET_AUTO_TIME_POLICY" -> dpm.getAutoTimePolicy()
            "GET_AUTO_TIME_ZONE_STATE" -> dpm.getAutoTimeZoneEnabled(receiver)
            "GET_AUTO_TIME_ZONE_POLICY" -> dpm.getAutoTimeZonePolicy()
            "GET_BLUETOOTH_CONTACT_SHARING_STATE" -> !dpm.getBluetoothContactSharingDisabled(receiver)
            "GET_CAMERA_STATE" -> !dpm.getCameraDisabled(receiver)
            "GET_CONTENT_PROTECTION_POLICY" -> dpm.getContentProtectionPolicy(receiver)
            "GET_FAILED_PASSWORD_ATTEMPTS" -> dpm.getCurrentFailedPasswordAttempts()
            "GET_DPM_ROLE_HOLDER_PACKAGE" -> dpm.getDevicePolicyManagementRoleHolderPackage()
            "GET_ENROLLMENT_SPECIFIC_ID" -> dpm.getEnrollmentSpecificId()
            "GET_GLOBAL_PRIVATE_DNS" -> dpm.getGlobalPrivateDnsHost(receiver)
            "GET_GLOBAL_PRIVATE_DNS_MODE" -> dpm.getGlobalPrivateDnsMode(receiver)
            "GET_KEEP_UNINSTALL_PACKAGES" -> JSONArray(dpm.getKeepUninstalledPackages(receiver) ?: emptyList<String>())
            "GET_KEYGUARD_DISABLED_FEATURES" -> dpm.getKeyguardDisabledFeatures(receiver)
            "GET_LOCK_TASK_FEATURES" -> dpm.getLockTaskFeatures(receiver)
            "GET_LOCK_TASK_PACKAGES" -> JSONArray(dpm.getLockTaskPackages(receiver))
            "GET_MAXIMUM_FAILED_PASSWORD_ATTEMPTS_FOR_WIPE" -> dpm.getMaximumFailedPasswordsForWipe(receiver)
            "GET_MAXIMUM_TIME_TO_LOCK" -> dpm.getMaximumTimeToLock(receiver)
            "GET_METERED_DATA_DISABLED_PACKAGES" -> JSONArray(dpm.getMeteredDataDisabledPackages(receiver))
            "GET_MINIMUM_WIFI_SECURITY_LEVEL" -> dpm.getMinimumRequiredWifiSecurityLevel()
            "GET_MTE_POLICY" -> dpm.getMtePolicy()
            "GET_PASSWORD_COMPLEXITY" -> dpm.getPasswordComplexity()
            "HAS_LOCKDOWN_ADMIN_CONFIGURED_NETWORKS" -> dpm.hasLockdownAdminConfiguredNetworks(receiver)
            "GET_PASSWORD_EXPIRATION" -> dpm.getPasswordExpiration(receiver)
            "GET_PASSWORD_HISTORY_LENGTH" -> dpm.getPasswordHistoryLength(receiver)
            "GET_PASSWORD_MAXIMUM_LENGTH" -> dpm.getPasswordMaximumLength(getArg<Int>("quality", req))
            "GET_PASSWORD_MINIMUM_LENGTH" -> dpm.getPasswordMinimumLength(receiver)
            "GET_PASSWORD_MINIMUM_LETTERS" -> dpm.getPasswordMinimumLetters(receiver)
            "GET_PASSWORD_MINIMUM_LOWERCASE" -> dpm.getPasswordMinimumLowerCase(receiver)
            "GET_PASSWORD_MINIMUM_NON_LETTER" -> dpm.getPasswordMinimumNonLetter(receiver)
            "GET_PASSWORD_MINIMUM_NUMERIC" -> dpm.getPasswordMinimumNumeric(receiver)
            "GET_PASSWORD_MINIMUM_SYMBOLS" -> dpm.getPasswordMinimumSymbols(receiver)
            "GET_PASSWORD_MINIMUM_UPPERCASE" -> dpm.getPasswordMinimumUpperCase(receiver)
            "GET_PASSWORD_QUALITY" -> dpm.getPasswordQuality(receiver)
            "GET_PENDING_SYSTEM_UPDATE" -> dpm.getPendingSystemUpdate(receiver).toString()
            "GET_PERMISSION_GRANT_STATE" -> dpm.getPermissionGrantState(receiver, getArg<String>("app", req), getArg<String>("permission", req))
            "GET_PERMISSION_POLICY" -> dpm.getPermissionPolicy(receiver)
            "GET_PERMITTED_ACCESSIBILITY_SERVICES" -> JSONArray(dpm.getPermittedAccessibilityServices(receiver) ?: emptyList<String>())
            "GET_PERMITTED_CROSS_PROFILE_NOTIFICATION_LISTENERS" -> JSONArray(dpm.getPermittedCrossProfileNotificationListeners(receiver) ?: emptyList<String>())
            "GET_PERMITTED_IME" -> JSONArray(dpm.getPermittedInputMethods(receiver) ?: emptyList<String>())
            "GET_PERSONAL_APPS_SUSPENDED_REASONS" -> dpm.getPersonalAppsSuspendedReasons(receiver)
            "GET_REQUIRED_PASSWORD_COMPLEXITY" -> dpm.getRequiredPasswordComplexity()
            "GET_REQUIRED_STRONG_AUTH_TIMEOUT" -> dpm.getRequiredStrongAuthTimeout(receiver)
            "GET_SCRCAP_STATE" -> !dpm.getScreenCaptureDisabled(receiver)
            "GET_STORAGE_ENCRYPTION_STATUS" -> dpm.getStorageEncryptionStatus()
            "GET_USER_CONTROL_DISABLED_PACKAGES" -> JSONArray(dpm.getUserControlDisabledPackages(receiver) ?: emptyList<String>())
            "GET_WIFI_MAC_ADDRESS" -> dpm.getWifiMacAddress(receiver)
            "IS_PASSWORD_SUFFICIENT" -> dpm.isActivePasswordSufficient()
            "IS_AFFILIATED" -> dpm.isAffiliatedUser()
            "IS_ALWAYS_ON_VPN_LOCKDOWN" -> dpm.isAlwaysOnVpnLockdownEnabled(receiver)
            "IS_APP_HIDDEN" -> dpm.isApplicationHidden(receiver, getArg<String>("app", req))
            "IS_BACKUP_SERVICE_ACTIVE" -> dpm.isBackupServiceEnabled(receiver)
            "IS_COMMON_CRITERIA" -> dpm.isCommonCriteriaModeEnabled(receiver)
            "IS_FINANCED" -> dpm.isDeviceFinanced()
            "IS_DEVICE_ID_ATTESTATION_SUPPORTED" -> dpm.isDeviceIdAttestationSupported()
            "IS_EPHEMERAL" -> dpm.isEphemeralUser(receiver)
            "CAN_LOGOUT" -> dpm.isLogoutEnabled()
            "IS_MANAGED" -> dpm.isManagedProfile(receiver)
            "IS_MASTER_VOLUME_MUTED" -> dpm.isMasterVolumeMuted(receiver)
            "IS_NETWORK_LOGGING" -> dpm.isNetworkLoggingEnabled(receiver)
            "IS_OVERRIDING_APNS" -> dpm.isOverrideApnEnabled(receiver)
            "IS_APP_SUSPENDED" -> dpm.isPackageSuspended(receiver, getArg<String>("app", req))
            "IS_PREFERENTIAL_NETWORK_SERVICE" -> dpm.isPreferentialNetworkServiceEnabled()
            "IS_RESET_PASSWORD_TOKEN_ACTIVE" -> dpm.isResetPasswordTokenActive(receiver)
            "IS_SECURITY_LOGGING" -> dpm.isSecurityLoggingEnabled(receiver)
            "GET_STATUS_BAR_STATE" -> dpm.isStatusBarDisabled()
            "IS_UNINSTALL_BLOCKED" -> dpm.isUninstallBlocked(receiver, getArg<String>("app", req))
            "IS_UNIQUE_DEVICE_ATTESTATION_SUPPORTED" -> dpm.isUniqueDeviceAttestationSupported()
            "IS_USING_UNIFIED_PASSWORD" -> dpm.isUsingUnifiedPassword(receiver)
            "EMERGENCY_TRANSFER_DHIZUKU" -> dpm.transferOwnership(receiver, ComponentName("com.rosan.dhizuku", "com.rosan.dhizuku.server.DhizukuDAReceiver"), null)
            "APP_INSTALL" -> installApp(context, getArg<String>("apkPath", req), pm)
            "GET_USER_RESTRICTIONS" -> JSONArray(dpm.getUserRestrictions(receiver).keySet().filter { dpm.getUserRestrictions(receiver).getBoolean(it, false) })
            "GET_FAN_SPEEDS" -> JSONArray(hwm?.getFanSpeeds())
            "GET_CPU_USAGES" -> JSONArray(
                hwm?.getCpuUsages()?.map {
                    JSONObject().apply {
                        put("active", it.getActive())
                        put("total", it.getTotal())
                    }
                }
            )
            "SYSTEM_SET_GLOBAL_PRIVATE_DNS" -> {
                val mode = getArg<Int>("mode", req)
                var host: String? = null
                if (mode == DevicePolicyManager.PRIVATE_DNS_MODE_PROVIDER_HOSTNAME) host = getArg<String>("host", req)
                val field = DevicePolicyManager::class.java.getDeclaredField("mService")
                field.isAccessible = true
                val newDpm = field.get(dpm) as IDevicePolicyManager
                newDpm.setGlobalPrivateDns(receiver, mode, host)
            }
            "SYSTEM_START_SECURITY_LOGGING" -> dpm.setSecurityLoggingEnabled(receiver, true)
            "SYSTEM_STOP_SECURITY_LOGGING" -> dpm.setSecurityLoggingEnabled(receiver, false)
            "GET_SECURITY_LOG_COUNT" -> repo.getSecurityLogsCount().toInt()
            "NETWORK_START_LOGGING" -> dpm.setNetworkLoggingEnabled(receiver, true)
            "NETWORK_STOP_LOGGING" -> dpm.setNetworkLoggingEnabled(receiver, false)
            "GET_NETWORK_LOG_COUNT" -> repo.getNetworkLogsCount().toInt()
            "GET_NETWORK_LOGS" -> {
                val out = ByteArrayOutputStream()
                repo.exportNetworkLogs(out)
                JSONArray(out.toString())
            }
            "GET_SECURITY_LOGS" -> {
                val out = ByteArrayOutputStream()
                repo.exportSecurityLogs(out)
                JSONArray(out.toString())
            }
            "CLEAR_SECURITY_LOGS" -> repo.deleteSecurityLogs()
            "CLEAR_NETWORK_LOGS" -> repo.deleteNetworkLogs()
            "GET_AFFILIATION_IDS" -> JSONArray(dpm.getAffiliationIds(receiver))
            "GET_ALWAYS_ON_VPN_PACKAGE" -> dpm.getAlwaysOnVpnPackage(receiver)
            "GET_ALWAYS_ON_VPN_LOCKDOWN_WHITELIST" -> JSONArray(dpm.getAlwaysOnVpnLockdownWhitelist(receiver))
            "GET_SECONDARY_USERS" -> {
                JSONArray(
                    dpm.getSecondaryUsers(receiver).map { um?.getSerialNumberForUser(it) }
                )
            }
            "GET_WIFI_SSID_POLICY" -> {
                JSONObject().apply {
                    val policy = dpm.getWifiSsidPolicy()
                    put("policy", policy?.getPolicyType() ?: JSONObject.NULL)
                    put("ssids", policy?.getSsids()?.let { JSONArray(it.map { it.toString() }) } ?: JSONObject.NULL)
                }
            }
            else -> throw IllegalArgumentException("invalid action '$act'")
        }
        ret = if (ret == null || ret is Unit) JSONObject.NULL else ret
    } catch (t: Throwable) {
        err = JSONArray(listOf(t::class.java.name, t.message, Log.getStackTraceString(t)))
    }
    res.put("result", ret)
    res.put("error", err)
    return res
}

private inline fun <reified T> getArg(key: String, req: JSONObject): T = req.opt(key) as? T ?: throw IllegalArgumentException("$key is required with ${T::class.simpleName} type")

private fun addWifiNetwork(wm: WifiManager?, ssid: String, sharedKey: String?, bssid: String?, hidden: Boolean, enabled: Boolean): Int {
    val wc = WifiConfiguration().apply {
        SSID = ssid.replace("\"", "\\\"")
        if (!sharedKey.isNullOrEmpty()) preSharedKey = sharedKey.replace("\"", "\\\"")
        if (!bssid.isNullOrEmpty()) BSSID = bssid
        hiddenSSID = hidden
    }
    val netId = wm?.addNetwork(wc) ?: -1
    wm?.enableNetwork(netId, enabled)
    return netId
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

private fun installApp(context: Context, apkPath: String, pm: PackageInstaller) {
    val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
    val sessionId = pm.createSession(params)
    val apk = File(apkPath)
    pm.openSession(sessionId).use { session ->
        if (SP.dhizuku) wrapSession(session)
        FileInputStream(apk).use { input ->
            session.openWrite("install", 0, apk.length()).use { output ->
                input.copyTo(output)
                session.fsync(output)
            }
        }
        val callbackIntent = Intent("PACKAGE_STATUS").apply { `package` = context.packageName }
        val piFlags = if (Build.VERSION.SDK_INT >= 34) PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT else PendingIntent.FLAG_MUTABLE
        val pi = PendingIntent.getBroadcast(context, sessionId, callbackIntent, piFlags).intentSender
        session.commit(pi)
    }
}

private fun uninstallApp(context: Context, app: String, pm: PackageInstaller?) {
    val intent = Intent("PACKAGE_STATUS").apply { `package` = context.packageName }
    val pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE).intentSender
    pm?.uninstall(app, pi)
}
