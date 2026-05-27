package com.yusuf.ailekilit.activity

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yusuf.ailekilit.data.PrefsManager
import com.yusuf.ailekilit.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PrefsManager(this)

        loadSettings()
        setupButtons()
    }

    private fun loadSettings() {
        binding.limitSeekBar.progress = prefs.dailyLimitMinutes
        binding.limitValueText.text = "${prefs.dailyLimitMinutes} dakika"
        binding.serviceSwitch.isChecked = prefs.serviceEnabled
    }

    private fun setupButtons() {
        binding.limitSeekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val actual = progress.coerceAtLeast(5)
                binding.limitValueText.text = "$actual dakika"
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        binding.saveLimitButton.setOnClickListener {
            showPinDialog { success ->
                if (success) {
                    val limit = binding.limitSeekBar.progress.coerceAtLeast(5)
                    prefs.dailyLimitMinutes = limit
                    Toast.makeText(this, "Limit kaydedildi: $limit dakika", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.serviceSwitch.setOnCheckedChangeListener { _, isChecked ->
            showPinDialog { success ->
                if (success) {
                    prefs.serviceEnabled = isChecked
                    Toast.makeText(
                        this,
                        if (isChecked) "İzleme servisi açıldı" else "İzleme servisi durduruldu",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    binding.serviceSwitch.isChecked = !isChecked
                }
            }
        }

        binding.changePinButton.setOnClickListener {
            showPinDialog { success ->
                if (success) {
                    startActivity(Intent(this, SetupPinActivity::class.java))
                }
            }
        }

        binding.deviceAdminButton.setOnClickListener {
            val intent = Intent(android.app.admin.DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            val cn = android.content.ComponentName(this, com.yusuf.ailekilit.admin.DeviceAdminReceiver::class.java)
            intent.putExtra(android.app.admin.DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn)
            intent.putExtra(android.app.admin.DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Uygulamanın kaldırılmasını zorlaştırmak için Cihaz Yöneticisi gereklidir.")
            startActivity(intent)
        }

        binding.batteryOptButton.setOnClickListener {
            val pm = getSystemService(PowerManager::class.java)
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            } else {
                Toast.makeText(this, "Pil optimizasyonu zaten devre dışı!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.backButton.setOnClickListener { finish() }
    }

    private fun showPinDialog(onResult: (Boolean) -> Unit) {
        val pinInput = android.widget.EditText(this).apply {
            hint = "PIN girin"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        AlertDialog.Builder(this)
            .setTitle("Ebeveyn PIN")
            .setView(pinInput)
            .setPositiveButton("Onayla") { _, _ ->
                val ok = prefs.verifyPin(pinInput.text.toString())
                if (!ok) Toast.makeText(this, "Yanlış PIN!", Toast.LENGTH_SHORT).show()
                onResult(ok)
            }
            .setNegativeButton("İptal", null)
            .show()
    }
}
