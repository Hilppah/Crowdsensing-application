package com.crowdsensing.model

data class BluetoothScan (
    val name: String,
    val address: String,
    val rssi: Int,
    val status: String,
    val distance: Double
)
