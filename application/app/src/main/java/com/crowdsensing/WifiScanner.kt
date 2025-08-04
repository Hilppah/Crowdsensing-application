package com.crowdsensing

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import android.os.Handler

class WifiScanner(
    private val context: Context,
    private val wifiManager: WifiManager,
    private val handler: Handler,
    private val interval: Long,
    private val updateCallback: (String) -> Unit
) {
    private val scanRunnable = object : Runnable {
        override fun run() {
            scan()
            handler.postDelayed(this, interval)
        }
    }

    fun start() {
        scanRunnable.run()
    }

    fun stop() {
        handler.removeCallbacks(scanRunnable)
    }

    private fun scan() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            updateCallback("Wi-Fi Location permission denied")
            return
        }

        try {
            val success = wifiManager.startScan()
            if (!success) {
                updateCallback("Wi-Fi scan failed")
                return
            }

            val results = wifiManager.scanResults
            if (results.isNotEmpty()) {
                val sb = StringBuilder("Nearby Wi-Fi Networks:\n")
                results.take(5).forEach { sb.append("${it.SSID} - ${it.level} dBm\n") }
                updateCallback(sb.toString())
            } else {
                updateCallback("No Wi-Fi networks found")
            }
        } catch (e: SecurityException) {
            updateCallback("Wi-Fi scan failed: permission denied")
        }
    }
}
