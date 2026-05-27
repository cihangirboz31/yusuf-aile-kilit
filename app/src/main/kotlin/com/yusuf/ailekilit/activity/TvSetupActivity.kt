package com.yusuf.ailekilit.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yusuf.ailekilit.data.PrefsManager
import com.yusuf.ailekilit.databinding.ActivityTvSetupBinding
import com.yusuf.ailekilit.tv.TvCommand
import com.yusuf.ailekilit.tv.TvController
import com.yusuf.ailekilit.tv.TvResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TvSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTvSetupBinding
    private lateinit var prefs: PrefsManager
    private lateinit var tvController: TvController
    private var selectedTvType = PrefsManager.TV_TYPE_ANDROID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTvSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = PrefsManager(this)
        tvController = TvController(this)

        if (prefs.tvPaired) {
            binding.ipInput.setText(prefs.tvIpAddress)
            showControls()
        }

        setupTypeButtons()
        setupActionButtons()
    }

    private fun setupTypeButtons() {
        binding.btnArcelik.setOnClickListener {
            selectedTvType = PrefsManager.TV_TYPE_ARCELIK
            prefs.tvPort = 6466
            binding.tvTypeInfo.text = "Arçelik TV seçildi. Port: 6466 (Android TV Remote)"
            highlightSelected(0)
        }
        binding.btnTurkcell.setOnClickListener {
            selectedTvType = PrefsManager.TV_TYPE_TURKCELL
            prefs.tvPort = 6466
            binding.tvTypeInfo.text = "Turkcell TV+ kutusu seçildi. Port: 6466 (Android TV Remote)"
            highlightSelected(1)
        }
        binding.btnAndroidTv.setOnClickListener {
            selectedTvType = PrefsManager.TV_TYPE_ANDROID
            prefs.tvPort = 6466
            binding.tvTypeInfo.text = "Android TV / Google TV seçildi. Port: 6466"
            highlightSelected(2)
        }
    }

    private fun highlightSelected(selected: Int) {
        val alpha = 1.0f
        val dim = 0.5f
        binding.btnArcelik.alpha = if (selected == 0) alpha else dim
        binding.btnTurkcell.alpha = if (selected == 1) alpha else dim
        binding.btnAndroidTv.alpha = if (selected == 2) alpha else dim
    }

    private fun setupActionButtons() {
        binding.btnScanNetwork.setOnClickListener {
            binding.scanProgress.visibility = View.VISIBLE
            binding.scanResultText.text = "Ağ taranıyor..."
            lifecycleScope.launch {
                val devices = tvController.discoverAndroidTvDevices()
                withContext(Dispatchers.Main) {
                    binding.scanProgress.visibility = View.GONE
                    if (devices.isEmpty()) {
                        binding.scanResultText.text = "Cihaz bulunamadı. IP'yi manuel girin."
                    } else {
                        binding.scanResultText.text = "Bulunan cihazlar:\n${devices.joinToString("\n")}"
                        binding.ipInput.setText(devices.first())
                    }
                }
            }
        }

        binding.btnConnect.setOnClickListener {
            val ip = binding.ipInput.text.toString().trim()
            if (ip.isEmpty()) {
                Toast.makeText(this, "IP adresi girin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.connectProgress.visibility = View.VISIBLE
            lifecycleScope.launch {
                val port = prefs.tvPort
                val reachable = tvController.pingTv(ip, port)
                withContext(Dispatchers.Main) {
                    binding.connectProgress.visibility = View.GONE
                    if (reachable) {
                        prefs.tvIpAddress = ip
                        prefs.tvType = selectedTvType
                        prefs.tvPaired = true
                        binding.connectionStatusText.text = "✅ TV bağlandı: $ip"
                        showControls()
                        Toast.makeText(this@TvSetupActivity, "TV başarıyla eşleştirildi!", Toast.LENGTH_SHORT).show()
                    } else {
                        binding.connectionStatusText.text = "❌ Bağlanılamadı: $ip:$port"
                        showTvNotSupported()
                    }
                }
            }
        }

        setupRemoteButtons()

        binding.btnDisconnect.setOnClickListener {
            prefs.tvPaired = false
            prefs.tvIpAddress = ""
            binding.controlsLayout.visibility = View.GONE
            binding.connectionStatusText.text = "TV bağlantısı kesildi"
            Toast.makeText(this, "TV bağlantısı kesildi", Toast.LENGTH_SHORT).show()
        }

        binding.backButton.setOnClickListener { finish() }
    }

    private fun setupRemoteButtons() {
        fun sendCmd(cmd: TvCommand) {
            lifecycleScope.launch {
                val result = tvController.sendCommand(cmd)
                withContext(Dispatchers.Main) {
                    when (result) {
                        is TvResult.Success -> {}
                        is TvResult.Failure -> Toast.makeText(
                            this@TvSetupActivity, "Komut hatası: ${result.reason}", Toast.LENGTH_SHORT
                        ).show()
                        TvResult.NotSupported -> Toast.makeText(
                            this@TvSetupActivity, "Bu komut desteklenmiyor", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        binding.btnHome.setOnClickListener { sendCmd(TvCommand.HOME) }
        binding.btnBack.setOnClickListener { sendCmd(TvCommand.BACK) }
        binding.btnOk.setOnClickListener { sendCmd(TvCommand.OK) }
        binding.btnUp.setOnClickListener { sendCmd(TvCommand.UP) }
        binding.btnDown.setOnClickListener { sendCmd(TvCommand.DOWN) }
        binding.btnLeft.setOnClickListener { sendCmd(TvCommand.LEFT) }
        binding.btnRight.setOnClickListener { sendCmd(TvCommand.RIGHT) }
        binding.btnVolUp.setOnClickListener { sendCmd(TvCommand.VOLUME_UP) }
        binding.btnVolDown.setOnClickListener { sendCmd(TvCommand.VOLUME_DOWN) }
        binding.btnMute.setOnClickListener { sendCmd(TvCommand.MUTE) }
        binding.btnPower.setOnClickListener { sendCmd(TvCommand.POWER) }

        binding.btnBlockYoutubeTv.setOnClickListener {
            lifecycleScope.launch {
                val result = tvController.blockYouTubeOnTv()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TvSetupActivity,
                        if (result is TvResult.Success) "TV'de YouTube kapatıldı!" else "Komut gönderilemedi",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showControls() {
        binding.controlsLayout.visibility = View.VISIBLE
        binding.notSupportedLayout.visibility = View.GONE
        binding.connectionStatusText.text = "✅ TV Bağlı: ${prefs.tvIpAddress}"
    }

    private fun showTvNotSupported() {
        binding.controlsLayout.visibility = View.GONE
        binding.notSupportedLayout.visibility = View.VISIBLE
    }
}
