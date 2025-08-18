package com.crowdsensing.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant

@Parcelize
data class Session(
    val phoneModel: String,
    val startTime: Instant,
    val endTime: Instant,
    val description: String? = null,
    val gps: List<GPSData> = emptyList(),
    val compass: List<CompassData> = emptyList(),
    val proximity: List<ProximityData> = emptyList(),
    val accelerometer: List<AccelerometerData> = emptyList(),
    val gyroscope: List<GyroscopeData> = emptyList()
) : Parcelable {

    @Parcelize
    data class GPSData(
        val latitude: Double,
        val longitude: Double,
        val timestamp: Instant
    ) : Parcelable

    @Parcelize
    data class CompassData(
        val compassData: Double,
        val timestamp: Instant
    ) : Parcelable

    @Parcelize
    data class ProximityData(
        val proximity: Double,
        val timestamp: Instant
    ) : Parcelable

    @Parcelize
    data class AccelerometerData(
        val accelX: Double,
        val accelY: Double,
        val accelZ: Double,
        val timestamp: Instant
    ) : Parcelable

    @Parcelize
    data class GyroscopeData(
        val gyroX: Double,
        val gyroY: Double,
        val gyroZ: Double,
        val timestamp: Instant
    ) : Parcelable
}
