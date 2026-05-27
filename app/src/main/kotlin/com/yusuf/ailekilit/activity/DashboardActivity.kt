package com.yusuf.ailekilit.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yusuf.ailekilit.data.PrefsManager
import com.yusuf.ailekilit.databinding.ActivityDashboardBinding
import com.yusuf.ailekilit.service.MonitorService
import com.yusuf.ailekilit.util.UsageStatsHelper

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var prefs: PrefsManager
    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            updateUI()
            handler.postDelayed(this, 3000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PrefsManager(this)

        setupButtons()
        MonitorService.start(this)
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    private fun updateUI() {
        val usedSec = if (UsageStatsHelper.hasUsageStatsPermission(this)) {
            val fromUsm = UsageStatsHelper.getTodayUsageSeconds(this)
            if (fromUsm > 0) {
                prefs.usedSecondsToday = fromUsm
                fromUsm
            } else prefs.usedSecondsToday
        } else prefs.usedSecondsToday

        val limitSec = prefs.dailyLimitMinutes * 60L
        val remainSec = if (limitSec > usedSec) limitSec - usedSec else 0L

        val usedMin = usedSec / 60
        val usedSecR = usedSec % 60
        val remMin = remainSec / 60
        val remSec = remainSec % 60

        binding.usedTimeText.text = "Kullanılan: ${usedMin}dk ${usedSecR}sn"
        binding.remainingTimeText.text = "Kalan: ${remMin}dk ${remSec}sn"
        binding.limitText.text = "Günlük limit: ${prefs.dailyLimitMinutes} dakika"

        val isLocked = prefs.isLocked
        binding.lockStatusText.text = if (isLocked) "🔒 YouTube KİLİTLİ" else "✅ YouTube Serbest"
        binding.lockStatusText.setTextColor(
            if (isLocked) getColor(android.R.color.holo_red_light)
            else getColor(android.R.color.holo_green_light)
        )

        val tvPaired = prefs.tvPaired
        binding.tvStatusText.text = if (tvPaired) "📺 TV Bağlı: ${prefs.tvIpAddress}" else "📺 TV Bağlı Değil"

        val hasUsage = UsageStatsHelper.hasUsageStatsPermission(this)
        val hasOverlay = android.provider.Settings.canDrawOverlays(this)
        binding.permStatusText.text = buildString {
            append(if (hasUsage) "✅" else "❌")
            append(" Kullanım İzni  ")
            append(if (hasOverlay) "✅" else "❌")
            append(" Overlay İzni")
        }
    }

    private fun setupButtons() {
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnTvSetup.setOnClickListener {
            startActivity(Intent(this, TvSetupActivity::class.java))
        }

        binding.btnReward.setOnClickListener {
            startActivity(Intent(this, RewardActivity::class.java))
        }

        binding.btnRouterGuide.setOnClickListener {
            startActivity(Intent(this, RouterGuideActivity::class.java))
        }

        binding.btnLockNow.setOnClickListener {
            prefs.isLocked = true
            Toast.makeText(this, "YouTube şimdi kilitlendi!", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        binding.btnUnlock.setOnClickListener {
            showPinDialog { success ->
                if (success) {
                    prefs.isLocked = false
                    Toast.makeText(this, "Kilit kaldırıldı.", Toast.LENGTH_SHORT).show()
                    updateUI()
                }
            }
        }

        binding.btnResetTime.setOnClickListener {
            showPinDialog { success ->
                if (success) {
                    showConfirmDialog("Günlük süreyi sıfırla?", "Bugünkü kullanım sayacı sıfırlanacak.") {
                        prefs.resetDailyUsage()
                        Toast.makeText(this, "Süre sıfırlandı.", Toast.LENGTH_SHORT).show()
                        updateUI()
                    }
                }
            }
        }

        binding.btnPermissions.setOnClickListener {
            startActivity(Intent(this, PermissionsActivity::class.java))
        }
    }

    private fun showPinDialog(onResult: (Boolean) -> Unit) {
        val dialogView = layoutInflater.inflate(android.R.layout.activity_list_item, null)
        val pinInput = android.widget.EditText(this).apply {
            hint = "PIN girin"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        AlertDialog.Builder(this)
            .setTitle("Ebeveyn PIN")
            .setView(pinInput)
            .setPositiveButton("Onayla") { _, _ ->
                onResult(prefs.verifyPin(pinInput.text.toString()))
                if (!prefs.verifyPin(pinInput.text.toString())) {
                    Toast.makeText(this, "Yanlış PIN!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showConfirmDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Evet") { _, _ -> onConfirm() }
            .setNegativeButton("Hayır", null)
            .show()
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}
