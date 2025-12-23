package dev.mr2.dpc

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.Serializable
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.serialization.KSerializer
import kotlin.reflect.typeOf
import android.content.pm.PackageManager
import dev.mr2.dpc.SP
import kotlin.io.encoding.Base64

var CJK = true

fun uriToStream(
    context: Context,
    uri: Uri,
    operation: (stream: InputStream)->Unit
){
    try {
        context.contentResolver.openInputStream(uri)?.use {
            operation(it)
        }
    }
    catch (_: FileNotFoundException) { context.popToast(R.string.file_not_exist) }
    catch (_: IOException) { context.popToast(R.string.io_exception) }
}

fun writeClipBoard(context: Context, string: String):Boolean{
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    try {
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", string))
    } catch (_:Exception) {
        return false
    }
    return true
}

fun formatFileSize(bytes: Long): String {
    val kb = 1024
    val mb = kb * 1024
    val gb = mb * 1024
    return when {
        bytes >= gb -> String.format(Locale.US, "%.2f GB", bytes / gb.toDouble())
        bytes >= mb -> String.format(Locale.US, "%.2f MB", bytes / mb.toDouble())
        bytes >= kb -> String.format(Locale.US, "%.2f KB", bytes / kb.toDouble())
        else -> "$bytes bytes"
    }
}

val Boolean.yesOrNo
    @StringRes get() = if (this) R.string.yes else R.string.no

fun formatDate(ms: Long): String {
    return formatDate(Date(ms))
}
fun formatDate(date: Date): String {
    return SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(date)
}

fun Context.showOperationResultToast(success: Boolean) {
    popToast(if (success) R.string.success else R.string.failed)
}

const val APK_MIME = "application/vnd.android.package-archive"

fun exportLogs(context: Context, uri: Uri) {
    context.contentResolver.openOutputStream(uri)?.use { output ->
        val proc = Runtime.getRuntime().exec("logcat -d")
        proc.inputStream.copyTo(output)
        if (Build.VERSION.SDK_INT >= 26) proc.waitFor(2L, TimeUnit.SECONDS)
        else proc.waitFor()
        context.showOperationResultToast(proc.exitValue() == 0)
    }
}

val HorizontalPadding = 16.dp
val BottomPadding = 60.dp

@OptIn(ExperimentalStdlibApi::class)
fun String.hash(): String {
    val md = MessageDigest.getInstance("SHA-512")
    return md.digest(this.encodeToByteArray()).toHexString()
}

val MyAdminComponent = ComponentName.unflattenFromString("dev.mr2.dpc/.Receiver")!!

@OptIn(ExperimentalStdlibApi::class)
fun getPackageSignature(info: PackageInfo): String? {
    val signatures = if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else info.signatures
    return signatures?.firstOrNull()?.toByteArray()
        ?.let { MessageDigest.getInstance("SHA-256").digest(it) }?.toHexString()
}

fun Context.popToast(resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}

fun Context.popToast(str: String) {
    Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
}

fun Context.reply(name: String, data: Any, forceSplitReply: Boolean = false): Intent? {
    val intent = when (data) {
        is Intent -> {
            data.setAction("dev.mr2.dpc.api.API_REPLY")
            data
        }
        else -> {
            val newIntent = Intent("dev.mr2.dpc.api.API_REPLY")
            val extraKey = if (SP.sharedApiReply && !forceSplitReply) "dev.mr2.dpc.api.reply.SINGLE_REPLY" else "dev.mr2.dpc.api.reply.$name"

            try {
                when (data) {
                    is String -> newIntent.putExtra(extraKey, data)
                    is Int -> newIntent.putExtra(extraKey, data)
                    is Long -> newIntent.putExtra(extraKey, data)
                    is Float -> newIntent.putExtra(extraKey, data)
                    is Double -> newIntent.putExtra(extraKey, data)
                    is Boolean -> newIntent.putExtra(extraKey, data)
                    is Char -> newIntent.putExtra(extraKey, data)
                    is Byte -> newIntent.putExtra(extraKey, data)
                    is Short -> newIntent.putExtra(extraKey, data)
                    is CharSequence -> newIntent.putExtra(extraKey, data)
                    is Serializable -> newIntent.putExtra(extraKey, data)
                    is Parcelable -> newIntent.putExtra(extraKey, data)
                    is Array<*> -> when {
                        data.isArrayOf<String>() -> newIntent.putExtra(extraKey, data)
                        data.isArrayOf<CharSequence>() -> newIntent.putExtra(extraKey, data)
                        data.isArrayOf<Parcelable>() -> newIntent.putExtra(extraKey, data)
                        else -> {
                            return null
                        }
                    }
                    is IntArray -> newIntent.putExtra(extraKey, data)
                    is LongArray -> newIntent.putExtra(extraKey, data)
                    is FloatArray -> newIntent.putExtra(extraKey, data)
                    is DoubleArray -> newIntent.putExtra(extraKey, data)
                    is BooleanArray -> newIntent.putExtra(extraKey, data)
                    is ByteArray -> newIntent.putExtra(extraKey, data)
                    is ShortArray -> newIntent.putExtra(extraKey, data)
                    is CharArray -> newIntent.putExtra(extraKey, data)
                    is Bundle -> newIntent.putExtra(extraKey, data)
                    else -> {
                        return null
                    }
                }
                newIntent
            } catch (e: Exception) {
                return null
            }
        }
    }

    if (SP.sharedApiReply && !forceSplitReply) intent.putExtra("dev.mr2.extra.EXTRA_OTHER", "dev.mr2.dpc.api.reply.$name")

    return intent
}

var Context.isLauncherVisible: Boolean
    get() {
        val componentName = ComponentName(this, "dev.mr2.dpc.LauncherActivity")
        return packageManager.getComponentEnabledSetting(componentName) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    }
    set(value) {
        val componentName = ComponentName(this, "dev.mr2.dpc.LauncherActivity")
        packageManager.setComponentEnabledSetting(
            componentName,
            if (value) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

class SerializableSaver<T>(val serializer: KSerializer<T>) : Saver<T, String> {
    override fun restore(value: String): T? {
        return Json.decodeFromString(serializer, value)
    }
    override fun SaverScope.save(value: T): String {
        return Json.encodeToString(serializer, value)
    }
}

fun generateBase64Key(length: Int): String {
    val ba = ByteArray(length)
    SecureRandom().nextBytes(ba)
    return Base64.withPadding(Base64.PaddingOption.ABSENT).encode(ba)
}

fun Modifier.clickableTextField(onClick: () -> Unit) =
    pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(pass = PointerEventPass.Initial)
            val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
            if (upEvent != null) onClick()
        }
    }

@Composable
fun adaptiveInsets(): WindowInsets {
    val navbar = WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
    return WindowInsets.ime.union(navbar).union(WindowInsets.displayCutout)
}

fun registerPackageRemovedReceiver(
    ctx: Context, callback: (String) -> Unit
) {
    val br = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            callback(intent.data!!.schemeSpecificPart)
        }
    }
    val filter = IntentFilter()
    filter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
    filter.addDataScheme("package")
    ctx.registerReceiver(br, filter)
}

fun parsePackageNames(input: String) = input.split('\n').filter { it.isNotEmpty() }
