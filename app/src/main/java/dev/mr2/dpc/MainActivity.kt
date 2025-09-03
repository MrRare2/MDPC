package dev.mr2.dpc

import android.content.Context
import android.os.Build.VERSION
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
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
import dev.mr2.dpc.dpm.AddNetwork
import dev.mr2.dpc.dpm.AddNetworkScreen
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
import dev.mr2.dpc.dpm.BlockUninstallScreen
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
import dev.mr2.dpc.dpm.CrossProfilePackagesScreen
import dev.mr2.dpc.dpm.CrossProfileWidgetProviders
import dev.mr2.dpc.dpm.CrossProfileWidgetProvidersScreen
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
import dev.mr2.dpc.dpm.DisableMeteredDataScreen
import dev.mr2.dpc.dpm.DisableUserControl
import dev.mr2.dpc.dpm.DisableUserControlScreen
import dev.mr2.dpc.dpm.EnableSystemApp
import dev.mr2.dpc.dpm.EnableSystemAppScreen
import dev.mr2.dpc.dpm.FrpPolicy
import dev.mr2.dpc.dpm.FrpPolicyScreen
import dev.mr2.dpc.dpm.HardwareMonitor
import dev.mr2.dpc.dpm.HardwareMonitorScreen
import dev.mr2.dpc.dpm.Hide
import dev.mr2.dpc.dpm.HideScreen
import dev.mr2.dpc.dpm.InstallExistingApp
import dev.mr2.dpc.dpm.InstallExistingAppScreen
import dev.mr2.dpc.dpm.InstallSystemUpdate
import dev.mr2.dpc.dpm.InstallSystemUpdateScreen
import dev.mr2.dpc.dpm.KeepUninstalledPackages
import dev.mr2.dpc.dpm.KeepUninstalledPackagesScreen
import dev.mr2.dpc.dpm.Keyguard
import dev.mr2.dpc.dpm.KeyguardDisabledFeatures
import dev.mr2.dpc.dpm.KeyguardDisabledFeaturesScreen
import dev.mr2.dpc.dpm.KeyguardScreen
import dev.mr2.dpc.dpm.LockScreenInfo
import dev.mr2.dpc.dpm.LockScreenInfoScreen
import dev.mr2.dpc.dpm.LockTaskMode
import dev.mr2.dpc.dpm.LockTaskModeScreen
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
import dev.mr2.dpc.dpm.Password
import dev.mr2.dpc.dpm.PasswordInfo
import dev.mr2.dpc.dpm.PasswordInfoScreen
import dev.mr2.dpc.dpm.PasswordScreen
import dev.mr2.dpc.dpm.PermissionPolicy
import dev.mr2.dpc.dpm.PermissionPolicyScreen
import dev.mr2.dpc.dpm.PermissionsManager
import dev.mr2.dpc.dpm.PermissionsManagerScreen
import dev.mr2.dpc.dpm.PermittedAccessibilityServices
import dev.mr2.dpc.dpm.PermittedAccessibilityServicesScreen
import dev.mr2.dpc.dpm.PermittedInputMethods
import dev.mr2.dpc.dpm.PermittedInputMethodsScreen
import dev.mr2.dpc.dpm.PreferentialNetworkService
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
import dev.mr2.dpc.dpm.Restriction
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
import dev.mr2.dpc.dpm.SuspendScreen
import dev.mr2.dpc.dpm.SystemManager
import dev.mr2.dpc.dpm.SystemManagerScreen
import dev.mr2.dpc.dpm.SystemOptions
import dev.mr2.dpc.dpm.SystemOptionsScreen
import dev.mr2.dpc.dpm.SystemUpdatePolicyScreen
import dev.mr2.dpc.dpm.TransferOwnership
import dev.mr2.dpc.dpm.TransferOwnershipScreen
import dev.mr2.dpc.dpm.UninstallApp
import dev.mr2.dpc.dpm.UninstallAppScreen
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
import dev.mr2.dpc.dpm.WifiAuthKeypair
import dev.mr2.dpc.dpm.WifiAuthKeypairScreen
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
import dev.mr2.dpc.ui.Animations
import dev.mr2.dpc.ui.theme.MDPCTheme
import kotlinx.serialization.Serializable
import java.util.Locale

/* @ExperimentalMaterial3Api
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val context = applicationContext
        val locale = context.resources?.configuration?.locale
        CJK = locale?.language in setOf("zh", "ja", "ko")
        val vm by viewModels<MyViewModel>()
        setContent {
            var appLockDialog by rememberSaveable { mutableStateOf(false) }
	    var certCheckDialog by rememberSaveable { mutableStateOf(false) }
            val theme by vm.theme.collectAsStateWithLifecycle()
            MDPCTheme(theme) {
                Home(vm) { appLockDialog = true }
                if (appLockDialog) {
                    AppLockDialog({ appLockDialog = false }) { moveTaskToBack(true) }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

} */

/*@ExperimentalMaterial3Api
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val currentContext = applicationContext
	val context = // here?
        val locale = context.resources?.configuration?.locale
        CJK = locale?.language in setOf("zh", "ja", "ko")
        val vm by viewModels<MyViewModel>()
        setContent {
            var appLockDialog by rememberSaveable { mutableStateOf(false) }
            var certCheckDialog by rememberSaveable { mutableStateOf(false) }
            val theme by vm.theme.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                if (!context.isVerifiedSignature()) {
                    certCheckDialog = true
                }
            }

            MDPCTheme(theme) {
                Home(vm) { appLockDialog = true }
                if (appLockDialog && !certCheckDialog) {
                    AppLockDialog(
                        onSucceed = { appLockDialog = false },
                        onDismiss = { moveTaskToBack(true) }
                    )
                }
                if (certCheckDialog) {
                    CertVerifyFailedDialog()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }
}*/

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
	setContent {
	    var appLockDialog by rememberSaveable { mutableStateOf(false) }
	    var certCheckDialog by rememberSaveable { mutableStateOf(false) }
	    val theme by vm.theme.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                if (!context.isVerifiedSignature()) {
                    certCheckDialog = true
                }
            }

            MDPCTheme(theme) {
                Home(vm) { appLockDialog = true }
                if (appLockDialog && !certCheckDialog) {
                    AppLockDialog(
                        onSucceed = { appLockDialog = false },
                        onDismiss = { moveTaskToBack(true) }
                    )
                }
                if (certCheckDialog) {
                    CertVerifyFailedDialog()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
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
    fun navigate(destination: Any) { navController.navigate(destination) }
    LaunchedEffect(Unit) {
        if(!Privilege.status.value.activated) {
            navController.navigate(WorkModes(false)) {
                popUpTo<Home> { inclusive = true }
            }
        }
    }
    @Suppress("NewApi") NavHost(
        navController = navController,
        startDestination = Home,
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .pointerInput(Unit) { detectTapGestures(onTap = { focusMgr.clearFocus() }) },
        enterTransition = Animations.navHostEnterTransition,
        exitTransition = Animations.navHostExitTransition,
        popEnterTransition = Animations.navHostPopEnterTransition,
        popExitTransition = Animations.navHostPopExitTransition
    ) {
        composable<Home> { HomeScreen(::navigate) }
        composable<WorkModes> {
            WorkModesScreen(it.toRoute(), ::navigateUp, {
                navController.navigate(Home) {
                    popUpTo<WorkModes> { inclusive = true }
                }
            }, ::navigate)
        }
        composable<DhizukuServerSettings> { DhizukuServerSettingsScreen(::navigateUp) }

        composable<DelegatedAdmins> { DelegatedAdminsScreen(::navigateUp, ::navigate) }
        composable<AddDelegatedAdmin>{ AddDelegatedAdminScreen(it.toRoute(), ::navigateUp) }
        composable<DeviceInfo> { DeviceInfoScreen(::navigateUp) }
        composable<LockScreenInfo> { LockScreenInfoScreen(::navigateUp) }
        composable<SupportMessage> { SupportMessageScreen(::navigateUp) }
        composable<TransferOwnership> {
            TransferOwnershipScreen(::navigateUp) {
                navController.navigate(WorkModes(false)) {
                    popUpTo(Home) { inclusive = true }
                }
            }
        }

        composable<SystemManager> { SystemManagerScreen(::navigateUp, ::navigate) }
        composable<SystemOptions> { SystemOptionsScreen(::navigateUp) }
        composable<Keyguard> { KeyguardScreen(::navigateUp) }
        composable<HardwareMonitor> { HardwareMonitorScreen(::navigateUp) }
        composable<ChangeTime> { ChangeTimeScreen(::navigateUp) }
        composable<ChangeTimeZone> { ChangeTimeZoneScreen(::navigateUp) }
        composable<AutoTimePolicy> { AutoTimePolicyScreen(::navigateUp) }
        composable<AutoTimeZonePolicy> { AutoTimeZonePolicyScreen(::navigateUp) }
        //composable<> { KeyPairs(::navigateUp) }
        composable<ContentProtectionPolicy> { ContentProtectionPolicyScreen(::navigateUp) }
        composable<PermissionPolicy> { PermissionPolicyScreen(::navigateUp) }
        composable<MtePolicy> { MtePolicyScreen(::navigateUp) }
        composable<NearbyStreamingPolicy> { NearbyStreamingPolicyScreen(::navigateUp) }
        composable<LockTaskMode> { LockTaskModeScreen(::navigateUp) }
        composable<CaCert> { CaCertScreen(::navigateUp) }
        composable<SecurityLogging> { SecurityLoggingScreen(::navigateUp) }
        composable<DisableAccountManagement> { DisableAccountManagementScreen(::navigateUp) }
        composable<SetSystemUpdatePolicy> { SystemUpdatePolicyScreen(::navigateUp) }
        composable<InstallSystemUpdate> { InstallSystemUpdateScreen(::navigateUp) }
        composable<FrpPolicy> { FrpPolicyScreen(::navigateUp) }
        composable<WipeData> { WipeDataScreen(::navigateUp) }

        composable<Network> { NetworkScreen(::navigateUp, ::navigate) }
        composable<WiFi> { WifiScreen(::navigateUp, ::navigate) { navController.navigate(AddNetwork, it)} }
        composable<NetworkOptions> { NetworkOptionsScreen(::navigateUp) }
        composable<AddNetwork> { AddNetworkScreen(it.arguments!!, ::navigateUp) }
        composable<WifiSecurityLevel> { WifiSecurityLevelScreen(::navigateUp) }
        composable<WifiSsidPolicyScreen> { WifiSsidPolicyScreen(::navigateUp) }
        composable<QueryNetworkStats> { NetworkStatsScreen(::navigateUp, ::navigate) }
        composable<NetworkStatsViewer>(mapOf(serializableNavTypePair<List<NetworkStatsViewer.Data>>())) {
            NetworkStatsViewerScreen(it.toRoute(), ::navigateUp)
        }
        composable<PrivateDns> { PrivateDnsScreen(::navigateUp) }
        composable<AlwaysOnVpnPackage> { AlwaysOnVpnPackageScreen(::navigateUp) }
        composable<RecommendedGlobalProxy> { RecommendedGlobalProxyScreen(::navigateUp) }
        composable<NetworkLogging> { NetworkLoggingScreen(::navigateUp) }
        composable<WifiAuthKeypair> { WifiAuthKeypairScreen(::navigateUp) }
        composable<PreferentialNetworkService> { PreferentialNetworkServiceScreen(::navigateUp, ::navigate) }
        composable<AddPreferentialNetworkServiceConfig> { AddPreferentialNetworkServiceConfigScreen(it.toRoute(), ::navigateUp) }
        composable<OverrideApn> { OverrideApnScreen(::navigateUp) { navController.navigate(AddApnSetting, it) } }
        composable<AddApnSetting> { AddApnSettingScreen(it.arguments?.getParcelable("setting"), ::navigateUp) }

        composable<WorkProfile> { WorkProfileScreen(::navigateUp, ::navigate) }
        composable<OrganizationOwnedProfile> { OrganizationOwnedProfileScreen(::navigateUp) }
        composable<CreateWorkProfile> { CreateWorkProfileScreen(::navigateUp) }
        composable<SuspendPersonalApp> { SuspendPersonalAppScreen(::navigateUp) }
        composable<CrossProfileIntentFilter> { CrossProfileIntentFilterScreen(::navigateUp) }
        composable<DeleteWorkProfile> { DeleteWorkProfileScreen(::navigateUp) }

        composable<ApplicationsList> {
            AppChooserScreen(it.toRoute(), { dest ->
                if(dest == null) navigateUp() else navigate(ApplicationDetails(dest))
            }, {
                SP.applicationsListView = false
                navController.navigate(ApplicationsFeatures) {
                    popUpTo(Home)
                }
            })
        }
        composable<ApplicationsFeatures> {
            ApplicationsFeaturesScreen(::navigateUp, ::navigate) {
                SP.applicationsListView = true
                navController.navigate(ApplicationsList(true)) {
                    popUpTo(Home)
                }
            }
        }
        composable<ApplicationDetails> { ApplicationDetailsScreen(it.toRoute(), ::navigateUp, ::navigate) }
        composable<Suspend> { SuspendScreen(::navigateUp) }
        composable<Hide> { HideScreen(::navigateUp) }
        composable<BlockUninstall> { BlockUninstallScreen(::navigateUp) }
        composable<DisableUserControl> { DisableUserControlScreen(::navigateUp) }
        composable<PermissionsManager> { PermissionsManagerScreen(::navigateUp, it.toRoute()) }
        composable<DisableMeteredData> { DisableMeteredDataScreen(::navigateUp) }
        composable<ClearAppStorage> { ClearAppStorageScreen(::navigateUp) }
        composable<UninstallApp> { UninstallAppScreen(::navigateUp) }
        composable<KeepUninstalledPackages> { KeepUninstalledPackagesScreen(::navigateUp) }
        composable<InstallExistingApp> { InstallExistingAppScreen(::navigateUp) }
        composable<CrossProfilePackages> { CrossProfilePackagesScreen(::navigateUp) }
        composable<CrossProfileWidgetProviders> { CrossProfileWidgetProvidersScreen(::navigateUp) }
        composable<CredentialManagerPolicy> { CredentialManagerPolicyScreen(::navigateUp) }
        composable<PermittedAccessibilityServices> { PermittedAccessibilityServicesScreen(::navigateUp) }
        composable<PermittedInputMethods> { PermittedInputMethodsScreen(::navigateUp) }
        composable<EnableSystemApp> { EnableSystemAppScreen(::navigateUp) }
        composable<SetDefaultDialer> { SetDefaultDialerScreen(::navigateUp) }

        composable<UserRestriction> {
            UserRestrictionScreen(::navigateUp) {
                navigate(it)
            }
        }
        composable<UserRestrictionEditor> {
            UserRestrictionEditorScreen(::navigateUp)
        }
        composable<UserRestrictionOptions>(mapOf(serializableNavTypePair<List<Restriction>>())) {
            UserRestrictionOptionsScreen(it.toRoute(), ::navigateUp)
        }

        composable<Users> { UsersScreen(::navigateUp, ::navigate) }
        composable<UserInfo> { UserInfoScreen(::navigateUp) }
        composable<UsersOptions> { UsersOptionsScreen(::navigateUp) }
        composable<UserOperation> { UserOperationScreen(::navigateUp) }
        composable<CreateUser> { CreateUserScreen(::navigateUp) }
        composable<ChangeUsername> { ChangeUsernameScreen(::navigateUp) }
        composable<UserSessionMessage> { UserSessionMessageScreen(::navigateUp) }
        composable<AffiliationId> { AffiliationIdScreen(::navigateUp) }

        composable<Password> { PasswordScreen(::navigateUp, ::navigate) }
        composable<PasswordInfo> { PasswordInfoScreen(::navigateUp) }
        composable<ResetPasswordToken> { ResetPasswordTokenScreen(::navigateUp) }
        composable<ResetPassword> { ResetPasswordScreen(::navigateUp) }
        composable<RequiredPasswordComplexity> { RequiredPasswordComplexityScreen(::navigateUp) }
        composable<KeyguardDisabledFeatures> { KeyguardDisabledFeaturesScreen(::navigateUp) }
        composable<RequiredPasswordQuality> { RequiredPasswordQualityScreen(::navigateUp) }

        composable<Settings> { SettingsScreen(::navigateUp, ::navigate) }
        composable<SettingsOptions> { SettingsOptionsScreen(::navigateUp) }
        composable<Appearance> {
            val theme by vm.theme.collectAsStateWithLifecycle()
            AppearanceScreen(::navigateUp, theme, vm::changeTheme)
        }
        composable<AppLockSettings> { AppLockSettingsScreen(::navigateUp) }
        composable<ApiSettings> { ApiSettings(::navigateUp) }
        composable<Notifications> { NotificationsScreen(::navigateUp) }
	composable<LanguageScreen> { LanguageScreen(::navigateUp) }
        composable<About> { AboutScreen(::navigateUp) }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (
                (event == Lifecycle.Event.ON_CREATE && !SP.lockPasswordHash.isNullOrEmpty()) ||
                (event == Lifecycle.Event.ON_RESUME && SP.lockWhenLeaving)
            ) {
                onLock()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    LaunchedEffect(Unit) {
        val profileNotActivated = !SP.managedProfileActivated && Privilege.status.value.work
        if(profileNotActivated) {
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
        contentWindowInsets = WindowInsets.ime
    ) {
        Column(Modifier.fillMaxSize().padding(it).verticalScroll(rememberScrollState())) {
            if(privilege.device || privilege.profile) {
                HomePageItem(R.string.system, R.drawable.android_fill0) { onNavigate(SystemManager) }
                HomePageItem(R.string.network, R.drawable.wifi_fill0) { onNavigate(Network) }
            }
            if(privilege.work) {
                HomePageItem(R.string.work_profile, R.drawable.work_fill0) {
                    onNavigate(WorkProfile)
                }
            }
            if(privilege.device || privilege.profile) {
                HomePageItem(R.string.applications, R.drawable.apps_fill0) {
                    onNavigate(if(SP.applicationsListView) ApplicationsList(true) else ApplicationsFeatures)
                }
                if(VERSION.SDK_INT >= 24) {
                    HomePageItem(R.string.user_restriction, R.drawable.person_off) { onNavigate(UserRestriction) }
                }
                HomePageItem(R.string.users,R.drawable.manage_accounts_fill0) { onNavigate(Users) }
                HomePageItem(R.string.password_and_keyguard, R.drawable.password_fill0) { onNavigate(Password) }
            }
            Spacer(Modifier.padding(vertical = 20.dp))
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
