package com.yusuf.ailekilit.activity

import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yusuf.ailekilit.R
import com.yusuf.ailekilit.data.PrefsManager
import com.yusuf.ailekilit.databinding.ActivityPinBinding
import com.yusuf.ailekilit.service.MonitorService

class PinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinBinding
    private lateinit var prefs: PrefsManager
    private var failedAttempts = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PrefsManager(this)

        binding.titleText.text = "Ebeveyn Girişi"
        binding.subtitleText.text = "PIN kodunuzu girin"

        setupNumpad()

        binding.confirmButton.setOnClickListener {
            val input = binding.pinInput.text.toString()
            if (prefs.verifyPin(input)) {
                MonitorService.start(this)
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                failedAttempts++
                binding.pinInput.text?.clear()
                val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
                binding.pinInput.startAnimation(shake)
                vibrate()
                if (failedAttempts >= 3) {
                    binding.subtitleText.text = "Hatalı deneme: $failedAttempts — Lütfen bekleyin"
                }
                Toast.makeText(this, "Yanlış PIN! ($failedAttempts. hatalı deneme)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupNumpad() {
        val nb = binding.numpadInclude
        val buttons = listOf(
            nb.btn0, nb.btn1, nb.btn2, nb.btn3,
            nb.btn4, nb.btn5, nb.btn6, nb.btn7,
            nb.btn8, nb.btn9
        )
        buttons.forEachIndexed { i, btn ->
            btn.setOnClickListener {
                val current = binding.pinInput.text.toString()
                if (current.length < 6) {
                    binding.pinInput.setText(current + i.toString())
                }
            }
        }
        nb.btnDelete.setOnClickListener {
            val current = binding.pinInput.text.toString()
            if (current.isNotEmpty()) {
                binding.pinInput.setText(current.dropLast(1))
            }
        }
    }

    private fun vibrate() {
        val vibrator = getSystemService(Vibrator::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(200)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Geri tuşunu engelle — PIN olmadan çıkış yok
    }
}
