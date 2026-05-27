package com.yusuf.ailekilit.data

import android.content.Context
import android.content.SharedPreferences

class PrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("yusuf_aile_kilit_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_PIN = "parent_pin"
        const val KEY_PIN_SET = "pin_is_set"
        const val KEY_DAILY_LIMIT_MINUTES = "daily_limit_minutes"
        const val KEY_USED_SECONDS_TODAY = "used_seconds_today"
        const val KEY_LAST_RESET_DATE = "last_reset_date"
        const val KEY_IS_LOCKED = "is_locked"
        const val KEY_LOCK_ACTIVE = "lock_active"
        const val KEY_TV_IP = "tv_ip_address"
        const val KEY_TV_TYPE = "tv_type"
        const val KEY_TV_PAIRED = "tv_paired"
        const val KEY_TV_PORT = "tv_port"
        const val KEY_SERVICE_ENABLED = "service_enabled"
        const val KEY_BATTERY_OPT_SHOWN = "battery_opt_shown"

        const val TV_TYPE_ARCELIK = "arcelik"
        const val TV_TYPE_TURKCELL = "turkcell"
        const val TV_TYPE_ANDROID = "android_tv"
    }

    var pin: String
        get() = prefs.getString(KEY_PIN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PIN, value).apply()

    var isPinSet: Boolean
        get() = prefs.getBoolean(KEY_PIN_SET, false)
        set(value) = prefs.edit().putBoolean(KEY_PIN_SET, value).apply()

    var dailyLimitMinutes: Int
        get() = prefs.getInt(KEY_DAILY_LIMIT_MINUTES, 60)
        set(value) = prefs.edit().putInt(KEY_DAILY_LIMIT_MINUTES, value).apply()

    var usedSecondsToday: Long
        get() = prefs.getLong(KEY_USED_SECONDS_TODAY, 0L)
        set(value) = prefs.edit().putLong(KEY_USED_SECONDS_TODAY, value).apply()

    var lastResetDate: String
        get() = prefs.getString(KEY_LAST_RESET_DATE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_RESET_DATE, value).apply()

    var isLocked: Boolean
        get() = prefs.getBoolean(KEY_IS_LOCKED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOCKED, value).apply()

    var lockActive: Boolean
        get() = prefs.getBoolean(KEY_LOCK_ACTIVE, true)
        set(value) = prefs.edit().putBoolean(KEY_LOCK_ACTIVE, value).apply()

    var tvIpAddress: String
        get() = prefs.getString(KEY_TV_IP, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TV_IP, value).apply()

    var tvType: String
        get() = prefs.getString(KEY_TV_TYPE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TV_TYPE, value).apply()

    var tvPaired: Boolean
        get() = prefs.getBoolean(KEY_TV_PAIRED, false)
        set(value) = prefs.edit().putBoolean(KEY_TV_PAIRED, value).apply()

    var tvPort: Int
        get() = prefs.getInt(KEY_TV_PORT, 6466)
        set(value) = prefs.edit().putInt(KEY_TV_PORT, value).apply()

    var serviceEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SERVICE_ENABLED, value).apply()

    val remainingSeconds: Long
        get() {
            val limitSeconds = dailyLimitMinutes * 60L
            val used = usedSecondsToday
            return if (limitSeconds > used) limitSeconds - used else 0L
        }

    fun addBonusSeconds(seconds: Long) {
        val current = usedSecondsToday
        val newUsed = (current - seconds).coerceAtLeast(0L)
        usedSecondsToday = newUsed
        if (newUsed < dailyLimitMinutes * 60L) {
            isLocked = false
        }
    }

    fun resetDailyUsage() {
        usedSecondsToday = 0L
        isLocked = false
    }

    fun verifyPin(inputPin: String): Boolean {
        return pin == inputPin && inputPin.isNotEmpty()
    }
}
