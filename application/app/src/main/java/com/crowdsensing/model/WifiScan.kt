package com.crowdsensing.model

data class WifiScan(
    val ssid: String,
    val rssi: Int,
    val status: String,
    val distance: Double)
