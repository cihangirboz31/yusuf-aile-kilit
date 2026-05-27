package com.yusuf.ailekilit.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yusuf.ailekilit.databinding.ActivityRouterGuideBinding

class RouterGuideActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRouterGuideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouterGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backButton.setOnClickListener { finish() }
    }
}
