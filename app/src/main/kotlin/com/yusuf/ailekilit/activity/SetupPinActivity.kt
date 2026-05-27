package com.yusuf.ailekilit.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yusuf.ailekilit.data.PrefsManager
import com.yusuf.ailekilit.databinding.ActivitySetupPinBinding

class SetupPinActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupPinBinding
    private lateinit var prefs: PrefsManager
    private var firstPin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupPinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PrefsManager(this)

        binding.titleText.text = "Ebeveyn PIN'i Oluştur"
        binding.subtitleText.text = "Güvenli bir 4-6 haneli PIN belirleyin"

        setupNumpad()

        binding.confirmButton.setOnClickListener {
            val input = binding.pinInput.text.toString()
            if (input.length < 4) {
                Toast.makeText(this, "PIN en az 4 haneli olmalı", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (firstPin.isEmpty()) {
                firstPin = input
                binding.pinInput.text?.clear()
                binding.titleText.text = "PIN'i Tekrar Girin"
                binding.subtitleText.text = "Doğrulamak için PIN'i tekrar girin"
            } else {
                if (firstPin == input) {
                    prefs.pin = input
                    prefs.isPinSet = true
                    Toast.makeText(this, "PIN başarıyla oluşturuldu!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, PermissionsActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "PIN'ler eşleşmiyor. Tekrar deneyin.", Toast.LENGTH_SHORT).show()
                    firstPin = ""
                    binding.pinInput.text?.clear()
                    binding.titleText.text = "Ebeveyn PIN'i Oluştur"
                    binding.subtitleText.text = "Güvenli bir 4-6 haneli PIN belirleyin"
                }
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
}
