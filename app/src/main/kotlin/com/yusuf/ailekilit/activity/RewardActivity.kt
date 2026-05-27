package com.yusuf.ailekilit.activity

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yusuf.ailekilit.data.PrefsManager
import com.yusuf.ailekilit.databinding.ActivityRewardBinding

class RewardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRewardBinding
    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRewardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PrefsManager(this)

        updateStatus()

        binding.btnHomework.setOnClickListener { grantReward("Ödev yaptı", 10) }
        binding.btnReading.setOnClickListener { grantReward("Kitap okudu", 15) }
        binding.btnChores.setOnClickListener { grantReward("Ev işi yaptı", 10) }
        binding.btnCustom.setOnClickListener { showCustomRewardDialog() }
        binding.backButton.setOnClickListener { finish() }
    }

    private fun grantReward(reason: String, minutes: Int) {
        showPinDialog { success ->
            if (success) {
                prefs.addBonusSeconds(minutes * 60L)
                Toast.makeText(this, "✅ $reason: +$minutes dakika eklendi!", Toast.LENGTH_LONG).show()
                updateStatus()
            }
        }
    }

    private fun showCustomRewardDialog() {
        val minuteInput = android.widget.EditText(this).apply {
            hint = "Eklenecek dakika"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        AlertDialog.Builder(this)
            .setTitle("Özel Süre Ekle")
            .setMessage("Kaç dakika eklemek istiyorsunuz?")
            .setView(minuteInput)
            .setPositiveButton("Ekle") { _, _ ->
                val minutes = minuteInput.text.toString().toIntOrNull()
                if (minutes == null || minutes <= 0) {
                    Toast.makeText(this, "Geçerli bir dakika girin", Toast.LENGTH_SHORT).show()
                } else {
                    grantReward("Özel ödül", minutes)
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun updateStatus() {
        val remaining = prefs.remainingSeconds
        val remMin = remaining / 60
        val remSec = remaining % 60
        binding.remainingText.text = "Kalan süre: ${remMin}dk ${remSec}sn"
        binding.usedText.text = "Kullanılan: ${prefs.usedSecondsToday / 60}dk"
        binding.limitText.text = "Günlük limit: ${prefs.dailyLimitMinutes}dk"
    }

    private fun showPinDialog(onResult: (Boolean) -> Unit) {
        val pinInput = android.widget.EditText(this).apply {
            hint = "Ebeveyn PIN'i"
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
