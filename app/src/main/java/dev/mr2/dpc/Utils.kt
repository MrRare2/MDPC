package dev.mr2.dpc

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
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
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.reflect.typeOf
import android.content.pm.PackageManager
import java.util.*

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
    catch(_: FileNotFoundException) { context.popToast(R.string.file_not_exist) }
    catch(_: IOException) { context.popToast(R.string.io_exception) }
}

fun writeClipBoard(context: Context, string: String):Boolean{
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    try {
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", string))
    } catch(_:Exception) {
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
    @StringRes get() = if(this) R.string.yes else R.string.no

@RequiresApi(26)
fun parseTimestamp(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

fun parseDate(date: Date): String = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(date)

val Long.humanReadableDate: String
    get() = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(this))

fun formatDate(pattern: String, value: Long): String
    = SimpleDateFormat(pattern, Locale.getDefault()).format(Date(value))

fun Context.showOperationResultToast(success: Boolean) {
    popToast(if(success) R.string.success else R.string.failed)
}

const val APK_MIME = "application/vnd.android.package-archive"

inline fun <reified T> serializableNavTypePair() =
    typeOf<T>() to object : NavType<T>(false) {
    override fun get(bundle: Bundle, key: String): T? =
        bundle.getString(key)?.let { parseValue(it) }
    override fun put(bundle: Bundle, key: String, value: T) =
        bundle.putString(key, serializeAsValue(value))
    override fun parseValue(value: String): T =
        Json.decodeFromString(value)
    override fun serializeAsValue(value: T): String =
        Json.encodeToString(value)
}

class ChoosePackageContract: ActivityResultContract<Nothing?, String?>() {
    override fun createIntent(context: Context, input: Nothing?): Intent =
        Intent(context, PackageChooserActivity::class.java)
    override fun parseResult(resultCode: Int, intent: Intent?): String? =
        intent?.getStringExtra("package")
}

fun exportLogs(context: Context, uri: Uri) {
    context.contentResolver.openOutputStream(uri)?.use { output ->
        val proc = Runtime.getRuntime().exec("logcat -d")
        proc.inputStream.copyTo(output)
        if(Build.VERSION.SDK_INT >= 26) proc.waitFor(2L, TimeUnit.SECONDS)
        else proc.waitFor()
        context.showOperationResultToast(proc.exitValue() == 0)
    }
}

fun <T> NavHostController.navigate(route: T, args: Bundle) {
    navigate(graph.findNode(route)!!.id, args)
}

val HorizontalPadding = 16.dp

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

fun Context.reply(name: String, data: Any): Boolean {
    val intent = when (data) {
        is Intent -> {
            data.setAction("dev.mr2.dpc.api.API_REPLY")
            data
        }
        else -> {
            val newIntent = Intent("dev.mr2.dpc.api.API_REPLY")
            val extraKey = "dev.mr2.dpc.api.reply.$name"

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
                            return false
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
                        return false
                    }
                }
                newIntent
            } catch (e: Exception) {
                return false
            }
        }
    }

    return try {
        this.sendBroadcast(intent)
        true
    } catch (e: Exception) {
        false
    }
}

fun Context.isVerifiedSignature(): Boolean {
    try {
        val a = byteArrayOf(22, 15, -10, 115, 104, -49, 13, -67)
        val b = byteArrayOf(98, 96, 96, 75, 93, -22, -2, 115)
        val c = byteArrayOf(-127, 126, -57, -55, -18, -92, -42, -60)
        val d = byteArrayOf(83, 97, 93, -53, 105, 8, -59, -19)
        val key: Int = ((0x7 * 0xD) - 0x1) and 0xFF

        val obf = ByteArray(a.size + b.size + c.size + d.size)
        var p = 0
        for (arr in listOf(a, b, c, d)) {
            for (x in arr) obf[p++] = x
        }

        val expected = ByteArray(obf.size)
        for (i in obf.indices) {
            val ob = obf[i].toInt() and 0xFF
            expected[i] = ((ob xor key) and 0xFF).toByte()
        }

        val pm = this.packageManager
        val pkg = this.packageName
        val pkgInfo = pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES)
        val sigs = pkgInfo.signatures ?: return false
        if (sigs.isEmpty()) return false
        val sigBytes = sigs[0].toByteArray()

        val shaChars = intArrayOf(0x53, 0x48, 0x41, 0x2D, 0x32, 0x35, 0x36)
        val algo = String(shaChars.map { it.toChar() }.toCharArray())

        val md = MessageDigest.getInstance(algo)
        md.update(sigBytes)
        val actual = md.digest()

        if (actual.size != expected.size) return false
        var ok = true
        var noise = 0
        for (i in expected.indices) {
            val aByte = actual[i].toInt() and 0xFF
            val eByte = expected[i].toInt() and 0xFF
            if (aByte != eByte) ok = false

            noise = (noise + ((aByte xor eByte) * (i + 7) % 99)) and 0x7FFF
        }

        if (noise != 0) {
            ok = ok && (noise != -1)
        }
        return ok
    } catch (t: Throwable) {
        return false
    }
}
