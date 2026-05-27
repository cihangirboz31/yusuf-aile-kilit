package com.yusuf.ailekilit.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yusuf.ailekilit.databinding.ActivityPermissionsBinding
import com.yusuf.ailekilit.service.MonitorService
import com.yusuf.ailekilit.util.UsageStatsHelper

class PermissionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPermissionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnUsageAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        binding.btnOverlay.setOnClickListener {
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
        }

        binding.btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.btnBatteryOpt.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }

        binding.btnContinue.setOnClickListener {
            val hasUsage = UsageStatsHelper.hasUsageStatsPermission(this)
            val hasOverlay = Settings.canDrawOverlays(this)
            if (!hasUsage) {
                Toast.makeText(this, "Kullanım İstatistikleri izni gerekli!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            MonitorService.start(this)
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun updatePermissionStatus() {
        val hasUsage = UsageStatsHelper.hasUsageStatsPermission(this)
        val hasOverlay = Settings.canDrawOverlays(this)

        binding.usageStatusText.text = if (hasUsage) "✅ Kullanım İstatistikleri: VERİLDİ" else "❌ Kullanım İstatistikleri: GEREKLİ"
        binding.overlayStatusText.text = if (hasOverlay) "✅ Diğer Uygulamaların Üzerinde: VERİLDİ" else "❌ Diğer Uygulamaların Üzerinde: GEREKLİ"
        binding.usageStatusText.setTextColor(
            if (hasUsage) getColor(android.R.color.holo_green_light) else getColor(android.R.color.holo_red_light)
        )
        binding.overlayStatusText.setTextColor(
            if (hasOverlay) getColor(android.R.color.holo_green_light) else getColor(android.R.color.holo_red_light)
        )
    }
}
