package dev.mr2.dpc

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mr2.dpc.ui.FunctionItem
import dev.mr2.dpc.ui.FullWidthRadioButtonItem
import dev.mr2.dpc.ui.MyScaffold
import dev.mr2.dpc.ui.NavIcon
import dev.mr2.dpc.ui.Notes
import dev.mr2.dpc.ui.SwitchItem
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

@Serializable object Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit) {
    val context = LocalContext.current
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    val exportLogsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) {
        if (it != null) exportLogs(context, it)
    }
    val sb = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var dropdown by remember { mutableStateOf(false) }
    Scaffold(
        Modifier.nestedScroll(sb.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                { Text(stringResource(R.string.settings)) },
                navigationIcon = { NavIcon(onNavigateUp) },
                scrollBehavior = sb,
                actions = {
                    Box {
                        IconButton({ dropdown = true }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenu(dropdown, { dropdown = false }) {
                            DropdownMenuItem(
                                { Text(stringResource(R.string.export_logs)) },
                                {
                                    dropdown = false
                                    val time = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
                                        .format(Date(System.currentTimeMillis()))
                                    exportLogsLauncher.launch("mr2dpc_log_$time")
                                },
                                leadingIcon = {
                                    Icon(painterResource(R.drawable.description_fill0), null)
                                }
                            )
                            DropdownMenuItem(
                                { Text(stringResource(R.string.exit)) },
                                { (context as? Activity)?.finishAffinity() },
                                leadingIcon = { Icon(Icons.Default.Close, null) }
                            )
                        }
                    }
                }
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            FunctionItem(title = R.string.options, icon = R.drawable.tune_fill0) { onNavigate(SettingsOptions) }
            FunctionItem(title = R.string.appearance, icon = R.drawable.format_paint_fill0) { onNavigate(Appearance) }
            FunctionItem(R.string.app_lock, icon = R.drawable.lock_fill0) { onNavigate(AppLockSettings) }
            if (privilege.device || privilege.profile)
                FunctionItem(title = R.string.api, icon = R.drawable.code_fill0) { onNavigate(ApiSettings) }
            if (privilege.device && !privilege.dhizuku)
                FunctionItem(R.string.notifications, icon = R.drawable.notifications_fill0) { onNavigate(Notifications) }
	        FunctionItem(R.string.languages, icon = R.drawable.language_fill0) { onNavigate(LanguageScreen) }
            FunctionItem(title = R.string.about, icon = R.drawable.info_fill0) { onNavigate(About) }
        }
    }
}

@Serializable object SettingsOptions

@Composable
fun SettingsOptionsScreen(
    getDisplayDangerousFeatures: () -> Boolean, getShortcutsEnabled: () -> Boolean, getLauncherVisible: () -> Boolean,
    setDisplayDangerousFeatures: (Boolean) -> Unit, setShortcutsEnabled: (Boolean) -> Unit, setLauncherVisible: (Boolean) -> Unit,
    onNavigateUp: () -> Unit
) {
    var dangerousFeatures by remember { mutableStateOf(getDisplayDangerousFeatures()) }
    var shortcuts by remember { mutableStateOf(getShortcutsEnabled()) }
    var launcherIconVisible by remember { mutableStateOf(getLauncherVisible()) }
    var dialog by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.options, onNavigateUp, 0.dp) {
        SwitchItem(
            R.string.show_dangerous_features, dangerousFeatures, {
                setDisplayDangerousFeatures(it)
                dangerousFeatures = it
            }, R.drawable.warning_fill0
        )
        SwitchItem(
            R.string.shortcuts, shortcuts, {
                setShortcutsEnabled(it)
                shortcuts = it
            }, R.drawable.open_in_new
        )
	    SwitchItem(
	        R.string.show_launcher_icon, launcherIconVisible, {
                if (!it) dialog = 1
                else {
                    setLauncherVisible(true)
                    launcherIconVisible = true
                }
            }, R.drawable.visibility_fill0
	    )

	    if (dialog == 1) {
            AlertDialog(
                onDismissRequest = { dialog = 0 },
                title = { Text(stringResource(R.string.warning)) },
                text = { Text(stringResource(R.string.info_hiding_launcher_icon)) },
                dismissButton = {
                    TextButton(onClick = { dialog = 0 }) {
                    Text(stringResource(R.string.cancel))
                   }
                },
                confirmButton = {
                    TextButton(onClick = {
                        setLauncherVisible(false)
                        launcherIconVisible = false
                        dialog = 0
                    }) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Serializable object Appearance

@Composable
fun AppearanceScreen(
    onNavigateUp: () -> Unit, currentTheme: StateFlow<ThemeSettings>,
    setTheme: (ThemeSettings) -> Unit
) {
    var darkThemeMenu by remember { mutableStateOf(false) }
    var colorSelectorMenu by remember { mutableStateOf(false) }
    val theme by currentTheme.collectAsStateWithLifecycle()
    val darkThemeTextID = when (theme.darkTheme) {
        1 -> R.string.on
        0 -> R.string.off
        else -> R.string.follow_system
    }
    val colorSchemeTextID = when (theme.themeColor) {
	    1 -> R.string.color_blue
	    2 -> R.string.color_red
	    3 -> R.string.color_orange
	    4 -> R.string.color_yellow
	    5 -> R.string.color_pink
	    6 -> R.string.color_purple
	    7 -> R.string.color_green
	    else -> R.string.material_you_color
    }
    MyScaffold(R.string.appearance, onNavigateUp, 0.dp) {
        Box {
            FunctionItem(R.string.color_scheme, stringResource(colorSchemeTextID)) { colorSelectorMenu = true }
            DropdownMenu(
                expanded = colorSelectorMenu, onDismissRequest = { colorSelectorMenu = false },
                offset = DpOffset(x = 25.dp, y = 0.dp)
            ) {
                if (VERSION.SDK_INT >= 31) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.material_you_color)) },
                        onClick = {
                            setTheme(theme.copy(themeColor = 0))
                            colorSelectorMenu = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.color_blue)) },
                    onClick = {
                        setTheme(theme.copy(themeColor = 1))
                        colorSelectorMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.color_red)) },
                    onClick = {
                        setTheme(theme.copy(themeColor = 2))
                        colorSelectorMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.color_orange)) },
                    onClick = {
                        setTheme(theme.copy(themeColor = 3))
                        colorSelectorMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.color_yellow)) },
                    onClick = {
                        setTheme(theme.copy(themeColor = 4))
                        colorSelectorMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.color_pink)) },
                    onClick = {
                        setTheme(theme.copy(themeColor = 5))
                        colorSelectorMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.color_purple)) },
                    onClick = {
                        setTheme(theme.copy(themeColor = 6))
                        colorSelectorMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.color_green)) },
                    onClick = {
                        setTheme(theme.copy(themeColor = 7))
                        colorSelectorMenu = false
                    }
                )
            }
        }
        Box {
            FunctionItem(R.string.dark_theme, stringResource(darkThemeTextID)) { darkThemeMenu = true }
            DropdownMenu(
                expanded = darkThemeMenu, onDismissRequest = { darkThemeMenu = false },
                offset = DpOffset(x = 25.dp, y = 0.dp)
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.follow_system)) },
                    onClick = {
                        setTheme(theme.copy(darkTheme = -1))
                        darkThemeMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.on)) },
                    onClick = {
                        setTheme(theme.copy(darkTheme = 1))
                        darkThemeMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.off)) },
                    onClick = {
                        setTheme(theme.copy(darkTheme = 0))
                        darkThemeMenu = false
                    }
                )
            }
        }
        AnimatedVisibility(theme.darkTheme == 1 || (theme.darkTheme == -1 && isSystemInDarkTheme())) {
            SwitchItem(
                R.string.black_theme, state = theme.blackTheme,
                onCheckedChange = { setTheme(theme.copy(blackTheme = it)) }
            )
        }
    }
}

data class AppLockConfig(
    /** null means no password, empty means password already set */
    val password: String?, val biometrics: Boolean, val whenLeaving: Boolean
)

@Serializable object AppLockSettings

@Composable
fun AppLockSettingsScreen(
    config: AppLockConfig, setConfig: (AppLockConfig) -> Unit,
    onNavigateUp: () -> Unit
) = MyScaffold(R.string.app_lock, onNavigateUp) {
    var context = LocalContext.current
    var password by rememberSaveable { mutableStateOf("") }
    var hidePassword by rememberSaveable { mutableStateOf(true) }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var hidePasswordConfirm by rememberSaveable { mutableStateOf(true) }
    var allowBiometrics by rememberSaveable { mutableStateOf(config.biometrics) }
    var lockWhenLeaving by rememberSaveable { mutableStateOf(config.whenLeaving) }
    var alreadySet by rememberSaveable { mutableStateOf(config.password != null) }
    val isInputLegal = password.length !in 1..3 && (alreadySet || password.isNotBlank())
    val biometricsAllowed = BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    OutlinedTextField(
        password, { password = it }, Modifier.fillMaxWidth().padding(vertical = 4.dp),
        label = { Text(stringResource(R.string.password)) },
        supportingText = { Text(stringResource(if (alreadySet) R.string.leave_empty_to_remain_unchanged else R.string.minimum_length_4)) },
        visualTransformation = if (hidePassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
        trailingIcon = {
            IconButton(onClick = { hidePassword = !hidePassword }) {
                Icon(painter = painterResource(if (hidePassword) R.drawable.visibility_fill0 else R.drawable.visibility_off_fill0), null)
            }
        }
    )
    OutlinedTextField(
        confirmPassword, { confirmPassword = it }, Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.confirm_password)) },
        visualTransformation = if (hidePasswordConfirm) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        trailingIcon = {
            IconButton(onClick = { hidePasswordConfirm = !hidePasswordConfirm }) {
                Icon(painter = painterResource(if (hidePasswordConfirm) R.drawable.visibility_fill0 else R.drawable.visibility_off_fill0), null)
            }
        }
    )
    if (VERSION.SDK_INT >= 28 && biometricsAllowed) Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        Arrangement.SpaceBetween, Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.allow_biometrics))
        Switch(allowBiometrics, { allowBiometrics = it })
    }
    Row(
        Modifier.fillMaxWidth().padding(bottom = 6.dp),
        Arrangement.SpaceBetween, Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.lock_when_leaving))
        Switch(lockWhenLeaving, { lockWhenLeaving = it })
    }
    Button(
        onClick = {
            setConfig(AppLockConfig(password, allowBiometrics, lockWhenLeaving))
            onNavigateUp()
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = isInputLegal && confirmPassword == password
    ) {
        Text(stringResource(if(alreadySet) R.string.update else R.string.set))
    }
    if (alreadySet) FilledTonalButton(
        onClick = {
            setConfig(AppLockConfig(null, false, false))
            onNavigateUp()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.disable))
    }
}

@Serializable object ApiSettings

@Composable
fun ApiSettings(
    getEnabled: () -> Boolean, getSrEnabled: () -> Boolean, setKey: (String) -> Unit, setSrEnabled: (Boolean) -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    var alreadyEnabled by remember { mutableStateOf(getEnabled()) }
    MyScaffold(R.string.api, onNavigateUp) {
        var enabled by remember { mutableStateOf(alreadyEnabled) }
        var sharedReplyEnabled by remember { mutableStateOf(getSrEnabled()) }
        var key by rememberSaveable { mutableStateOf("") }
        var dialog by rememberSaveable { mutableStateOf(0) }
        SwitchItem(R.string.enable, state = enabled, onCheckedChange = {
            if (!it) dialog = 1
            else enabled = true
        }, padding = false)
        if (enabled) {
	        SwitchItem(R.string.api_shared_response, state = sharedReplyEnabled, onCheckedChange = {
                setSrEnabled(it)
                sharedReplyEnabled = it
            }, padding = false)
            OutlinedTextField(
                key, { key = it }, Modifier.fillMaxWidth().padding(bottom = 4.dp),
                label = { Text(stringResource(R.string.api_key)) },
                trailingIcon = {
                    IconButton({ key = generateBase64Key(32) }) {
                        Icon(painterResource(R.drawable.casino_fill0), null)
                    }
                }
            )
        }
        Button(
            onClick = {
                setKey(if (enabled) key else "")
                alreadyEnabled = enabled
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            enabled = !enabled || key.length !in 0..7
        ) {
            Text(stringResource(R.string.apply))
        }
        if (enabled && alreadyEnabled) Notes(R.string.api_key_exist)
        when (dialog) {
            1 -> AlertDialog(
                onDismissRequest = { dialog = 0 },
                title = { Text(stringResource(R.string.warning)) },
                text = { Text(stringResource(R.string.info_api_disable)) },
                dismissButton = {
                    TextButton(onClick = { dialog = 0 }) { Text(stringResource(R.string.cancel)) }
                },
                confirmButton = {
                    TextButton(onClick = {
                        enabled = false
                        dialog = 0
                    }) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Serializable object Notifications

@Composable
fun NotificationsScreen(
    enabledNotifications: StateFlow<List<Int>>, getState: () -> Unit,
    setNotification: (NotificationType, Boolean) -> Unit, onNavigateUp: () -> Unit
) = MyScaffold(R.string.notifications, onNavigateUp, 0.dp) {
    val notifications by enabledNotifications.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        getState()
    }
    NotificationType.entries.filter {
        it.channel == MyNotificationChannel.Events
    }.forEach { type ->
        SwitchItem(type.text, type.id in notifications, { setNotification(type, it) })
    }
}

@Serializable object LanguageScreen

@Composable
fun LanguageScreen(getLanguage: () -> String?, getLanguageRegion: () -> String?, setLanguage: (String?, String?) -> Unit, onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val languages = BuiltInLocales.toLanguages(context)
    var currentLanguage by remember { mutableStateOf(getLanguage()) }
    var currentRegion by remember { mutableStateOf(getLanguageRegion()) }
    var dialog by remember { mutableStateOf(0) }

    MyScaffold(R.string.languages, onNavigateUp, 0.dp) {
	    FullWidthRadioButtonItem(
            stringResource(R.string.follow_system),
	        currentLanguage == "default"
        ) {
            currentLanguage = "default"
	        currentRegion = "default"
        }

	    languages.forEach { lang ->
	        FullWidthRadioButtonItem(
                "${lang.name} [${lang.lang}]",
                currentLanguage == lang.lang && currentRegion == lang.region,
            ) {
                currentLanguage = lang.lang
                currentRegion = lang.region
            }
        }

	    Button(
            onClick = {
                dialog = 1
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = HorizontalPadding),
            enabled = currentLanguage != getLanguage() || currentRegion != getLanguageRegion()
        ) {
            Text(stringResource(R.string.apply))
        }

        when (dialog) {
            1 -> AlertDialog(
                onDismissRequest = { dialog = 0 },
                title = { Text(stringResource(R.string.warning)) },
                text = { Text(stringResource(R.string.info_language_reload)) },
                dismissButton = {
                    TextButton(onClick = { dialog = 0 }) { Text(stringResource(R.string.no)) }
                },
                confirmButton = {
                    TextButton(onClick = {
                        dialog = 0
                        setLanguage(currentLanguage, currentRegion)

                        context.startActivity(Intent().apply {
                            component = ComponentName("dev.mr2.dpc", "dev.mr2.dpc.MainActivity")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        activity?.finish()
                    }) { Text(stringResource(R.string.yes)) }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Serializable object About

@Composable
fun AboutScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val pkgInfo = context.packageManager.getPackageInfo(context.packageName,0)
    val verCode = pkgInfo.versionCode
    val verName = pkgInfo.versionName
    MyScaffold(R.string.about, onNavigateUp, 0.dp) {
        Text(text = stringResource(R.string.app_name)+" v$verName ($verCode)", modifier = Modifier.padding(start = 16.dp))
        Spacer(Modifier.padding(vertical = 5.dp))
        FunctionItem(R.string.project_homepage, "GitHub | BinTianqi/OwnDroid", R.drawable.open_in_new) { shareLink(context, "https://github.com/BinTianqi/OwnDroid") }
        FunctionItem(R.string.project_fork_homepage, "GitHub | MrRare2/MDPC", R.drawable.open_in_new) { shareLink(context, "https://github.com/MrRare2/MDPC") }
    }
}

fun shareLink(inputContext: Context, link: String) {
    val uri = link.toUri()
    val intent = Intent(Intent.ACTION_VIEW, uri)
    inputContext.startActivity(Intent.createChooser(intent, "Open in browser"), null)
}
