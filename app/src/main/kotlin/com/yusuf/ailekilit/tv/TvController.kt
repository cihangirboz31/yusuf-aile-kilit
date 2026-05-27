package com.yusuf.ailekilit.tv

import android.content.Context
import com.yusuf.ailekilit.data.PrefsManager
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

enum class TvCommand(val adbKeycode: Int, val label: String) {
    HOME(3, "Ana Ekran"),
    BACK(4, "Geri"),
    OK(23, "Tamam"),
    UP(19, "Yukarı"),
    DOWN(20, "Aşağı"),
    LEFT(21, "Sol"),
    RIGHT(22, "Sağ"),
    VOLUME_UP(24, "Ses Arttır"),
    VOLUME_DOWN(25, "Ses Azalt"),
    MUTE(164, "Sessiz"),
    POWER(26, "Güç"),
    MEDIA_STOP(86, "Durdur"),
    MEDIA_PLAY_PAUSE(85, "Oynat/Duraklat")
}

sealed class TvResult {
    object Success : TvResult()
    data class Failure(val reason: String) : TvResult()
    object NotSupported : TvResult()
}

class TvController(private val context: Context) {

    private val prefs = PrefsManager(context)

    suspend fun sendCommand(command: TvCommand): TvResult = withContext(Dispatchers.IO) {
        val ip = prefs.tvIpAddress
        val port = prefs.tvPort
        if (ip.isBlank()) return@withContext TvResult.Failure("TV IP adresi ayarlanmamış")
        try {
            sendAdbCommand(ip, port, command.adbKeycode)
        } catch (e: Exception) {
            TvResult.Failure("Komut gönderilemedi: ${e.message}")
        }
    }

    private fun sendAdbCommand(ip: String, port: Int, keycode: Int): TvResult {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ip, port), 3000)
            socket.soTimeout = 3000
            val writer = PrintWriter(socket.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            writer.println("sendevent keycode $keycode")
            val response = try { reader.readLine() } catch (e: Exception) { null }
            writer.close()
            reader.close()
            socket.close()
            TvResult.Success
        } catch (e: java.net.ConnectException) {
            TvResult.Failure("TV'ye bağlanılamadı. IP: $ip")
        } catch (e: java.net.SocketTimeoutException) {
            TvResult.Failure("Bağlantı zaman aşımı")
        } catch (e: Exception) {
            TvResult.Failure(e.message ?: "Bilinmeyen hata")
        }
    }

    suspend fun sendKeyToTv(keycode: Int): TvResult = withContext(Dispatchers.IO) {
        val ip = prefs.tvIpAddress
        val port = prefs.tvPort
        if (ip.isBlank()) return@withContext TvResult.Failure("TV bağlı değil")
        sendAdbCommand(ip, port, keycode)
    }

    suspend fun blockYouTubeOnTv(): TvResult {
        val homeResult = sendCommand(TvCommand.HOME)
        delay(500)
        return homeResult
    }

    suspend fun pingTv(ip: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ip, port), 2000)
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun discoverAndroidTvDevices(): List<String> = withContext(Dispatchers.IO) {
        val found = mutableListOf<String>()
        val localIp = getLocalIpPrefix() ?: return@withContext found
        val jobs = (1..254).map { i ->
            async {
                val ip = "$localIp.$i"
                val ports = listOf(6466, 5555, 7105)
                for (port in ports) {
                    try {
                        val socket = Socket()
                        socket.connect(InetSocketAddress(ip, port), 300)
                        socket.close()
                        return@async ip
                    } catch (e: Exception) { }
                }
                null
            }
        }
        jobs.awaitAll().filterNotNull().forEach { found.add(it) }
        found
    }

    private fun getLocalIpPrefix(): String? {
        return try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            interfaces?.toList()?.forEach { ni ->
                ni.inetAddresses?.toList()?.forEach { addr ->
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        val ip = addr.hostAddress ?: return@forEach
                        val parts = ip.split(".")
                        if (parts.size == 4) {
                            return "${parts[0]}.${parts[1]}.${parts[2]}"
                        }
                    }
                }
            }
            null
        } catch (e: Exception) { null }
    }
}
