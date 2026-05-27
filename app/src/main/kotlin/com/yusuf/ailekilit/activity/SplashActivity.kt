package com.yusuf.ailekilit.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.yusuf.ailekilit.data.PrefsManager
import com.yusuf.ailekilit.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PrefsManager(this)
        playAnimation()
    }

    private fun playAnimation() {
        binding.iconView.alpha = 0f
        binding.titleText.alpha = 0f
        binding.subtitleText.alpha = 0f
        binding.devText.alpha = 0f

        val iconFade = ObjectAnimator.ofFloat(binding.iconView, View.ALPHA, 0f, 1f).apply {
            duration = 700
        }
        val iconScale = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.iconView, View.SCALE_X, 0.5f, 1f),
                ObjectAnimator.ofFloat(binding.iconView, View.SCALE_Y, 0.5f, 1f)
            )
            duration = 700
            interpolator = AccelerateDecelerateInterpolator()
        }
        val titleAnim = ObjectAnimator.ofFloat(binding.titleText, View.ALPHA, 0f, 1f).apply {
            duration = 600
            startDelay = 500
        }
        val subtitleAnim = ObjectAnimator.ofFloat(binding.subtitleText, View.ALPHA, 0f, 1f).apply {
            duration = 600
            startDelay = 800
        }
        val devAnim = ObjectAnimator.ofFloat(binding.devText, View.ALPHA, 0f, 1f).apply {
            duration = 600
            startDelay = 1100
        }

        val fullSet = AnimatorSet()
        fullSet.playTogether(iconFade, iconScale, titleAnim, subtitleAnim, devAnim)
        fullSet.start()

        binding.root.postDelayed({ navigateNext() }, 2800)
    }

    private fun navigateNext() {
        val intent = if (!prefs.isPinSet) {
            Intent(this, SetupPinActivity::class.java)
        } else {
            Intent(this, PinActivity::class.java)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
