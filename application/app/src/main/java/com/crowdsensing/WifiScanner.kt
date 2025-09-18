package com.crowdsensing

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat

class WifiScanner(
    private val context: Context,
    private val wifiManager: WifiManager,
    private val handler: Handler,
    private val interval: Long,
    private val updateCallback: (String) -> Unit
) {
    private var isRunning = false
    private val lastRssiMap = mutableMapOf<String, Int>()

    private val scanRunnable = object : Runnable {
        override fun run() {
            scan()
            handler.postDelayed(this, interval)
        }
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        scanRunnable.run()
    }

    fun stop() {
        if (!isRunning) return
        isRunning = false
        handler.removeCallbacks(scanRunnable)
    }

    private fun scan() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            updateCallback("Wi-Fi: Location permission denied")
            return
        }

        try {
            if (!wifiManager.isWifiEnabled) {
                updateCallback("Wi-Fi is disabled")
                return
            }

            val success = wifiManager.startScan()
            if (!success) {
                updateCallback("Wi-Fi scan failed")
                return
            }

            val results = wifiManager.scanResults
            if (results.isNotEmpty()) {
                val sb = StringBuilder("Nearby Wi-Fi Networks:\n")
                results.take(5).forEach {
                    val ssid = it.SSID.ifBlank { "<hidden>" }
                    val rssi = it.level
                    val status = proximityStatus(ssid, rssi)
                    sb.append("$ssid → $rssi dBm ($status)\n")
                }
                updateCallback(sb.toString())
            } else {
                updateCallback("No Wi-Fi networks found")
            }
        } catch (e: SecurityException) {
            updateCallback("Wi-Fi scan failed: permission denied")
        }
    }

    private fun proximityStatus(key: String, newRssi: Int): String {
        val lastRssi = lastRssiMap[key]
        lastRssiMap[key] = newRssi
        if (lastRssi == null) return "first seen"
        return if (newRssi > lastRssi) "closer"
        else if (newRssi < lastRssi) "further"
        else "same"
    }
}

class BluetoothScanner(
    private val context: Context,
    private val updateCallback: (String) -> Unit
) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bleScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private var isScanning = false
    private val lastRssiMap = mutableMapOf<String, Int>()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result ?: return
            @SuppressLint("MissingPermission")
            val name = result.device.name ?: "Unknown"
            val addr = result.device.address
            val rssi = result.rssi
            val key = "$name ($addr)"
            val status = proximityStatus(key, rssi)
            updateCallback("BLE: $key → $rssi dBm ($status)")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach  {onScanResult(0, it)}
        }
    }

    fun start() {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else true

        if (!hasPermission) {
            updateCallback("Bluetooth: Scan permission required")
            return
        }

        if (bluetoothAdapter == null || bleScanner == null) {
            updateCallback("Bluetooth not supported")
            return
        }

        if (isScanning) return
        isScanning = true
        @SuppressLint("MissingPermission")
        bleScanner?.startScan(scanCallback)
        updateCallback("Bluetooth scan started…")
    }

    fun stop() {
    if (!isScanning) return
        isScanning = false
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
                == PackageManager.PERMISSION_GRANTED
            ) {
                @SuppressLint("MissingPermission")
                bleScanner?.stopScan(scanCallback)
            }
        } catch (_: Exception) { }
        updateCallback("Bluetooth scan stopped")
    }

    private fun proximityStatus(key: String, newRssi: Int): String {
        val lastRssi = lastRssiMap[key]
        lastRssiMap[key] = newRssi
        if (lastRssi == null) return "first seen"
        return if (newRssi > lastRssi) "closer"
        else if (newRssi < lastRssi) "further"
        else "same"
    }
}
