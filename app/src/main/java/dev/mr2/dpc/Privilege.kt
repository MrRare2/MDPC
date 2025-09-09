package dev.mr2.dpc

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageInstaller
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Build
import android.os.HardwarePropertiesManager
import dev.mr2.dpc.dpm.binderWrapperDevicePolicyManager
import dev.mr2.dpc.dpm.binderWrapperPackageInstaller
import dev.mr2.dpc.dpm.dhizukuErrorStatus
import com.rosan.dhizuku.api.Dhizuku
import kotlinx.coroutines.flow.MutableStateFlow

object Privilege {
    fun initialize(context: Context) {
        if (SP.dhizuku) {
            Dhizuku.init(context)
            val hasPermission = try {
                Dhizuku.isPermissionGranted()
            } catch(_: Exception) {
                false
            }
            if (hasPermission) {
                val dhizukuDpm = binderWrapperDevicePolicyManager(context)
		val dhizukuPm = binderWrapperPackageInstaller(context)
		if (dhizukuPm != null) PIM = dhizukuPm
                if (dhizukuDpm != null) {
                    DPM = dhizukuDpm
                    DAR = Dhizuku.getOwnerComponent()
                    return
                }
            }
            dhizukuErrorStatus.value = 2
        }
        DPM = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
	HWM = context.getSystemService(Context.HARDWARE_PROPERTIES_SERVICE) as HardwarePropertiesManager
	WM = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
	PIM = context.packageManager.packageInstaller
        DAR = MyAdminComponent
    }
    lateinit var DPM: DevicePolicyManager
        private set
    lateinit var DAR: ComponentName
        private set
    lateinit var PIM: PackageInstaller
        private set
    var WM: WifiManager? = null
    var HWM: HardwarePropertiesManager? = null

    data class Status(
        val device: Boolean = false,
        val profile: Boolean = false,
        val dhizuku: Boolean = false,
        val work: Boolean = false,
        val org: Boolean = false,
        val affiliated: Boolean = false
    ) {
        val activated = device || profile
        val primary = Binder.getCallingUid() / 100000 == 0 // Primary user
    }
    val status = MutableStateFlow(Status())
    fun updateStatus() {
        val profile = DPM.isProfileOwnerApp(DAR.packageName)
        val work = profile && Build.VERSION.SDK_INT >= 24 && DPM.isManagedProfile(DAR)
        status.value = Status(
            device = DPM.isDeviceOwnerApp(DAR.packageName),
            profile = profile,
            dhizuku = SP.dhizuku,
            work = work,
            org = work && Build.VERSION.SDK_INT >= 30 && DPM.isOrganizationOwnedDeviceWithManagedProfile,
            affiliated = Build.VERSION.SDK_INT >= 28 && DPM.isAffiliatedUser
        )
    }
}
