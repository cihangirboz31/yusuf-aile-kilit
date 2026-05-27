package com.yusuf.ailekilit.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.yusuf.ailekilit.activity.LockScreenActivity
import com.yusuf.ailekilit.data.PrefsManager
import com.yusuf.ailekilit.util.UsageStatsHelper

class AppMonitorAccessibilityService : AccessibilityService() {

    private lateinit var prefs: PrefsManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = PrefsManager(this)
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.notificationTimeout = 100
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        if (UsageStatsHelper.YOUTUBE_PACKAGES.contains(packageName)) {
            if (prefs.isLocked) {
                showLockScreen()
                return
            }
            val used = prefs.usedSecondsToday
            val limit = prefs.dailyLimitMinutes * 60L
            if (used >= limit) {
                prefs.isLocked = true
                showLockScreen()
            }
        }
    }

    private fun showLockScreen() {
        val intent = Intent(this, LockScreenActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun onInterrupt() {}
}
