package dev.mr2.dpc

import android.Manifest
import android.content.pm.PackageManager
import android.content.Context
import android.os.Build.VERSION
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.mr2.dpc.dpm.AddApnSetting
import dev.mr2.dpc.dpm.AddApnSettingScreen
import dev.mr2.dpc.dpm.AddDelegatedAdmin
import dev.mr2.dpc.dpm.AddDelegatedAdminScreen
import dev.mr2.dpc.dpm.AddPreferentialNetworkServiceConfig
import dev.mr2.dpc.dpm.AddPreferentialNetworkServiceConfigScreen
import dev.mr2.dpc.dpm.AffiliationId
import dev.mr2.dpc.dpm.AffiliationIdScreen
import dev.mr2.dpc.dpm.AlwaysOnVpnPackage
import dev.mr2.dpc.dpm.AlwaysOnVpnPackageScreen
import dev.mr2.dpc.dpm.ApplicationDetails
import dev.mr2.dpc.dpm.ApplicationDetailsScreen
import dev.mr2.dpc.dpm.ApplicationsFeatures
import dev.mr2.dpc.dpm.ApplicationsFeaturesScreen
import dev.mr2.dpc.dpm.AutoTimePolicy
import dev.mr2.dpc.dpm.AutoTimePolicyScreen
import dev.mr2.dpc.dpm.AutoTimeZonePolicy
import dev.mr2.dpc.dpm.AutoTimeZonePolicyScreen
import dev.mr2.dpc.dpm.BlockUninstall
import dev.mr2.dpc.dpm.CaCert
import dev.mr2.dpc.dpm.CaCertScreen
import dev.mr2.dpc.dpm.ChangeTime
import dev.mr2.dpc.dpm.ChangeTimeScreen
import dev.mr2.dpc.dpm.ChangeTimeZone
import dev.mr2.dpc.dpm.ChangeTimeZoneScreen
import dev.mr2.dpc.dpm.ChangeUsername
import dev.mr2.dpc.dpm.ChangeUsernameScreen
import dev.mr2.dpc.dpm.ClearAppStorage
import dev.mr2.dpc.dpm.ClearAppStorageScreen
import dev.mr2.dpc.dpm.ContentProtectionPolicy
import dev.mr2.dpc.dpm.ContentProtectionPolicyScreen
import dev.mr2.dpc.dpm.CreateUser
import dev.mr2.dpc.dpm.CreateUserScreen
import dev.mr2.dpc.dpm.CreateWorkProfile
import dev.mr2.dpc.dpm.CreateWorkProfileScreen
import dev.mr2.dpc.dpm.CredentialManagerPolicy
import dev.mr2.dpc.dpm.CredentialManagerPolicyScreen
import dev.mr2.dpc.dpm.CrossProfileIntentFilter
import dev.mr2.dpc.dpm.CrossProfileIntentFilterScreen
import dev.mr2.dpc.dpm.CrossProfilePackages
import dev.mr2.dpc.dpm.CrossProfileWidgetProviders
import dev.mr2.dpc.dpm.DefaultInputMethod
import dev.mr2.dpc.dpm.DefaultInputMethodScreen
import dev.mr2.dpc.dpm.DelegatedAdmins
import dev.mr2.dpc.dpm.DelegatedAdminsScreen
import dev.mr2.dpc.dpm.DeleteWorkProfile
import dev.mr2.dpc.dpm.DeleteWorkProfileScreen
import dev.mr2.dpc.dpm.DeviceInfo
import dev.mr2.dpc.dpm.DeviceInfoScreen
import dev.mr2.dpc.dpm.DhizukuServerSettings
import dev.mr2.dpc.dpm.DhizukuServerSettingsScreen
import dev.mr2.dpc.dpm.DisableAccountManagement
import dev.mr2.dpc.dpm.DisableAccountManagementScreen
import dev.mr2.dpc.dpm.DisableMeteredData
import dev.mr2.dpc.dpm.DisableUserControl
import dev.mr2.dpc.dpm.EditAppGroup
import dev.mr2.dpc.dpm.EditAppGroupScreen
import dev.mr2.dpc.dpm.EnableSystemApp
import dev.mr2.dpc.dpm.EnableSystemAppScreen
import dev.mr2.dpc.dpm.FrpPolicy
import dev.mr2.dpc.dpm.FrpPolicyScreen
import dev.mr2.dpc.dpm.HardwareMonitor
import dev.mr2.dpc.dpm.HardwareMonitorScreen
import dev.mr2.dpc.dpm.Hide
import dev.mr2.dpc.dpm.InstallExistingApp
import dev.mr2.dpc.dpm.InstallExistingAppScreen
import dev.mr2.dpc.dpm.InstallSystemUpdate
import dev.mr2.dpc.dpm.InstallSystemUpdateScreen
import dev.mr2.dpc.dpm.KeepUninstalledPackages
import dev.mr2.dpc.dpm.Keyguard
import dev.mr2.dpc.dpm.KeyguardDisabledFeatures
import dev.mr2.dpc.dpm.KeyguardDisabledFeaturesScreen
import dev.mr2.dpc.dpm.KeyguardScreen
import dev.mr2.dpc.dpm.LockScreenInfo
import dev.mr2.dpc.dpm.LockScreenInfoScreen
import dev.mr2.dpc.dpm.LockTaskMode
import dev.mr2.dpc.dpm.LockTaskModeScreen
import dev.mr2.dpc.dpm.ManageAppGroups
import dev.mr2.dpc.dpm.ManageAppGroupsScreen
import dev.mr2.dpc.dpm.ManagedConfiguration
import dev.mr2.dpc.dpm.ManagedConfigurationScreen
import dev.mr2.dpc.dpm.MtePolicy
import dev.mr2.dpc.dpm.MtePolicyScreen
import dev.mr2.dpc.dpm.NearbyStreamingPolicy
import dev.mr2.dpc.dpm.NearbyStreamingPolicyScreen
import dev.mr2.dpc.dpm.Network
import dev.mr2.dpc.dpm.NetworkLogging
import dev.mr2.dpc.dpm.NetworkLoggingScreen
import dev.mr2.dpc.dpm.NetworkOptions
import dev.mr2.dpc.dpm.NetworkOptionsScreen
import dev.mr2.dpc.dpm.NetworkScreen
import dev.mr2.dpc.dpm.NetworkStatsScreen
import dev.mr2.dpc.dpm.NetworkStatsViewer
import dev.mr2.dpc.dpm.NetworkStatsViewerScreen
import dev.mr2.dpc.dpm.OrganizationOwnedProfile
import dev.mr2.dpc.dpm.OrganizationOwnedProfileScreen
import dev.mr2.dpc.dpm.OverrideApn
import dev.mr2.dpc.dpm.OverrideApnScreen
import dev.mr2.dpc.dpm.PackageFunctionScreen
import dev.mr2.dpc.dpm.Password
import dev.mr2.dpc.dpm.PasswordInfo
import dev.mr2.dpc.dpm.PasswordInfoScreen
import dev.mr2.dpc.dpm.PasswordScreen
import dev.mr2.dpc.dpm.PermissionPolicy
import dev.mr2.dpc.dpm.PermissionPolicyScreen
import dev.mr2.dpc.dpm.AppPermissionsManager
import dev.mr2.dpc.dpm.AppPermissionsManagerScreen
import dev.mr2.dpc.dpm.PermissionDetail
import dev.mr2.dpc.dpm.PermissionDetailScreen
import dev.mr2.dpc.dpm.PermissionManager
import dev.mr2.dpc.dpm.PermissionManagerScreen
import dev.mr2.dpc.dpm.PermittedAccessibilityServices
import dev.mr2.dpc.dpm.PermittedAsAndImPackages
import dev.mr2.dpc.dpm.PermittedInputMethods
import dev.mr2.dpc.dpm.PreferentialNetworkService
import dev.mr2.dpc.dpm.PreferentialNetworkServiceInfo
import dev.mr2.dpc.dpm.PreferentialNetworkServiceScreen
import dev.mr2.dpc.dpm.PrivateDns
import dev.mr2.dpc.dpm.PrivateDnsScreen
import dev.mr2.dpc.dpm.QueryNetworkStats
import dev.mr2.dpc.dpm.RecommendedGlobalProxy
import dev.mr2.dpc.dpm.RecommendedGlobalProxyScreen
import dev.mr2.dpc.dpm.RequiredPasswordComplexity
import dev.mr2.dpc.dpm.RequiredPasswordComplexityScreen
import dev.mr2.dpc.dpm.RequiredPasswordQuality
import dev.mr2.dpc.dpm.RequiredPasswordQualityScreen
import dev.mr2.dpc.dpm.ResetPassword
import dev.mr2.dpc.dpm.ResetPasswordScreen
import dev.mr2.dpc.dpm.ResetPasswordToken
import dev.mr2.dpc.dpm.ResetPasswordTokenScreen
import dev.mr2.dpc.dpm.SecurityLogging
import dev.mr2.dpc.dpm.SecurityLoggingScreen
import dev.mr2.dpc.dpm.SetDefaultDialer
import dev.mr2.dpc.dpm.SetDefaultDialerScreen
import dev.mr2.dpc.dpm.SetSystemUpdatePolicy
import dev.mr2.dpc.dpm.SupportMessage
import dev.mr2.dpc.dpm.SupportMessageScreen
import dev.mr2.dpc.dpm.Suspend
import dev.mr2.dpc.dpm.SuspendPersonalApp
import dev.mr2.dpc.dpm.SuspendPersonalAppScreen
import dev.mr2.dpc.dpm.SystemManager
import dev.mr2.dpc.dpm.SystemManagerScreen
import dev.mr2.dpc.dpm.SystemOptions
import dev.mr2.dpc.dpm.SystemOptionsScreen
import dev.mr2.dpc.dpm.SystemUpdatePolicyScreen
import dev.mr2.dpc.dpm.TransferOwnership
import dev.mr2.dpc.dpm.TransferOwnershipScreen
import dev.mr2.dpc.dpm.UninstallApp
import dev.mr2.dpc.dpm.UninstallAppScreen
import dev.mr2.dpc.dpm.UpdateNetwork
import dev.mr2.dpc.dpm.UpdateNetworkScreen
import dev.mr2.dpc.dpm.UserInfo
import dev.mr2.dpc.dpm.UserInfoScreen
import dev.mr2.dpc.dpm.UserOperation
import dev.mr2.dpc.dpm.UserOperationScreen
import dev.mr2.dpc.dpm.UserRestriction
import dev.mr2.dpc.dpm.UserRestrictionEditor
import dev.mr2.dpc.dpm.UserRestrictionEditorScreen
import dev.mr2.dpc.dpm.UserRestrictionOptions
import dev.mr2.dpc.dpm.UserRestrictionOptionsScreen
import dev.mr2.dpc.dpm.UserRestrictionScreen
import dev.mr2.dpc.dpm.UserSessionMessage
import dev.mr2.dpc.dpm.UserSessionMessageScreen
import dev.mr2.dpc.dpm.Users
import dev.mr2.dpc.dpm.UsersOptions
import dev.mr2.dpc.dpm.UsersOptionsScreen
import dev.mr2.dpc.dpm.UsersScreen
import dev.mr2.dpc.dpm.WiFi
import dev.mr2.dpc.dpm.WifiScreen
import dev.mr2.dpc.dpm.WifiSecurityLevel
import dev.mr2.dpc.dpm.WifiSecurityLevelScreen
import dev.mr2.dpc.dpm.WifiSsidPolicyScreen
import dev.mr2.dpc.dpm.WipeData
import dev.mr2.dpc.dpm.WipeDataScreen
import dev.mr2.dpc.dpm.WorkModes
import dev.mr2.dpc.dpm.WorkModesScreen
import dev.mr2.dpc.dpm.WorkProfile
import dev.mr2.dpc.dpm.WorkProfileScreen
import dev.mr2.dpc.dpm.dhizukuErrorStatus
import dev.mr2.dpc.ui.NavTransition
import dev.mr2.dpc.ui.theme.MDPCTheme
import kotlinx.serialization.Serializable
import java.util.Locale

@ExperimentalMaterial3Api
class MainActivity : FragmentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val lang = SP.language ?: "default"
        val region = SP.languageRegion ?: "default"
        val wrapped = if (lang != "default") newBase.setLocale(lang ?: "en", region ?: "") else newBase
        super.attachBaseContext(wrapped)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val context: Context = this

        val locale = if (VERSION.SDK_INT >= 24) {
            context.resources.configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
        CJK = locale.language in setOf("zh", "ja", "ko")

        val vm by viewModels<MyViewModel>()

        if (
            VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            val launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        registerPackageRemovedReceiver(this) {
            vm.onPackageRemoved(it)
        }

	    setContent {
	        var appLockDialog by rememberSaveable { mutableStateOf(false) }
	        val theme by vm.theme.collectAsStateWithLifecycle()

            MDPCTheme(theme) {
                Home(vm) { appLockDialog = true }
                if (appLockDialog) {
                    AppLockDialog(
                        onSucceed = { appLockDialog = false },
                        onDismiss = { moveTaskToBack(true) }
                    )
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun Home(vm: MyViewModel, onLock: () -> Unit) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    val lifecycleOwner = LocalLifecycleOwner.current
    fun navigateUp() { navController.navigateUp() }
    fun navigate(destination: Any) {
        navController.navigate(destination) {
            launchSingleTop = true
        }
    }
    fun choosePackage() {
        navController.navigate(ApplicationsList(false, true))
    }
    fun chooseSinglePackage() {
        navController.navigate(ApplicationsList(false, false))
    }
    fun navigateToAppGroups() {
        navController.navigate(ManageAppGroups)
    }
    LaunchedEffect(Unit) {
        if (!Privilege.status.value.activated) {
            navController.navigate(WorkModes(false)) {
                popUpTo<Home> { inclusive = true }
            }
        }

        if (SP.apiTcpEnabled) vm.startApiTcpServer(true)
        else vm.startApiTcpServer(false)
    }
    @Suppress("NewApi") NavHost(
        navController = navController,
        startDestination = Home,
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .pointerInput(Unit) { detectTapGestures(onTap = { focusMgr.clearFocus() }) },
        enterTransition = { NavTransition.enterTransition },
        exitTransition = { NavTransition.exitTransition },
        popEnterTransition = { NavTransition.popEnterTransition },
        popExitTransition = { NavTransition.popExitTransition }
    ) {
        composable<Home> { HomeScreen(::navigate) }
        composable<WorkModes> {
            WorkModesScreen(vm, it.toRoute(), ::navigateUp, {
                navController.navigate(Home) {
                    popUpTo<WorkModes> { inclusive = true }
                }
	    }, {
                navController.navigate(WorkModes(false)) {
                    popUpTo(Home) { inclusive = true }
                }
            }, ::navigate)
        }
        composable<DhizukuServerSettings> {
            DhizukuServerSettingsScreen(vm.dhizukuClients, vm::getDhizukuClients,
                vm::updateDhizukuClient, vm::getDhizukuServerEnabled, vm::setDhizukuServerEnabled,
                ::navigateUp)
        }

        composable<DelegatedAdmins> {
            DelegatedAdminsScreen(vm.delegatedAdmins, vm::getDelegatedAdmins, ::navigateUp, ::navigate)
        }

        composable<AddDelegatedAdmin>{
	        AddDelegatedAdminScreen(vm.chosenPackage, ::chooseSinglePackage, it.toRoute(),
                vm::setDelegatedAdmin,  ::navigateUp)
        }
        composable<DeviceInfo> { DeviceInfoScreen(vm, ::navigateUp) }
        composable<LockScreenInfo> {
            LockScreenInfoScreen(vm::getLockScreenInfo, vm::setLockScreenInfo, ::navigateUp)
        }
        composable<LockScreenInfo> {
            LockScreenInfoScreen(vm::getLockScreenInfo, vm::setLockScreenInfo, ::navigateUp)
        }
        composable<SupportMessage> {
            SupportMessageScreen(vm::getShortSupportMessage, vm::getLongSupportMessage,
                vm::setShortSupportMessage, vm::setLongSupportMessage, ::navigateUp)
        }
        composable<TransferOwnership> {
            TransferOwnershipScreen(vm.deviceAdminReceivers, vm::getDeviceAdminReceivers,
                vm::transferOwnership, vm::startApiTcpServer, ::navigateUp) {
                navController.navigate(WorkModes(false)) {
                    popUpTo(Home) { inclusive = true }
                }
            }
        }
        composable<SystemManager> { SystemManagerScreen(vm, ::navigateUp, ::navigate) }
        composable<SystemOptions> { SystemOptionsScreen(vm, ::navigateUp) }
        composable<Keyguard> {
            KeyguardScreen(vm::setKeyguardDisabled, vm::lockScreen, ::navigateUp)
        }
        composable<HardwareMonitor> {
            HardwareMonitorScreen(vm.hardwareProperties, vm::getHardwareProperties,
                vm::setHpRefreshInterval, ::navigateUp)
        }
        composable<DefaultInputMethod> {
            DefaultInputMethodScreen(vm::getCurrentInputMethod, vm.inputMethodList,
                vm::getInputMethods, vm::setDefaultInputMethod, ::navigateUp)
        }
        composable<ChangeTime> { ChangeTimeScreen(vm::setTime, ::navigateUp) }
        composable<ChangeTimeZone> { ChangeTimeZoneScreen(vm::setTimeZone, ::navigateUp) }
        composable<AutoTimePolicy> {
            AutoTimePolicyScreen(vm::getAutoTimePolicy, vm::setAutoTimePolicy, ::navigateUp)
        }
        composable<AutoTimeZonePolicy> {
            AutoTimeZonePolicyScreen(vm::getAutoTimeZonePolicy, vm::setAutoTimeZonePolicy,
                ::navigateUp)
        }
        //composable<> { KeyPairs(::navigateUp) }
        composable<ContentProtectionPolicy> {
            ContentProtectionPolicyScreen(vm::getContentProtectionPolicy,
                vm::setContentProtectionPolicy, ::navigateUp)
        }
        composable<PermissionPolicy> {
            PermissionPolicyScreen(vm::getPermissionPolicy, vm::setPermissionPolicy, ::navigateUp)
        }
        composable<MtePolicy> {
            MtePolicyScreen(vm::getMtePolicy, vm::setMtePolicy, ::navigateUp)
        }
        composable<NearbyStreamingPolicy> {
            NearbyStreamingPolicyScreen(vm::getNsAppPolicy, vm::setNsAppPolicy,
                vm::getNsNotificationPolicy, vm::setNsNotificationPolicy, ::navigateUp)
        }
        composable<LockTaskMode> {
            LockTaskModeScreen(
                vm.chosenPackage, ::chooseSinglePackage, ::choosePackage, vm.lockTaskPackages,
                vm::getLockTaskPackages, vm::setLockTaskPackage, vm::startLockTaskMode,
                vm::getLockTaskFeatures, vm::setLockTaskFeatures, ::navigateUp
            )
        }
        composable<CaCert> {
            CaCertScreen(vm.installedCaCerts, vm::getCaCerts, vm.selectedCaCert, vm::selectCaCert, vm::installCaCert, vm::parseCaCert,
	            vm::exportCaCert, vm::uninstallCaCert, vm::uninstallAllCaCerts, ::navigateUp)
        }
        composable<SecurityLogging> {
            SecurityLoggingScreen(vm::getSecurityLoggingEnabled, vm::setSecurityLoggingEnabled,
                vm::exportSecurityLogs, vm::getSecurityLogsCount, vm::deleteSecurityLogs,
                vm::getPreRebootSecurityLogs, vm::exportPreRebootSecurityLogs, ::navigateUp)
        }
        composable<DisableAccountManagement> {
            DisableAccountManagementScreen(vm.mdAccountTypes, vm::getMdAccountTypes,
                vm::setMdAccountType, ::navigateUp)
        }
        composable<SetSystemUpdatePolicy> {
            SystemUpdatePolicyScreen(vm::getSystemUpdatePolicy, vm::setSystemUpdatePolicy,
                vm::getPendingSystemUpdate, ::navigateUp)
        }
        composable<InstallSystemUpdate> {
            InstallSystemUpdateScreen(vm::installSystemUpdate, ::navigateUp)
        }
        composable<FrpPolicy> {
            FrpPolicyScreen(vm.getFrpPolicy(), vm::setFrpPolicy, ::navigateUp)
        }
        composable<WipeData> { WipeDataScreen(vm::wipeData, ::navigateUp) }

        composable<Network> { NetworkScreen(::navigateUp, ::navigate) }
        
        composable<WiFi> {
            WifiScreen(vm, ::navigateUp, ::navigate) { navController.navigate(UpdateNetwork(it)) }
        }
        composable<NetworkOptions> {
            NetworkOptionsScreen(vm::getLanEnabled, vm::setLanEnabled, ::navigateUp)
        }
        composable<UpdateNetwork> {
            val info = vm.configuredNetworks.collectAsStateWithLifecycle().value[
                (it.toRoute() as UpdateNetwork).index
            ]
            UpdateNetworkScreen(info, vm::setWifi, ::navigateUp)
        }
        composable<WifiSecurityLevel> {
            WifiSecurityLevelScreen(vm::getMinimumWifiSecurityLevel,
                vm::setMinimumWifiSecurityLevel, ::navigateUp)
        }
        composable<WifiSsidPolicyScreen> {
            WifiSsidPolicyScreen(vm::getSsidPolicy, vm::setSsidPolicy, ::navigateUp)
        }
        composable<QueryNetworkStats> {
            NetworkStatsScreen(vm.chosenPackage, ::chooseSinglePackage, vm::getPackageUid,
                vm::queryNetworkStats, ::navigateUp) { navController.navigate(NetworkStatsViewer) }
        }
        composable<NetworkStatsViewer> {
            NetworkStatsViewerScreen(vm.networkStatsData, vm::clearNetworkStats, ::navigateUp)
        }
        composable<PrivateDns> {
            PrivateDnsScreen(vm::getPrivateDns, vm::setPrivateDns, ::navigateUp)
        }
        composable<AlwaysOnVpnPackage> {
            AlwaysOnVpnPackageScreen(vm::getAlwaysOnVpnPackage, vm::getAlwaysOnVpnLockdown,
                vm::setAlwaysOnVpn, vm.chosenPackage, ::chooseSinglePackage, ::navigateUp)
        }
        composable<RecommendedGlobalProxy> {
            RecommendedGlobalProxyScreen(vm::setRecommendedGlobalProxy, ::navigateUp)
        }
        composable<NetworkLogging> {
            NetworkLoggingScreen(vm::getNetworkLoggingEnabled, vm::setNetworkLoggingEnabled,
                vm::getNetworkLogsCount, vm::exportNetworkLogs, vm::deleteNetworkLogs, ::navigateUp)
        }
        //composable<WifiAuthKeypair> { WifiAuthKeypairScreen(::navigateUp) }
        composable<PreferentialNetworkService> {
            PreferentialNetworkServiceScreen(vm::getPnsEnabled, vm::setPnsEnabled, vm.pnsConfigs,
                vm::getPnsConfigs, ::navigateUp, ::navigate)
        }
        composable<AddPreferentialNetworkServiceConfig> {
            val info = vm.pnsConfigs.collectAsStateWithLifecycle().value.getOrNull(
                it.toRoute<AddPreferentialNetworkServiceConfig>().index
            ) ?: PreferentialNetworkServiceInfo()
            AddPreferentialNetworkServiceConfigScreen(info, vm::setPnsConfig, ::navigateUp)
        }
        composable<OverrideApn> {
            OverrideApnScreen(vm.apnConfigs, vm::getApnConfigs, vm::getApnEnabled,
                vm::setApnEnabled, ::navigateUp) { navController.navigate(AddApnSetting(it)) }
        }
        composable<AddApnSetting> {
            val origin = vm.apnConfigs.collectAsStateWithLifecycle().value.getOrNull((it.toRoute() as AddApnSetting).index)
            AddApnSettingScreen(vm::setApnConfig, vm::removeApnConfig, origin, ::navigateUp)
        }

        composable<WorkProfile> { WorkProfileScreen(::navigateUp, ::navigate) }

        composable<OrganizationOwnedProfile> {
            OrganizationOwnedProfileScreen(vm::activateOrgProfileByShizuku, ::navigateUp)
        }
        composable<CreateWorkProfile> {
            CreateWorkProfileScreen(vm::createWorkProfile, ::navigateUp)
        }
        composable<SuspendPersonalApp> {
            SuspendPersonalAppScreen(
                vm::getPersonalAppsSuspendedReason, vm::setPersonalAppsSuspended,
                vm::getProfileMaxTimeOff, vm::setProfileMaxTimeOff, ::navigateUp
            )
        }
        composable<CrossProfileIntentFilter> {
             CrossProfileIntentFilterScreen(
                vm::addCrossProfileIntentFilter, vm::clearCrossProfileIntentFilters,
                vm::importCrossProfileIntentFilters, vm::exportCrossProfileIntentFilters,
                ::navigateUp
            )
        }
        composable<DeleteWorkProfile> { DeleteWorkProfileScreen(vm::wipeData, ::navigateUp) }

        composable<ApplicationsList> {
            val params = it.toRoute<ApplicationsList>()
            AppChooserScreen(
                params, vm.installedPackages, vm.refreshPackagesProgress, { name ->
                if (params.canSwitchView) {
                        if (name == null)  navigateUp()
                        else navigate(ApplicationDetails(name))
                    } else {
                        if (name != null) vm.chosenPackage.trySend(name)
                        navigateUp()
                    }
                }, {
                    SP.applicationsListView = false
                    navController.navigate(ApplicationsFeatures) { popUpTo(Home) }
                    }, vm::refreshPackageList, vm::setPackageSuspended, vm::setPackageHidden)
            }
        composable<ApplicationsFeatures> {
            ApplicationsFeaturesScreen(::navigateUp, ::navigate) {
                SP.applicationsListView = true
                navController.navigate(ApplicationsList(true, true)) { popUpTo(Home) }
                }
            }

        composable<ApplicationDetails> {
            ApplicationDetailsScreen(it.toRoute(), vm, ::navigateUp, ::navigate)
        }
        composable<Suspend> {
            PackageFunctionScreen(
                R.string.suspend, vm.suspendedPackages, vm::getSuspendedPackaged,
                vm::setPackageSuspended, ::navigateUp, vm.chosenPackage, ::choosePackage,
                ::navigateToAppGroups, vm.appGroups, R.string.info_suspend_app
            )
        }
        composable<Hide> {
            PackageFunctionScreen(
                R.string.hide, vm.hiddenPackages, vm::getHiddenPackages, vm::setPackageHidden,
                ::navigateUp, vm.chosenPackage, ::choosePackage, ::navigateToAppGroups, vm.appGroups
            )
        }
        composable<BlockUninstall> {
            PackageFunctionScreen(
                R.string.block_uninstall, vm.ubPackages, vm::getUbPackages, vm::setPackageUb,
                ::navigateUp, vm.chosenPackage, ::choosePackage, ::navigateToAppGroups, vm.appGroups
            )
        }
	    composable<DisableUserControl> {
            PackageFunctionScreen(
                R.string.disable_user_control, vm.ucdPackages, vm::getUcdPackages,
                vm::setPackageUcd, ::navigateUp, vm.chosenPackage, ::choosePackage,
                ::navigateToAppGroups, vm.appGroups, R.string.info_disable_user_control
            )
        }
        composable<AppPermissionsManager> {
            AppPermissionsManagerScreen(
                vm::getPackagePermissions, vm::setPackagePermission, ::navigateUp, it.toRoute()
            )
        }
        composable<PermissionManager> {
            PermissionManagerScreen(::navigate, ::navigateUp)
        }
        composable<PermissionDetail> {
            PermissionDetailScreen(
                it.toRoute(), vm::getPermissionPackages, vm::setPackagePermission, ::navigateUp
            )
        }
	    composable<DisableMeteredData> {
            PackageFunctionScreen(
                R.string.disable_metered_data, vm.mddPackages, vm::getMddPackages,
                vm::setPackageMdd, ::navigateUp, vm.chosenPackage, ::choosePackage,
                ::navigateToAppGroups, vm.appGroups
            )
        }
	    composable<ClearAppStorage> {
            ClearAppStorageScreen(
                vm.chosenPackage, ::chooseSinglePackage, vm::clearAppData, ::navigateUp
            )
        }
        composable<UninstallApp> {
            UninstallAppScreen(
                vm.chosenPackage, ::chooseSinglePackage, vm::uninstallPackage, ::navigateUp
            )
        }
	    composable<KeepUninstalledPackages> {
            PackageFunctionScreen(
                R.string.keep_uninstalled_packages, vm.kuPackages, vm::getKuPackages,
                vm::setPackageKu, ::navigateUp, vm.chosenPackage, ::choosePackage,
                ::navigateToAppGroups, vm.appGroups, R.string.info_keep_uninstalled_apps
            )
        }
	    composable<InstallExistingApp> {
            InstallExistingAppScreen(
                vm.chosenPackage, ::chooseSinglePackage, vm::installExistingApp, ::navigateUp
            )
        }
        composable<CrossProfilePackages> {
            PackageFunctionScreen(
                R.string.cross_profile_apps, vm.cpPackages,
                vm::getCpPackages, vm::setPackageCp, ::navigateUp, vm.chosenPackage,
                ::choosePackage, ::navigateToAppGroups, vm.appGroups
            )
        }
	    composable<CrossProfileWidgetProviders> {
            PackageFunctionScreen(R.string.cross_profile_widget, vm.cpwProviders,
                vm::getCpwProviders, vm::setCpwProvider, ::navigateUp, vm.chosenPackage,
                ::choosePackage, ::navigateToAppGroups, vm.appGroups)
        }
        composable<CredentialManagerPolicy> {
            CredentialManagerPolicyScreen(
                vm.chosenPackage, ::choosePackage, vm.cmPackages, vm::getCmPolicy,
                vm::setCmPackage, vm::setCmPolicy, ::navigateUp
            )
        }
	    composable<PermittedAccessibilityServices> {
            PermittedAsAndImPackages(
                R.string.permitted_accessibility_services,
                R.string.system_accessibility_always_allowed, vm.chosenPackage, ::choosePackage,
                vm.pasPackages, vm::getPasPackages, vm::setPasPackage, vm::setPasPolicy, ::navigateUp
            )
        }
	    composable<PermittedInputMethods> {
            PermittedAsAndImPackages(
                R.string.permitted_ime, R.string.system_ime_always_allowed,
                vm.chosenPackage, ::choosePackage, vm.pimPackages, vm::getPimPackages,
                vm::setPimPackage, vm::setPimPolicy, ::navigateUp
            )
        }
	    composable<EnableSystemApp> {
            EnableSystemAppScreen(
                vm.chosenPackage, ::choosePackage, vm::enableSystemApp, ::navigateUp
            )
        }
        composable<SetDefaultDialer> {
            SetDefaultDialerScreen(
                vm.chosenPackage, ::choosePackage, vm::setDefaultDialer, ::navigateUp
            )
        }
        composable<ManagedConfiguration> {
            ManagedConfigurationScreen(
                it.toRoute(), vm.appRestrictions, vm::setAppRestrictions,
                vm::clearAppRestrictions, ::navigateUp
            )
        }
        composable<ManageAppGroups> {
            ManageAppGroupsScreen(
                vm.appGroups, vm::exportAppGroups, vm::importAppGroups,
                { id, name, apps -> navController.navigate(EditAppGroup(id, name, apps)) },
                ::navigateUp
            )
        }
        composable<EditAppGroup> {
            EditAppGroupScreen(
                it.toRoute(), vm::getAppInfo, ::navigateUp, vm::setAppGroup,
                vm::deleteAppGroup, ::choosePackage, vm.chosenPackage
            )
        }

        composable<UserRestriction> {
	        UserRestrictionScreen(vm::getUserRestrictions, vm::getShortcutsEnabled, ::navigateUp, ::navigate)
        }
        composable<UserRestrictionEditor> {
            UserRestrictionEditorScreen(vm.userRestrictions, vm::setUserRestriction, ::navigateUp)
        }
        composable<UserRestrictionOptions> {
            UserRestrictionOptionsScreen(it.toRoute(), vm.userRestrictions,
                vm::setUserRestriction, vm::createUserRestrictionShortcut,
                vm::getShortcutsEnabled,
                ::navigateUp)
        }

        composable<Users> { UsersScreen(vm, ::navigateUp, ::navigate) }
        composable<UserInfo> { UserInfoScreen(vm::getUserInformation, ::navigateUp) }
        composable<UsersOptions> {
            UsersOptionsScreen(vm::getLogoutEnabled, vm::setLogoutEnabled, ::navigateUp)
        }
        composable<UserOperation> {
            UserOperationScreen(vm::getUserIdentifiers, vm::doUserOperation,
                vm::createUserOperationShortcut, ::navigateUp)
        }
        composable<CreateUser> { CreateUserScreen(vm::createUser, ::navigateUp) }
        composable<ChangeUsername> { ChangeUsernameScreen(vm::setProfileName, ::navigateUp) }
        composable<UserSessionMessage> {
            UserSessionMessageScreen(vm::getUserSessionMessages, vm::setStartUserSessionMessage,
                vm::setEndUserSessionMessage, ::navigateUp)
        }
        composable<AffiliationId> {
            AffiliationIdScreen(vm.affiliationIds, vm::getAffiliationIds, vm::setAffiliationId,
                ::navigateUp)
        }

        composable<Password> { PasswordScreen(vm, ::navigateUp, ::navigate) }
        composable<PasswordInfo> {
            PasswordInfoScreen(vm::getPasswordComplexity, vm::isPasswordComplexitySufficient,
                vm::isUsingUnifiedPassword, ::navigateUp)
        }
        composable<ResetPasswordToken> {
            ResetPasswordTokenScreen(vm::getRpTokenState, vm::setRpToken,
                vm::createActivateRpTokenIntent, vm::clearRpToken, ::navigateUp)
        }
        composable<ResetPassword> { ResetPasswordScreen(vm::resetPassword, ::navigateUp) }
        composable<RequiredPasswordComplexity> {
            RequiredPasswordComplexityScreen(vm::getRequiredPasswordComplexity,
                vm::setRequiredPasswordComplexity, ::navigateUp)
        }
        composable<KeyguardDisabledFeatures> {
            KeyguardDisabledFeaturesScreen(vm::getKeyguardDisableConfig,
                vm::setKeyguardDisableConfig, ::navigateUp)
        }
        composable<RequiredPasswordQuality> { RequiredPasswordQualityScreen(::navigateUp, vm::getPasswordQuality, vm::setPasswordQuality) }

        composable<Settings> { SettingsScreen(::navigateUp, ::navigate) }
        composable<SettingsOptions> {
            SettingsOptionsScreen(vm::getDisplayDangerousFeatures, vm::getShortcutsEnabled, vm::getLauncherVisible,
                vm::setDisplayDangerousFeatures, vm::setShortcutsEnabled, vm::setLauncherVisible, ::navigateUp)
        }
        composable<Appearance> {
            AppearanceScreen(::navigateUp, vm.theme, vm::changeTheme)
        }
        composable<AppLockSettings> {
            AppLockSettingsScreen(vm.getAppLockConfig(), vm::setAppLockConfig, ::navigateUp)
        }
        composable<ApiSettings> {
            ApiSettings(vm::getApiEnabled, vm::getApiSrEnabled, vm::setApiKey, vm::setApiSrEnabled,
                vm::getApiTcpEnabled, vm::setApiTcpEnabled,
                vm::getApiPort, vm::setApiPort, vm::startApiTcpServer, vm::restartApiTcpServer, ::navigateUp)
        }
        composable<Notifications> {
            NotificationsScreen(vm.enabledNotifications, vm::getEnabledNotifications,
                vm::setNotificationEnabled, ::navigateUp)
        }
        composable<LanguageScreen> {
            LanguageScreen(vm::getLanguage, vm::getLanguageRegion, vm::setLanguage, ::navigateUp)
        }
        composable<About> { AboutScreen(::navigateUp) }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_CREATE && !SP.lockPasswordHash.isNullOrEmpty()) onLock()
            if (event == Lifecycle.Event.ON_RESUME) {
                if (SP.lockWhenLeaving) onLock()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    LaunchedEffect(Unit) {
        val profileNotActivated = !SP.managedProfileActivated && Privilege.status.value.work
        if (profileNotActivated) {
            Privilege.DPM.setProfileEnabled(Privilege.DAR)
            SP.managedProfileActivated = true
            context.popToast(R.string.work_profile_activated)
        }
    }
    DhizukuErrorDialog {
        dhizukuErrorStatus.value = 0
        Privilege.updateStatus()
        navController.navigate(WorkModes(false)) {
            popUpTo<Home> { inclusive = true }
            launchSingleTop = true
        }
    }
}

@Serializable private object Home

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(onNavigate: (Any) -> Unit) {
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    val sb = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        Modifier.nestedScroll(sb.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton({ onNavigate(WorkModes(true)) }) { Icon(painterResource(R.drawable.security_fill0), null) }
                    IconButton({ onNavigate(Settings) }) { Icon(Icons.Default.Settings, null) }
                },
                scrollBehavior = sb
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) {
        Column(Modifier
            .fillMaxSize()
            .padding(it)
            .verticalScroll(rememberScrollState())) {
            if (privilege.device || privilege.profile) {
                HomePageItem(R.string.system, R.drawable.android_fill0) { onNavigate(SystemManager) }
                HomePageItem(R.string.network, R.drawable.wifi_fill0) { onNavigate(Network) }
            }
            if (privilege.work) {
                HomePageItem(R.string.work_profile, R.drawable.work_fill0) {
                    onNavigate(WorkProfile)
                }
            }
            if (privilege.device || privilege.profile) {
                HomePageItem(R.string.applications, R.drawable.apps_fill0) {
                    onNavigate(
                        if (SP.applicationsListView) ApplicationsList(true, true)
                        else ApplicationsFeatures
                    )
                }
                if (VERSION.SDK_INT >= 24) {
                    HomePageItem(R.string.user_restriction, R.drawable.person_off) { onNavigate(UserRestriction) }
                }
                HomePageItem(R.string.users,R.drawable.manage_accounts_fill0) { onNavigate(Users) }
                HomePageItem(R.string.password_and_keyguard, R.drawable.password_fill0) { onNavigate(Password) }
            }
            Spacer(Modifier.height(BottomPadding))
        }
    }
}

@Composable
fun HomePageItem(name: Int, imgVector: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.padding(start = 30.dp))
        Icon(
            painter = painterResource(imgVector),
            contentDescription = null
        )
        Spacer(Modifier.padding(start = 15.dp))
        Text(
            text = stringResource(name),
            style = typography.headlineSmall,
            modifier = Modifier.padding(bottom = if(CJK) { 2 } else { 0 }.dp)
        )
    }
}

@Composable
private fun DhizukuErrorDialog(onClose: () -> Unit) {
    val status by dhizukuErrorStatus.collectAsState()
    if (status != 0) {
        LaunchedEffect(Unit) {
            SP.dhizuku = false
        }
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                TextButton(onClose) {
                    Text(stringResource(R.string.confirm))
                }
            },
            title = { Text(stringResource(R.string.dhizuku)) },
            text = {
                val text = stringResource(
                    when(status){
                        1 -> R.string.failed_to_init_dhizuku
                        2 -> R.string.dhizuku_permission_not_granted
                        else -> R.string.failed_to_init_dhizuku
                    }
                )
                Text(text)
            },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        )
    }
}
