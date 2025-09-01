package dev.mr2.dpc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt

suspend fun hashPassword(password: String?): String =
    withContext(Dispatchers.Default) {
        BCrypt.hashpw(password ?: "", BCrypt.gensalt())
    }

suspend fun verifyPassword(password: String?, hashed: String?): Boolean =
    withContext(Dispatchers.Default) {
        if (password == null || hashed == null) return@withContext false
        BCrypt.checkpw(password, hashed)
    }
