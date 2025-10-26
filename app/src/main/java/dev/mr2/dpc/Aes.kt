package dev.mr2.dpc

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

private fun String.toByteArrayFromHex(): ByteArray {
    val len = length / 2
    return ByteArray(len) { i ->
        substring(2 * i, 2 * i + 2).toInt(16).toByte()
    }
}

fun AesEncrypt(data: String): String {
    if (SP.apiKeyHash.isNullOrEmpty()) return ""
    val key = SP.apiKeyHash!!
    return try {
        val keyBytes = key.toByteArrayFromHex().copyOfRange(0, 32)
        val secretKey = SecretKeySpec(keyBytes, "AES")
        val iv = ByteArray(12).apply { SecureRandom().nextBytes(this) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        }
        val cipherBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        val combined = iv + cipherBytes
        Base64.encodeToString(combined, Base64.NO_WRAP)
    } catch (_: Exception) { "" }
}

fun AesDecrypt(encryptedData: String): String {
    if (SP.apiKeyHash.isNullOrEmpty()) return ""
    val key = SP.apiKeyHash!!
    return try {
        val combined = Base64.decode(encryptedData, Base64.NO_WRAP)
        if (combined.size < 12) return ""
        val iv = combined.copyOfRange(0, 12)
        val cipherBytes = combined.copyOfRange(12, combined.size)
        val keyBytes = key.trim().toByteArrayFromHex().copyOfRange(0, 32)
        val secretKey = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        }
        String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
    } catch (_: Exception) { "" }
}
