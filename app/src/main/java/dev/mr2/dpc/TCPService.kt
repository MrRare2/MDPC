package dev.mr2.dpc

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.BindException
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TCPService : Service() {
    companion object {
        private const val TAG = "MDPC-TCPService"
        private const val ID = "mdpc_api_tcp"
        private const val CHANNEL = "API TCP Service"
        private const val NOTIF_ID = 672372
    }

    private var server: ServerSocket? = null
    private var client: Socket? = null
    private var executor: ExecutorService? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        val pm = getSystemService(PowerManager::class.java)
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "$TAG:wakeLock").apply { acquire() }
        createChannel()
        startFgService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = init().let { START_STICKY }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() = super.onDestroy().also { close() }

    private fun createChannel() = Build.VERSION.SDK_INT.takeIf { it >= Build.VERSION_CODES.O }?.let {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(
                ID, CHANNEL, NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.api_notif_channel_description)
            })
        }

    private fun startFgService() = startForeground(NOTIF_ID, NotificationCompat.Builder(this, ID)
        .setContentTitle(getString(R.string.api_notif_title))
        .setSmallIcon(R.drawable.info_fill0)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()
    )

    fun init() {
        executor = Executors.newSingleThreadExecutor()
        try { server = ServerSocket(SP.apiPort).apply { reuseAddress = true } }
        catch (_: BindException) { return }

        executor?.execute {
            Log.d(TAG, "TCP Server started on port ${SP.apiPort}")
            try {
                while (!Thread.currentThread().isInterrupted) {
                    client?.close()
                    client = server?.accept()
                    Log.d(TAG, "connected: ${client?.inetAddress?.hostAddress}:${client?.port}")
                    handle(client!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun send(data: String) {
        if (client != null) {
            try {
                client!!.getOutputStream().write((data).toByteArray())
            } catch (e: Exception) {
                Log.e(TAG, "${e.message}")
                e.printStackTrace()
            } finally {
                try { client?.close() } catch (_: Exception) {}
                client = null
            }
        }
}

    fun handle(socket: Socket) {
        var reader: BufferedReader? = null
        var response: String = ""
        try {
            reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val buffer = CharArray(16384)
            val bytesRead = reader.read(buffer, 0, 16384)
            if (bytesRead > 0) {
                val data = AesDecrypt(String(buffer, 0, bytesRead))
                var json: JSONObject? = null

                try { json = JSONObject(data) }
                catch (e: Exception) {
                    e.printStackTrace()
                    return
                }
                if (json == null) {
                    Log.e(TAG, "data is null")
                    return
                }
                response = realHandler(this, json).toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            response = JSONObject().apply { put("result", JSONObject.NULL); put("error", JSONArray(listOf(e::class.java.name, e.message, Log.getStackTraceString(e)))) }.toString()
        } finally {
            try {
                send(AesEncrypt(response))
                reader?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun close() {
        try {
            wakeLock?.release()
            client?.close().let { client = null }
            server?.close()
            executor?.shutdownNow()
            stopForeground(true)
            Log.d(TAG, "TCPService closed")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
