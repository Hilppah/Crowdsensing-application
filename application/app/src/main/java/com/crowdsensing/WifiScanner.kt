package com.crowdsensing

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Handler
import com.crowdsensing.model.WifiScan
import kotlin.math.pow

class WifiScanner(
    private val context: Context,
    private val wifiManager: WifiManager,
    private val handler: Handler,
    private val scanInterval: Long,
    private val callback: (String, List<WifiScan>) -> Unit
) {
    private val scanRunnable = object : Runnable {
        override fun run() {
            scan()
            handler.postDelayed(this, scanInterval)
        }
    }

    fun scan() {
        val results = wifiManager.scanResults
        val networks = results.map {
            val distance = calculateDistance(it.level, it.frequency)
            WifiScan(
                ssid = it.SSID.ifBlank { "<hidden>" },
                rssi = it.level,
                status = proximityStatus(it.SSID, it.level),
                distance = distance
            )
        }
        val summary = "Found ${networks.size} networks"
        callback(summary, networks)
    }

    fun start() {
        handler.post(scanRunnable)
    }

    fun stop() {
        handler.removeCallbacks(scanRunnable)
    }

    private fun calculateDistance(rssi: Int, freqMHz: Int): Double {
        val exp = (27.55 - (20 * Math.log10(freqMHz.toDouble())) + kotlin.math.abs(rssi)) / 20.0
        return 10.0.pow(exp)
    }

    private val lastRssiMap = mutableMapOf<String, Int>()
    private fun proximityStatus(key: String, newRssi: Int): String {
        val lastRssi = lastRssiMap[key]
        lastRssiMap[key] = newRssi
        return when {
            lastRssi == null -> "first seen"
            newRssi > lastRssi -> "closer"
            newRssi < lastRssi -> "further"
            else -> "same"
        }
    }
}
