package com.yusuf.ailekilit.activity

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.KeyEvent
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yusuf.ailekilit.R
import com.yusuf.ailekilit.data.PrefsManager
import com.yusuf.ailekilit.databinding.ActivityLockScreenBinding

class LockScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockScreenBinding
    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PrefsManager(this)

        updateTimeInfo()
        setupNumpad()

        binding.unlockButton.setOnClickListener {
            val input = binding.pinInput.text.toString()
            if (prefs.verifyPin(input)) {
                prefs.isLocked = false
                finish()
            } else {
                binding.pinInput.text?.clear()
                val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
                binding.lockCard.startAnimation(shake)
                vibrate()
                Toast.makeText(this, "Yanlış PIN!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.addTimeButton.setOnClickListener {
            val input = binding.pinInput.text.toString()
            if (prefs.verifyPin(input)) {
                prefs.addBonusSeconds(15 * 60)
                prefs.isLocked = false
                Toast.makeText(this, "+15 dakika eklendi!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                binding.pinInput.text?.clear()
                Toast.makeText(this, "Yanlış PIN!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTimeInfo() {
        val usedMin = prefs.usedSecondsToday / 60
        binding.usedTimeText.text = "Bugün kullanılan: $usedMin dakika"
        binding.limitText.text = "Günlük limit: ${prefs.dailyLimitMinutes} dakika"
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
            vibrator?.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(300)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_HOME,
            KeyEvent.KEYCODE_APP_SWITCH -> true
            else -> super.onKeyDown(keyCode, event)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { }

    override fun onResume() {
        super.onResume()
        if (!prefs.isLocked) {
            finish()
        }
    }
}
