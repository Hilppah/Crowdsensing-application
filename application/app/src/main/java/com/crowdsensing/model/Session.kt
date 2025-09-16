package com.crowdsensing.model

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Parcelize
data class Session(
    @JsonProperty("_id") val id: String? = null,
    val phoneModel: String,
    val stability: Int? = null,
    val startTime: Instant,
    val endTime: Instant,
    val frequency: Long? = null,
    val description: String? = null,
    val gps: List<GPSData>? = null,
    val compass: List<CompassData>? = null,
    val proximity: List<ProximityData>? = null,
    val accelerometer: List<AccelerometerData>? = null,
    val gyroscope: List<GyroscopeData>? = null,
    val chosenMeasurement: String? = null
) : Parcelable {

    @Parcelize
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GPSData(
        @JsonProperty("_id") val id: String? = null,
        val latitude: Double,
        val longitude: Double,
        val timestamp: Instant,
        val stability: Int? = null
    ) : Parcelable

    @Parcelize
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class CompassData(
        @JsonProperty("_id") val id: String? = null,
        val compassData: Double,
        val timestamp: Instant,
        val stability: Int? = null
    ) : Parcelable

    @Parcelize
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ProximityData(
        @JsonProperty("_id") val id: String? = null,
        val proximity: Double,
        val timestamp: Instant,
        val stability: Int? = null
    ) : Parcelable

    @Parcelize
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AccelerometerData(
        @JsonProperty("_id") val id: String? = null,
        val accelX: Double,
        val accelY: Double,
        val accelZ: Double,
        val timestamp: Instant,
        val stability: Int? = null
    ) : Parcelable

    @Parcelize
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GyroscopeData(
        @JsonProperty("_id") val id: String? = null,
        val gyroX: Double,
        val gyroY: Double,
        val gyroZ: Double,
        val timestamp: Instant,
        val stability: Int? = null
    ) : Parcelable
}
