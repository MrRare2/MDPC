package dev.mr2.dpc

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.BindException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class TCPService : Service() {
    companion object {
        private const val TAG = "MDPC-TCPService"
        private const val ID = "mdpc_api_tcp"
        private const val CHANNEL = "API TCP Service"
        private const val NOTIF_ID = 672372
        private const val TIMEOUT = 100
        private const val BUF = 65536
    }

    private var server: ServerSocket? = null
    private var pool: ExecutorService? = null
    private val run = AtomicBoolean(true)

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startFgService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        init()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() = super.onDestroy().also { close() }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(
                    NotificationChannel(ID, CHANNEL, NotificationManager.IMPORTANCE_LOW)
                        .apply { description = getString(R.string.api_notif_channel_description) }
                )
        }
    }

    private fun startFgService() = startForeground(
        NOTIF_ID,
        NotificationCompat.Builder(this, ID)
            .setContentTitle(getString(R.string.api_notif_title))
            .setSmallIcon(R.drawable.info_fill0)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    )

    private fun init() {
        pool = Executors.newCachedThreadPool()
        try { server = ServerSocket(SP.apiPort).apply { reuseAddress = true } }
        catch (_: BindException) { return }

        pool?.execute {
            Log.d(TAG, "TCP listening :${SP.apiPort}")
            try {
                while (run.get()) {
                    val s = server?.accept() ?: break
                    s.soTimeout = TIMEOUT
                    pool?.execute { handle(s) }
                }
            } catch (_: Exception) {}
        }
    }

    private fun handle(s: Socket) {
        val addr = "${s.inetAddress?.hostAddress}:${s.port}"
        Log.d(TAG, "connected $addr")
        var i: InputStream? = null
        var o: OutputStream? = null
        var ok = false

        try {
            i = s.getInputStream()
            o = s.getOutputStream()

            val data = ByteArrayOutputStream()
            val buf = ByteArray(BUF)
            var read: Int

            while (true) {
                read = try { i.read(buf) } catch (_: SocketTimeoutException) { -1 }
                if (read == -1) break
                data.write(buf, 0, read)
            }

            if (data.size() > 0) {
                val raw = data.toString(Charsets.UTF_8).trim()
                val json = JSONObject(AesDecrypt(raw))
                val resp = realHandler(this, json).toString()
                o.write(AesEncrypt(resp).toByteArray(Charsets.UTF_8))
                o.flush()
                ok = true
            }
        } catch (_: Throwable) {} finally {
            if (!ok) {
                try {
                    o?.write(AesEncrypt("").toByteArray(Charsets.UTF_8))
                    o?.flush()
                } catch (_: Throwable) {}
            }
            try { s.close() } catch (_: Throwable) {}
            Log.d(TAG, "disconnected $addr ${if (ok) "OK" else "ERR"}")
        }
    }

    private fun close() {
        run.set(false)
        try { server?.close() } catch (_: Throwable) {}
        pool?.shutdownNow()
        stopForeground(true)
        Log.d(TAG, "TCPService closed")
    }
}
