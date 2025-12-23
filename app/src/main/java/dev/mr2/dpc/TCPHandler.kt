package dev.mr2.dpc

import android.app.ActivityOptions
import android.app.PendingIntent
import android.app.admin.IDevicePolicyManager
import android.app.admin.DevicePolicyManager
import android.app.admin.FactoryResetProtectionPolicy
import android.app.usage.NetworkStatsManager
import android.app.usage.NetworkStats
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.RestrictionEntry
import android.content.RestrictionsManager
import android.content.pm.IPackageInstaller
import android.content.pm.IPackageInstallerSession
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.Session
import android.net.ConnectivityManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.HardwarePropertiesManager
import android.os.UserManager
import android.util.Base64
import android.util.Log
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuBinderWrapper
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import org.json.JSONArray

private val unsupportedDhizukuError: Exception = IllegalStateException("This function is unsupported in Dhizuku mode")

private fun requires(api: Int) {
    if (Build.VERSION.SDK_INT < api) throw UnsupportedOperationException(
        "This function requires API $api to work, but got API ${Build.VERSION.SDK_INT}"
    )
}

fun realHandler(context: Context, req: JSONObject): JSONObject {
    val res = JSONObject()
    var err: Any? = JSONObject.NULL
    var ret: Any? = JSONObject.NULL
    val act = req.optString("action")

    val dpm = Privilege.DPM
    val receiver = Privilege.DAR
    val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val hwm = context.getSystemService(Context.HARDWARE_PROPERTIES_SERVICE) as HardwarePropertiesManager
    val rm = context.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
    val um = context.getSystemService(Context.USER_SERVICE) as UserManager
    val nsm = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
    val pm = context.packageManager
    val repo = (context.applicationContext as MyApplication).myRepo
    val status = Privilege.status.value
    try {
        ret = when (act) {
            "SYSTEM_DISABLE_CAMERA" -> dpm.setCameraDisabled(receiver, true)
            "SYSTEM_ENABLE_CAMERA" -> dpm.setCameraDisabled(receiver, false)
            "SYSTEM_DISABLE_SCRCAP" -> dpm.setScreenCaptureDisabled(receiver, true)
            "SYSTEM_ENABLE_SCRCAP" -> dpm.setScreenCaptureDisabled(receiver, false)
            "SYSTEM_DISABLE_STATBAR" -> dpm.setStatusBarDisabled(receiver, true)
            "SYSTEM_ENABLE_STATBAR" -> dpm.setStatusBarDisabled(receiver, false)
            "SYSTEM_DISABLE_AUTO_TIME" -> { requires(30); dpm.setAutoTimeEnabled(receiver, false) }
            "SYSTEM_ENABLE_AUTO_TIME" -> { requires(30); dpm.setAutoTimeEnabled(receiver, true) }
            "SYSTEM_DISABLE_AUTO_TZ" -> { requires(30); dpm.setAutoTimeZoneEnabled(receiver, false) }
            "SYSTEM_ENABLE_AUTO_TZ" -> { requires(30); dpm.setAutoTimeZoneEnabled(receiver, true) }
            "SYSTEM_DISABLE_AUTO_TIME_OLD" -> dpm.setAutoTimeRequired(receiver, false)
            "SYSTEM_ENABLE_AUTO_TIME_OLD" -> dpm.setAutoTimeRequired(receiver, true)
            "SYSTEM_MASTER_VOLUME_MUTE" -> dpm.setMasterVolumeMuted(receiver, true)
            "SYSTEM_MASTER_VOLUME_UNMUTE" -> dpm.setMasterVolumeMuted(receiver, false)
            "SYSTEM_DISABLE_BACKUP_SERVICE" -> { requires(26); dpm.setBackupServiceEnabled(receiver, false) }
            "SYSTEM_ENABLE_BACKUP_SERVICE" -> { requires(26); dpm.setBackupServiceEnabled(receiver, true) }
            "SYSTEM_DISABLE_BT_SHARE" -> dpm.setBluetoothContactSharingDisabled(receiver, true)
            "SYSTEM_ENABLE_BT_SHARE" -> dpm.setBluetoothContactSharingDisabled(receiver, false)
            "SYSTEM_DISABLE_COMMON_CRITERIA" -> { requires(30); dpm.setCommonCriteriaModeEnabled(receiver, false) }
            "SYSTEM_ENABLE_COMMON_CRITERIA" -> { requires(30); dpm.setCommonCriteriaModeEnabled(receiver, true) }
            "SYSTEM_DISABLE_USB_SIGNAL" -> { requires(31); dpm.isUsbDataSignalingEnabled = false }
            "SYSTEM_ENABLE_USB_SIGNAL" -> { requires(31); dpm.isUsbDataSignalingEnabled = true }
            "SYSTEM_DISABLE_KEYGUARD" -> dpm.setKeyguardDisabled(receiver, true)
            "SYSTEM_ENABLE_KEYGUARD" -> dpm.setKeyguardDisabled(receiver, false)
            "SYSTEM_LOCK_NOW_EVICT_CREDENTIAL_ENCRYPTION_KEY" -> { requires(26); dpm.lockNow(DevicePolicyManager.FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY) }
            "SYSTEM_LOCK_NOW" -> dpm.lockNow()
            "SYSTEM_SET_TIME" -> { requires(28); dpm.setTime(receiver, getArg<Long>("time_ts", req)) }
            "SYSTEM_SET_TZ" -> { requires(28); dpm.setTimeZone(receiver, getArg<String>("time_tz", req)) }
            "SYSTEM_SET_AUTO_TIME_POLICY" -> { requires(36); dpm.setAutoTimePolicy(getArg<Int>("policy", req)) }
            "SYSTEM_SET_AUTO_TZ_POLICY" -> { requires(36); dpm.setAutoTimeZonePolicy(getArg<Int>("policy", req)) }
            "SYSTEM_SET_CONTENT_PROTECTION_POLICY" -> { requires(35); dpm.setContentProtectionPolicy(receiver, getArg<Int>("policy", req)) }
            "SYSTEM_SET_PERMISSION_POLICY" -> { requires(35); dpm.setPermissionPolicy(receiver, getArg<Int>("policy", req)) }
            "SYSTEM_SET_MTE_POLICY" -> { requires(34); dpm.setMtePolicy(getArg<Int>("policy", req)) }
            "SYSTEM_SET_NEARBY_APP_STREAMING_POLICY" -> { requires(31); dpm.setNearbyAppStreamingPolicy(getArg<Int>("policy", req)) }
            "SYSTEM_SET_NEARBY_NOTIFICATION_STREAMING_POLICY" -> { requires(31); dpm.setNearbyNotificationStreamingPolicy(getArg<Int>("policy", req)) }
            "SYSTEM_DISABLE_ACCOUNTS_MANAGEMENT" -> dpm.setAccountManagementDisabled(receiver, getArg<String>("account", req), true)
            "SYSTEM_ENABLE_ACCOUNTS_MANAGEMENT" -> dpm.setAccountManagementDisabled(receiver, getArg<String>("account", req), false)
            "SYSTEM_DISABLE_FRP_POLICY" -> {
                requires(30)
                dpm.setFactoryResetProtectionPolicy(receiver, FactoryResetProtectionPolicy.Builder()
                    .setFactoryResetProtectionEnabled(false)
                    .setFactoryResetProtectionAccounts(listOfNotNull(getArg<String>("account", req)))
                    .build())
            }
            "SYSTEM_ENABLE_FRP_POLICY" -> {
                requires(30)
                dpm.setFactoryResetProtectionPolicy(receiver, FactoryResetProtectionPolicy.Builder()
                    .setFactoryResetProtectionEnabled(true)
                    .setFactoryResetProtectionAccounts(listOfNotNull(getArg<String>("account", req)))
                    .build())
            }
            "SYSTEM_SET_ORGANIZATION_NAME" -> { requires(24); dpm.setOrganizationName(receiver, getArg<String>("text", req)) }
            "SYSTEM_SET_ORGANIZATION_ID" -> { requires(31); dpm.setOrganizationId(getArg<String>("id", req)) }
            "APP_SET_PERMISSION_DEFAULT" -> dpm.setPermissionGrantState(receiver, getArg<String>("package", req), getArg<String>("permission", req), DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT)
            "APP_SET_PERMISSION_GRANTED" -> dpm.setPermissionGrantState(receiver, getArg<String>("package", req), getArg<String>("permission", req), DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED)
            "APP_SET_PERMISSION_DENIED" -> dpm.setPermissionGrantState(receiver, getArg<String>("package", req), getArg<String>("permission", req), DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED)
            "APP_HIDE" -> dpm.setApplicationHidden(receiver, getArg<String>("package", req), true)
            "APP_UNHIDE" -> dpm.setApplicationHidden(receiver, getArg<String>("package", req), false)
            "APP_SUSPEND" -> { requires(24); dpm.setPackagesSuspended(receiver, arrayOf(getArg<String>("package", req)), true) }
            "APP_UNSUSPEND" -> { requires(24); dpm.setPackagesSuspended(receiver, arrayOf(getArg<String>("package", req)), false) }
            "APP_ADD_UNINSTALL_BLOCK" -> dpm.setUninstallBlocked(receiver, getArg<String>("package", req), true)
            "APP_REMOVE_UNINSTALL_BLOCK" -> dpm.setUninstallBlocked(receiver, getArg<String>("package", req), false)
            "APP_UNINSTALL" -> runBlocking { uninstallApp(getArg<String>("app", req), context) }
            "SYSTEM_REBOOT" -> dpm.reboot(receiver)
            "USER_SET_LOCK_SCREEN_INFO" -> { requires(24); dpm.setDeviceOwnerLockScreenInfo(receiver, getArg<String>("text", req)) }
            "USER_SET_SHORT_SUPPORT_MESSAGE" -> { requires(24); dpm.setShortSupportMessage(receiver, getArg<String>("text", req)) }
            "USER_SET_LONG_SUPPORT_MESSAGE" -> { requires(24); dpm.setLongSupportMessage(receiver, getArg<String>("text", req)) }
            "USER_SET_START_USER_SESSION_MESSAGE" -> { requires(28); dpm.setStartUserSessionMessage(receiver, getArg<String>("text", req)) }
            "USER_SET_END_USER_SESSION_MESSAGE" -> { requires(28); dpm.setEndUserSessionMessage(receiver, getArg<String>("text", req)) }
            "USER_ADD_RESTRICTION" -> dpm.addUserRestriction(receiver, getArg<String>("restriction", req))
            "USER_REMOVE_RESTRICTION" -> dpm.clearUserRestriction(receiver, getArg<String>("restriction", req))
            "SYSTEM_ENABLE_WIFI" -> if (status.dhizuku) throw unsupportedDhizukuError else wm.setWifiEnabled(true)
            "SYSTEM_DISABLE_WIFI" -> if (status.dhizuku) throw unsupportedDhizukuError else wm.setWifiEnabled(false)
            "SYSTEM_WIFI_RECONNECT" -> if (status.dhizuku) throw unsupportedDhizukuError else wm.reconnect()
            "SYSTEM_WIFI_DISCONNECT" -> if (status.dhizuku) throw unsupportedDhizukuError else wm.disconnect()
            "SYSTEM_WIFI_DISABLE_NETWORK" -> if (status.dhizuku) throw unsupportedDhizukuError else wm.disableNetwork(getArg<Int>("wifiNetId", req))
            "SYSTEM_WIFI_ENABLE_NETWORK" -> if (status.dhizuku) throw unsupportedDhizukuError else wm.enableNetwork(getArg<Int>("wifiNetId", req), getArg<Boolean>("wifiEnabled", req))
            "SYSTEM_REMOVE_WIFI_NETWORK" -> if (status.dhizuku) throw unsupportedDhizukuError else wm.removeNetwork(getArg<Int>("wifiNetId", req))
            "SYSTEM_ADD_WIFI_NETWORK" -> if (status.dhizuku) throw unsupportedDhizukuError else addWifiNetwork(wm, getArg<String>("ssid", req), req.optString("sharedKey"), req.optString("bssid"), req.optBoolean("wifiHidden", false), req.optBoolean("wifiEnabled", true))
            "GET_CPU_TEMPERATURES" -> if (status.dhizuku) throw unsupportedDhizukuError else JSONArray(hwm.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_CPU, req.optInt("flags")))
            "GET_GPU_TEMPERATURES" -> if (status.dhizuku) throw unsupportedDhizukuError else JSONArray(hwm.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_GPU, req.optInt("flags")))
            "GET_BATTERY_TEMPERATURES" -> if (status.dhizuku) throw unsupportedDhizukuError else JSONArray(hwm.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_BATTERY, req.optInt("flags")))
            "GET_SKIN_TEMPERATURES" -> if (status.dhizuku) throw unsupportedDhizukuError else JSONArray(hwm.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_SKIN, req.optInt("flags")))
            "GET_ORGANIZATION_NAME" -> {
                requires(24)
                try { dpm.getOrganizationName(receiver) }
                catch (_: Exception) {
                    val method = DevicePolicyManager::class.java.getDeclaredMethod("getDeviceOwnerOrganizationName")
                    method.isAccessible = true
                    (method.invoke(dpm) as CharSequence)
                }
            }
            "GET_SHORT_SUPPORT_MESSAGE" -> { requires(24); dpm.getShortSupportMessage(receiver) }
            "GET_LONG_SUPPORT_MESSAGE" -> { requires(24); dpm.getLongSupportMessage(receiver) }
            "GET_LOCK_SCREEN_INFO_MESSAGE" -> dpm.getDeviceOwnerLockScreenInfo()
            "GET_START_SESSION_MESSAGE" -> dpm.getStartUserSessionMessage(receiver)
            "GET_END_SESSION_MESSAGE" -> dpm.getEndUserSessionMessage(receiver)
            "GET_DEVICE_OWNER_PACKAGE" -> {
                requires(24)
                val method = DevicePolicyManager::class.java.getDeclaredMethod("getDeviceOwnerComponentOnCallingUser")
                method.isAccessible = true
                (method.invoke(dpm) as ComponentName).getPackageName()
            }
            "GET_DEVICE_OWNER_COMPONENT" -> {
                requires(24)
                val method = DevicePolicyManager::class.java.getDeclaredMethod("getDeviceOwnerComponentOnCallingUser")
                method.isAccessible = true
                (method.invoke(dpm) as ComponentName).flattenToString()
            }
            "GET_AUTO_TIME_STATE" -> { requires(30); dpm.getAutoTimeEnabled(receiver) }
            "GET_AUTO_TIME_POLICY" -> { requires(36); dpm.getAutoTimePolicy() }
            "GET_AUTO_TIME_STATE_OLD" -> dpm.getAutoTimeRequired()
            "GET_AUTO_TIME_ZONE_STATE" -> dpm.getAutoTimeZoneEnabled(receiver)
            "GET_AUTO_TIME_ZONE_POLICY" -> { requires(36); dpm.getAutoTimeZonePolicy() }
            "GET_BLUETOOTH_CONTACT_SHARING_STATE" -> dpm.getBluetoothContactSharingDisabled(receiver)
            "GET_CAMERA_STATE" -> dpm.getCameraDisabled(receiver)
            "GET_CONTENT_PROTECTION_POLICY" -> { requires(35); dpm.getContentProtectionPolicy(receiver) }
            "GET_FAILED_PASSWORD_ATTEMPTS" -> dpm.getCurrentFailedPasswordAttempts()
            "GET_DPM_ROLE_HOLDER_PACKAGE" -> { requires(33); dpm.getDevicePolicyManagementRoleHolderPackage() }
            "GET_ENROLLMENT_SPECIFIC_ID" -> { requires(31); dpm.getEnrollmentSpecificId() }
            "GET_GLOBAL_PRIVATE_DNS" -> { requires(29); dpm.getGlobalPrivateDnsHost(receiver) }
            "GET_GLOBAL_PRIVATE_DNS_MODE" -> { requires(29); dpm.getGlobalPrivateDnsMode(receiver) }
            "GET_KEEP_UNINSTALL_PACKAGES" -> { requires(28); dpm.getKeepUninstalledPackages(receiver) }
            "GET_KEYGUARD_DISABLED_FEATURES" -> dpm.getKeyguardDisabledFeatures(receiver)
            "GET_LOCK_TASK_FEATURES" -> { requires(28); dpm.getLockTaskFeatures(receiver) }
            "GET_LOCK_TASK_PACKAGES" -> { requires(28); JSONArray(dpm.getLockTaskPackages(receiver)) }
            "GET_MAXIMUM_FAILED_PASSWORD_ATTEMPTS_FOR_WIPE" -> dpm.getMaximumFailedPasswordsForWipe(receiver)
            "GET_MAXIMUM_TIME_TO_LOCK" -> dpm.getMaximumTimeToLock(receiver)
            "GET_METERED_DATA_DISABLED_PACKAGES" -> JSONArray(dpm.getMeteredDataDisabledPackages(receiver) ?: emptyList<String>())
            "GET_MINIMUM_WIFI_SECURITY_LEVEL" -> { requires(28); dpm.getMinimumRequiredWifiSecurityLevel() }
            "GET_MTE_POLICY" -> { requires(29); dpm.getMtePolicy() }
            "GET_PASSWORD_COMPLEXITY" -> dpm.getPasswordComplexity()
            "HAS_LOCKDOWN_ADMIN_CONFIGURED_NETWORKS" -> { requires(30); dpm.hasLockdownAdminConfiguredNetworks(receiver) }
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
            "GET_PENDING_SYSTEM_UPDATE" -> {
                requires(26)
                val info = dpm.getPendingSystemUpdate(receiver)
                JSONObject().apply {
                    put("received", info?.getReceivedTime() ?: JSONObject.NULL)
                    put("security_patch_state", info?.getSecurityPatchState() ?: JSONObject.NULL)
                }
            }
            "GET_PERMISSION_GRANT_STATE" -> dpm.getPermissionGrantState(receiver, getArg<String>("app", req), getArg<String>("permission", req))
            "GET_PERMISSION_POLICY" -> dpm.getPermissionPolicy(receiver)
            "GET_PERMITTED_ACCESSIBILITY_SERVICES" -> JSONArray(dpm.getPermittedAccessibilityServices(receiver) ?: emptyList<String>())
            "GET_PERMITTED_CROSS_PROFILE_NOTIFICATION_LISTENERS" -> JSONArray(dpm.getPermittedCrossProfileNotificationListeners(receiver) ?: emptyList<String>())
            "GET_PERMITTED_IME" -> JSONArray(dpm.getPermittedInputMethods(receiver) ?: emptyList<String>())
            "GET_PERSONAL_APPS_SUSPENDED_REASONS" -> { requires(30); dpm.getPersonalAppsSuspendedReasons(receiver) }
            "GET_REQUIRED_PASSWORD_COMPLEXITY" -> { requires(31); dpm.getRequiredPasswordComplexity() }
            "GET_REQUIRED_STRONG_AUTH_TIMEOUT" -> { requires(26); dpm.getRequiredStrongAuthTimeout(receiver) }
            "GET_SCRCAP_STATE" -> dpm.getScreenCaptureDisabled(receiver)
            "GET_STORAGE_ENCRYPTION_STATUS" -> dpm.getStorageEncryptionStatus()
            "GET_USER_CONTROL_DISABLED_PACKAGES" -> JSONArray(dpm.getUserControlDisabledPackages(receiver) ?: emptyList<String>())
            "GET_WIFI_MAC_ADDRESS" -> { requires(24); dpm.getWifiMacAddress(receiver) }
            "IS_PASSWORD_SUFFICIENT" -> dpm.isActivePasswordSufficient()
            "IS_AFFILIATED" -> { requires(28); dpm.isAffiliatedUser() }
            "IS_ALWAYS_ON_VPN_LOCKDOWN" -> dpm.isAlwaysOnVpnLockdownEnabled(receiver)
            "IS_APP_HIDDEN" -> dpm.isApplicationHidden(receiver, getArg<String>("app", req))
            "GET_BACKUP_SERVICE_STATE" -> dpm.isBackupServiceEnabled(receiver)
            "GET_COMMON_CRITERIA_STATE" -> dpm.isCommonCriteriaModeEnabled(receiver)
            "GET_FINANCED_STATE" -> dpm.isDeviceFinanced()
            "GET_DEVICE_ID_ATTESTATION_SUPPORTED_STATE" -> dpm.isDeviceIdAttestationSupported()
            "IS_EPHEMERAL" -> dpm.isEphemeralUser(receiver)
            "CAN_LOGOUT" -> dpm.isLogoutEnabled()
            "IS_MANAGED" -> dpm.isManagedProfile(receiver)
            "GET_MASTER_VOLUME_STATE" -> dpm.isMasterVolumeMuted(receiver)
            "IS_NETWORK_LOGGING" -> dpm.isNetworkLoggingEnabled(receiver)
            "IS_OVERRIDING_APNS" -> dpm.isOverrideApnEnabled(receiver)
            "IS_APP_SUSPENDED" -> { requires(24); dpm.isPackageSuspended(receiver, getArg<String>("app", req)) }
            "GET_PREFERENTIAL_NETWORK_SERVICE_STATE" -> { requires(31); dpm.isPreferentialNetworkServiceEnabled() }
            "GET_RESET_PASSWORD_TOKEN_ACTIVE_STATE" -> { requires(26); dpm.isResetPasswordTokenActive(receiver) }
            "IS_SECURITY_LOGGING" -> { requires(24); dpm.isSecurityLoggingEnabled(receiver) }
            "GET_STATUS_BAR_STATE" -> { requires(34); dpm.isStatusBarDisabled() }
            "IS_UNINSTALL_BLOCKED" -> dpm.isUninstallBlocked(receiver, getArg<String>("app", req))
            "IS_UNIQUE_DEVICE_ATTESTATION_SUPPORTED" -> { requires(30); dpm.isUniqueDeviceAttestationSupported() }
            "IS_USING_UNIFIED_PASSWORD" -> { requires(28); dpm.isUsingUnifiedPassword(receiver) }
            "EMERGENCY_TRANSFER_DHIZUKU" -> { requires(28); dpm.transferOwnership(receiver, ComponentName("com.rosan.dhizuku", "com.rosan.dhizuku.server.DhizukuDAReceiver"), null) }
            "APP_INSTALL" -> runBlocking { installApp(getArg<String>("apkPath", req), context) }
            "GET_USER_RESTRICTIONS" -> JSONArray(dpm.getUserRestrictions(receiver).keySet().filter { dpm.getUserRestrictions(receiver).getBoolean(it, false) })
            "GET_FAN_SPEEDS" -> if (status.dhizuku) throw unsupportedDhizukuError else JSONArray(hwm.getFanSpeeds())
            "GET_CPU_USAGES" -> if (status.dhizuku) throw unsupportedDhizukuError else JSONArray(
                hwm.getCpuUsages()?.map {
                    JSONObject().apply {
                        put("active", it.getActive())
                        put("total", it.getTotal())
                    }
                }
            )
            "SYSTEM_SET_GLOBAL_PRIVATE_DNS" -> {
                requires(29)
                val mode = getArg<Int>("mode", req)
                var host: String? = null
                if (mode == DevicePolicyManager.PRIVATE_DNS_MODE_PROVIDER_HOSTNAME) host = getArg<String>("host", req)
                val field = DevicePolicyManager::class.java.getDeclaredField("mService")
                field.isAccessible = true
                val newDpm = field.get(dpm) as IDevicePolicyManager
                newDpm.setGlobalPrivateDns(receiver, mode, host)
            }
            "SYSTEM_START_SECURITY_LOGGING" -> { requires(24); if (status.dhizuku) throw unsupportedDhizukuError else dpm.setSecurityLoggingEnabled(receiver, true) }
            "SYSTEM_STOP_SECURITY_LOGGING" -> { requires(24); if (status.dhizuku) throw unsupportedDhizukuError else dpm.setSecurityLoggingEnabled(receiver, false) }
            "GET_SECURITY_LOG_COUNT" -> { requires(24); if (status.dhizuku) throw unsupportedDhizukuError else repo.getSecurityLogsCount().toInt() }
            "NETWORK_START_LOGGING" -> { requires(26); if (status.dhizuku) throw unsupportedDhizukuError else dpm.setNetworkLoggingEnabled(receiver, true) }
            "NETWORK_STOP_LOGGING" -> { requires(26); if (status.dhizuku) throw unsupportedDhizukuError else dpm.setNetworkLoggingEnabled(receiver, false) }
            "GET_NETWORK_LOG_COUNT" -> { requires(26); if (status.dhizuku) throw unsupportedDhizukuError else repo.getNetworkLogsCount().toInt() }
            "GET_NETWORK_LOGS" -> {
                requires(26)
                if (status.dhizuku) throw unsupportedDhizukuError else {
                    val out = ByteArrayOutputStream()
                    repo.exportNetworkLogs(out)
                    JSONArray(out.toString())
                }
            }
            "GET_SECURITY_LOGS" -> {
                requires(24)
                if (status.dhizuku) throw unsupportedDhizukuError else {
                    val out = ByteArrayOutputStream()
                    repo.exportSecurityLogs(out)
                    JSONArray(out.toString())
                }
            }
            "CLEAR_SECURITY_LOGS" -> { requires(24); if (status.dhizuku) throw unsupportedDhizukuError else repo.deleteSecurityLogs() }
            "CLEAR_NETWORK_LOGS" -> { requires(26); if (status.dhizuku) throw unsupportedDhizukuError else repo.deleteNetworkLogs() }
            "GET_AFFILIATION_IDS" -> { requires(26); JSONArray(dpm.getAffiliationIds(receiver)) }
            "GET_ALWAYS_ON_VPN_PACKAGE" -> { requires(24); dpm.getAlwaysOnVpnPackage(receiver) }
            "GET_ALWAYS_ON_VPN_LOCKDOWN_WHITELIST" -> { requires(29); JSONArray(dpm.getAlwaysOnVpnLockdownWhitelist(receiver)) }
            "GET_SECONDARY_USERS" -> {
                requires(29)
                JSONArray(
                    dpm.getSecondaryUsers(receiver).map { um.getSerialNumberForUser(it) }
                )
            }
            "GET_WIFI_SSID_POLICY" -> {
                requires(33)
                JSONObject().apply {
                    val policy = dpm.getWifiSsidPolicy()
                    put("policy", policy?.getPolicyType() ?: JSONObject.NULL)
                    put("ssids", policy?.getSsids()?.let { JSONArray(it.map { it.toString() }) } ?: JSONObject.NULL)
                }
            }
            "SYSTEM_LOCATION_ENABLE" -> { requires(30); dpm.setLocationEnabled(receiver, true) }
            "SYSTEM_LOCATION_DISABLE" -> { requires(30); dpm.setLocationEnabled(receiver, false) }
            "GET_DELEGATED_PACKAGES" -> { requires(26); JSONArray(dpm.getDelegatePackages(receiver, getArg<String>("scope", req))) }
            "GET_DELEGATED_SCOPES" -> { requires(26); JSONArray(dpm.getDelegatedScopes(receiver, getArg<String>("package", req))) }
            "GET_NEARBY_APP_STREAMING_POLICY" -> { requires(31); dpm.getNearbyAppStreamingPolicy() }
            "GET_NEARBY_NOTIFICATION_STREAMING_POLICY" -> { requires(31); dpm.getNearbyNotificationStreamingPolicy() }
            "SYSTEM_ENABLE_CONFIGURED_NETWORKS_LOCKDOWN" -> { requires(30); dpm.setConfiguredNetworksLockdownState(receiver, true) }
            "SYSTEM_DISABLE_CONFIGURED_NETWORKS_LOCKDOWN" -> { requires(30); dpm.setConfiguredNetworksLockdownState(receiver, false) }
            "APP_RESTRICTIONS_GET" -> getAppRestrictions(getArg<String>("package", req), dpm, receiver, rm)
            "APP_RESTRICTIONS_SET" -> setAppRestrictions(getArg<String>("package", req), getArg<String>("key", req), getArg<Any>("value", req), dpm, receiver)
            "SET_LOCK_TASK_FEATURES" -> { requires(28); dpm.setLockTaskFeatures(receiver, getArg<Int>("flags", req)) }
            "SET_LOCK_TASK_PACKAGES" -> { requires(28); dpm.setLockTaskPackages(receiver, getArgArray<String>("packages", req)) }
            "SYSTEM_LOCK_TASK_STOP" -> {
                requires(28)
                val features = dpm.getLockTaskFeatures(receiver)
                val packages = dpm.getLockTaskPackages(receiver)
                dpm.setLockTaskPackages(receiver, arrayOf())
                dpm.setLockTaskPackages(receiver, packages)
                dpm.setLockTaskFeatures(receiver, features)
            }
            "SYSTEM_LOCK_TASK_START" -> {
                requires(28)
                if (status.dhizuku) throw unsupportedDhizukuError else { // cant launch ltm here, at least in my knowledge, let me know if its possible (this is a foreground service)
                    val packageName: String = getArg("package", req)
                    val activity: String? = optArg("activity", req)
                    val showNotification: Boolean = optArg("show_notification", req) ?: true
                    if (!dpm.isLockTaskPermitted(packageName)) dpm.setLockTaskPackages(receiver, (dpm.getLockTaskPackages(receiver) + packageName).distinct().toTypedArray())
                    if (showNotification) dpm.setLockTaskFeatures(receiver, dpm.getLockTaskFeatures(receiver) or
                        DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS or
                        DevicePolicyManager.LOCK_TASK_FEATURE_HOME
                    )
                    val options = ActivityOptions.makeBasic().setLockTaskEnabled(true)
                    val intent = if (activity?.isNotEmpty() ?: false) Intent().setComponent(ComponentName(packageName, activity!!))
                    else pm.getLaunchIntentForPackage(packageName)
                    if (intent != null) {
                        intent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        )
                        context.startActivity(intent, options.toBundle())
                        if (showNotification) context.startForegroundService(Intent(context, LockTaskService::class.java))
                        true
                    } else false
                }
            }
            "IS_LOCK_TASK_PERMITTED" -> dpm.isLockTaskPermitted(getArg<String>("package", req))
            "SYSTEM_USER_CA_CERTS" -> {
                JSONArray().apply {
                    for (cert in dpm.getInstalledCaCerts(receiver)) put(String(Base64.encode(cert, Base64.NO_WRAP)))
                }
            }
            "SYSTEM_ADD_CA_CERT" -> dpm.installCaCert(receiver, Base64.decode(getArg<String>("cert", req), Base64.DEFAULT))
            "SYSTEM_REMOVE_CA_CERT" -> dpm.uninstallCaCert(receiver, Base64.decode(getArg<String>("cert", req), Base64.DEFAULT))
            "NETWORK_MINIMUM_WIFI_SECURITY_LEVEL" -> { requires(33); dpm.minimumRequiredWifiSecurityLevel }
            "GET_MINIMUM_WIFI_SECURITY_LEVEL" -> { requires(33); dpm.minimumRequiredWifiSecurityLevel = getArg<Int>("level", req) }
            "NETWORK_STATS" -> {
                val target = getArg<Int>("target", req)
                val type = getArg<Int>("type", req)
                val start = getArg<Long>("start", req)
                val end = optArg<Long>("end", req) ?: System.currentTimeMillis()
                when (target) {
                    0 -> readNetworkStatsBucket(nsm.querySummaryForDevice(type, null, start, end))
                    1 -> readNetworkStatsBucket(nsm.querySummaryForUser(type, null, start, end))
                    2 -> readNetworkStats(nsm.queryDetailsForUid(type, null, start, end, getArg<Int>("uid", req)))
                    3 -> readNetworkStats(nsm.queryDetailsForUidTag(type, null, start, end, getArg<Int>("uid", req),getArg<Int>("tag", req)))
                    4 -> readNetworkStats(nsm.queryDetailsForUidTagState(type, null, start, end, getArg<Int>("uid", req),getArg<Int>("tag", req), getArg<Int>("state", req)))
                    else -> throw IllegalArgumentException("invalid target (0 -> device, 1 -> user, 2 -> uid, 3 -> uid tag, 4 -> uid tag state)")
                }
            }
            "DEBUG" -> {
                if (!BuildConfig.DEBUG) throw UnsupportedOperationException("debug mode only")
                JSONObject().apply {
                    put("args", req)
                }
            }
            "VERSION" -> JSONObject().apply {
                put("version_name", BuildConfig.VERSION_NAME)
                put("version_code", BuildConfig.VERSION_CODE)
                put("release_type", BuildConfig.BUILD_TYPE)
                put("package_name", context.packageName)
                put("dhizuku_mode", status.dhizuku)
                put("work_profile", status.work)
                put("device_owner", status.device)
                put("organization", status.org)
                put("profile_mode", status.profile)
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

private inline fun <reified T> getArg(key: String, req: JSONObject): T {
    if (!req.has(key) || req.isNull(key)) throw IllegalArgumentException("'$key' is required with ${T::class.simpleName} type")
    val value = req.get(key)
    return when (T::class) {
        Int::class -> (value as? Number)?.toInt() as? T ?: throw IllegalArgumentException("'$key' cannot be converted to Int")
        Long::class -> (value as? Number)?.toLong() as? T ?: throw IllegalArgumentException("'$key' cannot be converted to Long")
        Double::class -> (value as? Number)?.toDouble() as? T ?: throw IllegalArgumentException("'$key' cannot be converted to Double")
        Float::class -> (value as? Number)?.toFloat() as? T ?: throw IllegalArgumentException("'$key' cannot be converted to Float")
        String::class -> value.toString() as T
        Boolean::class -> (value as? Boolean) as? T ?: throw IllegalArgumentException("'$key' cannot be converted to Boolean")
        else -> value as? T ?: throw IllegalArgumentException("'$key' is required with ${T::class.simpleName} type")
    }
}

@Suppress("UNCHECKED_CAST")
private inline fun <reified T> convertOrThrow(v: Any?, context: String = ""): T {
    when (T::class) {
        Int::class -> if (v is Number) return v.toInt() as T
        Long::class -> if (v is Number) return v.toLong() as T
        Double::class -> if (v is Number) return v.toDouble() as T
        Float::class -> if (v is Number) return v.toFloat() as T
        String::class -> if (v is String) return v as T else return v.toString() as T
        Boolean::class -> if (v is Boolean) return v as T
        JSONObject::class -> if (v is JSONObject) return v as T
        JSONArray::class -> if (v is JSONArray) return v as T
        else -> if (v is T) return v as T
    }
    val got = v?.let { it::class.simpleName } ?: "null"
    val place = if (context.isBlank()) "" else " in $context"
    throw IllegalArgumentException("Key'$place' expected ${T::class.simpleName} but got $got")
}

@Suppress("UNCHECKED_CAST")
private inline fun <reified T> optArg(key: String, req: JSONObject): T? {
    val v = req.opt(key)
    if (v == null || v === JSONObject.NULL) return null
    return convertOrThrow(v, key)
}

@Suppress("UNCHECKED_CAST")
private inline fun <reified T> getArgArray(key: String, req: JSONObject): Array<T> {
    val arr = req.optJSONArray(key) ?: throw IllegalArgumentException("$key is required with Array<${T::class.simpleName}> type")
    return Array(arr.length()) { i ->
        val v = arr.opt(i)
        try {
            convertOrThrow<T>(v, "$key[$i]")
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Element at $i in $key is not ${T::class.simpleName}: ${e.message}")
        }
    }
}

private fun addWifiNetwork(wm: WifiManager?, ssid: String, sharedKey: String?, bssid: String?, hidden: Boolean, enabled: Boolean): Int { // TODO: Add more options
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

private suspend fun installApp(apkPath: String, context: Context): JSONObject = // TODO: more optiosn
    suspendCancellableCoroutine { cont ->
        val pm = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        val sessionId = pm.createSession(params)

        val resumed = AtomicBoolean(false)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val result = JSONObject().apply {
                    val extras = intent.extras
                    extras?.keySet()?.forEach { key ->
                        val value = extras.get(key)
                        when (value) {
                            null -> put(key, JSONObject.NULL)
                            else -> put(key, value.toString())
                        }
                    }

                    val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
                    put(PackageInstaller.EXTRA_STATUS, status)
                    put(PackageInstaller.EXTRA_STATUS_MESSAGE, intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE) ?: "")
                    put("sessionId", sessionId)
                }
                    if (resumed.compareAndSet(false, true)) {
                        cont.resumeWith(Result.success(result))
                    }

                    runCatching { context.unregisterReceiver(this) }
                }
            }

        val action = "dev.mr2.tmp.INSTALL_$sessionId"
        val filter = IntentFilter(action)
        val flags = if (Build.VERSION.SDK_INT >= 33)
            Context.RECEIVER_NOT_EXPORTED else 0
        context.registerReceiver(receiver, filter, flags)

        cont.invokeOnCancellation {
            if (resumed.compareAndSet(false, true)) {
                cont.resumeWith(
                    Result.success(
                        JSONObject().apply {
                            put(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
                            put(PackageInstaller.EXTRA_STATUS_MESSAGE, "")
                            put("sessionId", sessionId)
                        }
                    )
                )
            }
            runCatching { context.unregisterReceiver(receiver) }
            runCatching { pm.openSession(sessionId).abandon() }
        }

        pm.openSession(sessionId).use { session ->
            if (Privilege.status.value.dhizuku) wrapSession(session)

            FileInputStream(File(apkPath)).use { input ->
                session.openWrite("app", 0, File(apkPath).length()).use { output ->
                    input.copyTo(output)
                    session.fsync(output)
                }
            }

            val pendingIntent = Intent(action).apply {
                `package` = context.packageName
            }
            val piFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            val pi = PendingIntent.getBroadcast(context, sessionId, pendingIntent, piFlags)

            session.commit(pi.intentSender)
        }
    }

private suspend fun uninstallApp(packageName: String, context: Context): JSONObject =
    suspendCancellableCoroutine { cont ->
        val pm = context.packageManager.packageInstaller
        val resumed = AtomicBoolean(false)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val result = JSONObject().apply {
                    val extras = intent.extras
                    extras?.keySet()?.forEach { key ->
                        val value = extras.get(key)
                        when (value) {
                            null -> put(key, JSONObject.NULL)
                            else -> put(key, value.toString())
                        }
                    }

                    val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
                    put(PackageInstaller.EXTRA_STATUS, status)
                    put(PackageInstaller.EXTRA_STATUS_MESSAGE, intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE) ?: "")
                    put("packageName", packageName)
                }

                if (resumed.compareAndSet(false, true)) {
                    cont.resumeWith(Result.success(result))
                }

                runCatching { context.unregisterReceiver(this) }
            }
        }

        val action = "dev.mr2.tmp.UNINSTALL"
        val filter = IntentFilter(action)
        val flags = if (Build.VERSION.SDK_INT >= 33)
            Context.RECEIVER_NOT_EXPORTED else 0
        context.registerReceiver(receiver, filter, flags)

        cont.invokeOnCancellation {
            if (resumed.compareAndSet(false, true)) {
                cont.resumeWith(
                    Result.success(
                        JSONObject().apply {
                            put(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
                            put(PackageInstaller.EXTRA_STATUS_MESSAGE, "Cancelled")
                            put("packageName", packageName)
                        }
                    )
                )
            }
            runCatching { context.unregisterReceiver(receiver) }
        }

        val pendingIntent = Intent(action).apply {
            setPackage(context.packageName)
        }
        val piFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        val pi = PendingIntent.getBroadcast(context, packageName.hashCode(), pendingIntent, piFlags)

        pm.uninstall(packageName, pi.intentSender)
    }

private fun getAppRestrictions(packageName: String, dpm: DevicePolicyManager, comp: ComponentName, rm: RestrictionsManager): JSONArray {
    val out = JSONArray()
    val bundle = dpm.getApplicationRestrictions(comp, packageName)
    val restrictions = try { rm.getManifestRestrictions(packageName) ?: emptyList() }
    catch (_: NullPointerException) { emptyList() }
    for (restriction in restrictions) {
        val obj = JSONObject()
        obj.put("title", restriction.getTitle())
        obj.put("description", restriction.getDescription())
        obj.put("key", restriction.key)
        obj.put("value", bundle.get(restriction.key) ?: JSONObject.NULL)
        obj.put("type", restriction.type)
        obj.put("choice_entries", restriction?.getChoiceEntries()?.let { JSONArray(it) } ?: JSONObject.NULL)
        obj.put("choice_values", restriction?.getChoiceValues()?.let { JSONArray(it) } ?: JSONObject.NULL)
        obj.put("int_value", try {
            restriction?.getIntValue() ?: JSONObject.NULL
        } catch (e: NumberFormatException) { JSONObject.NULL })
        obj.put("selected_state", restriction?.getSelectedState() ?: JSONObject.NULL)
        obj.put("selected_string", restriction?.getSelectedString() ?: JSONObject.NULL)
        obj.put("available_strings", restriction?.getAllSelectedStrings()?.let { JSONArray(it) } ?: JSONObject.NULL)
        out.put(obj)
    }
    return out
}

private fun setAppRestrictions(packageName: String, key: String, value: Any, dpm: DevicePolicyManager, comp: ComponentName) {
    val bundle = dpm.getApplicationRestrictions(comp, packageName)
    when (value) {
        is String -> bundle.putString(key, value)
        is Int -> bundle.putInt(key, value)
        is Boolean -> bundle.putBoolean(key, value)
        is JSONArray -> {
            val sa: Array<String> = Array(value.length()) { i -> value.getString(i) }
            bundle.putStringArray(key, sa)
        }
        else -> bundle.remove(key)
    }
    dpm.setApplicationRestrictions(comp, packageName, bundle)
}

private fun readNetworkStats(stats: NetworkStats): JSONArray {
    val list = JSONArray()
    while (stats.hasNextBucket()) {
        val bucket = NetworkStats.Bucket()
        stats.getNextBucket(bucket)
        list.put(readNetworkStatsBucket(bucket))
    }
    stats.close()
    return list
}

private fun readNetworkStatsBucket(bucket: NetworkStats.Bucket): JSONObject = JSONObject().apply {
    put("rxBytes", bucket.rxBytes)
    put("rxPackets", bucket.rxPackets)
    put("txBytes", bucket.txBytes)
    put("txPackets", bucket.txPackets)
    put("uid", bucket.uid)
    put("state", bucket.state)
    put("startTimeStamp", bucket.startTimeStamp)
    put("endTimeStamp", bucket.endTimeStamp)
    put("tag", if (Build.VERSION.SDK_INT >= 24) bucket.tag else JSONObject.NULL)
    put("roaming", if (Build.VERSION.SDK_INT >= 24) bucket.roaming else JSONObject.NULL)
    put("metered", if (Build.VERSION.SDK_INT >= 24) bucket.metered else JSONObject.NULL)
}
