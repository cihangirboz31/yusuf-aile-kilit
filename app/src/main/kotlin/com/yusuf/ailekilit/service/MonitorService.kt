package com.yusuf.ailekilit.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import com.yusuf.ailekilit.R
import com.yusuf.ailekilit.activity.DashboardActivity
import com.yusuf.ailekilit.activity.LockScreenActivity
import com.yusuf.ailekilit.data.PrefsManager
import com.yusuf.ailekilit.util.UsageStatsHelper
import kotlinx.coroutines.*

class MonitorService : Service() {

    companion object {
        const val CHANNEL_ID = "YusufAileKilitChannel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_UNLOCK = "com.yusuf.ailekilit.ACTION_UNLOCK"
        const val ACTION_STOP = "com.yusuf.ailekilit.ACTION_STOP"

        fun start(context: Context) {
            val intent = Intent(context, MonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, MonitorService::class.java)
            intent.action = ACTION_STOP
            context.startService(intent)
        }
    }

    private lateinit var prefs: PrefsManager
    private var monitorJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        prefs = PrefsManager(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("İzleme aktif..."))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent?.action == ACTION_UNLOCK) {
            prefs.isLocked = false
        }
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            while (isActive) {
                try {
                    checkDailyReset()
                    if (!prefs.serviceEnabled) {
                        delay(5000)
                        continue
                    }
                    if (UsageStatsHelper.hasUsageStatsPermission(this@MonitorService)) {
                        val foregroundPkg = UsageStatsHelper.getForegroundPackage(this@MonitorService)
                        val isYoutube = foregroundPkg != null && UsageStatsHelper.YOUTUBE_PACKAGES.contains(foregroundPkg)
                        if (isYoutube) {
                            val used = UsageStatsHelper.getTodayUsageSeconds(this@MonitorService)
                            prefs.usedSecondsToday = used
                            val limit = prefs.dailyLimitMinutes * 60L
                            if (used >= limit || prefs.isLocked) {
                                prefs.isLocked = true
                                showLockScreen()
                            }
                        }
                        updateNotification()
                    }
                } catch (e: Exception) {
                    // Hata sessizce geçilir, servis devam eder
                }
                delay(2000)
            }
        }
    }

    private fun checkDailyReset() {
        val today = UsageStatsHelper.getTodayDateString()
        if (prefs.lastResetDate != today) {
            prefs.resetDailyUsage()
            prefs.lastResetDate = today
        }
    }

    private fun showLockScreen() {
        val intent = Intent(this, LockScreenActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun updateNotification() {
        val remaining = prefs.remainingSeconds
        val minutes = remaining / 60
        val seconds = remaining % 60
        val text = if (prefs.isLocked) {
            "🔒 YouTube KİLİTLİ"
        } else {
            "Kalan süre: ${minutes}dk ${seconds}sn"
        }
        val notification = buildNotification(text)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(text: String): Notification {
        val intent = Intent(this, DashboardActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Yusuf Aile Kilit")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentIntent(pi)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aile Kilit İzleme",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "YouTube kullanım süresini izler"
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        monitorJob?.cancel()
        serviceScope.cancel()
        // Servis kapanırsa 3 saniye içinde yeniden başlat
        val restartIntent = Intent(this, MonitorService::class.java)
        val pi = PendingIntent.getService(
            this, 1, restartIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 3000, pi)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val restartIntent = Intent(this, MonitorService::class.java)
        val pi = PendingIntent.getService(
            this, 1, restartIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 3000, pi)
    }
}
