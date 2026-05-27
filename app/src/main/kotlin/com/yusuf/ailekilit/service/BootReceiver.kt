package com.yusuf.ailekilit.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.yusuf.ailekilit.data.PrefsManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val prefs = PrefsManager(context)
            if (prefs.isPinSet && prefs.serviceEnabled) {
                val today = com.yusuf.ailekilit.util.UsageStatsHelper.getTodayDateString()
                if (prefs.lastResetDate != today) {
                    prefs.resetDailyUsage()
                    prefs.lastResetDate = today
                }
                val serviceIntent = Intent(context, MonitorService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
