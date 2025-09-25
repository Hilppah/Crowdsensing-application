package com.crowdsensing

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.Context
import com.crowdsensing.model.BluetoothScan
import com.crowdsensing.model.Session
import java.time.Instant
import kotlin.math.pow

class BluetoothScanner(
    private val context: Context,
    private val callback: (String, List<BluetoothScan>) -> Unit
) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bleScanner = bluetoothAdapter?.bluetoothLeScanner
    private var isScanning = false
    private val lastRssiMap = mutableMapOf<String, Int>()
    private val devicesFound = mutableListOf<BluetoothScan>()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result ?: return
            @SuppressLint("MissingPermission")
            val name = result.device.name ?: "Unknown"
            val address = result.device.address
            val rssi = result.rssi
            val key = "$name ($address)"
            val status = proximityStatus(key, rssi)
            val distance = calculateDistance(rssi)
            val device = BluetoothScan(name, address, rssi, status, distance)

            val deviceWithTimestamp = Session.BluetoothData(
                name = name,
                address = address,
                rssi = rssi,
                status = status,
                timestamp = Instant.now()
            )

            devicesFound.removeAll { it.address == address }
            devicesFound.add(device)
            callback("Found ${devicesFound.size} devices", devicesFound.toList())
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { onScanResult(0, it) }
        }
    }

    fun start() {
        if (isScanning) return
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else{
            true }

        if (!hasPermission) {
            callback("Bluetooth: Scan permission required", emptyList())
            return
        }

        isScanning = true
        devicesFound.clear()
        @SuppressLint("MissingPermission")
        bleScanner?.startScan(scanCallback)
    }

    fun stop() {
        if (!isScanning) return
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (!hasPermission) {
            callback("Bluetooth: Scan permission required", emptyList())
            return
        }
        isScanning = false
        @SuppressLint("MissingPermission")
        bleScanner?.stopScan(scanCallback)
    }

    private fun proximityStatus(key: String, newRssi: Int): String {
        val lastRssi = lastRssiMap[key]
        lastRssiMap[key] = newRssi
        return when {
            lastRssi == null -> "first seen"
            newRssi > lastRssi -> "closer"
            newRssi < lastRssi -> "further"
            else -> "same" }
    }

    private fun calculateDistance(rssi: Int): Double {
        val txPower = -59
        val pathLossExponent = 2.0
        return 10.0.pow((txPower - rssi) / (10 * pathLossExponent))
    }
}
