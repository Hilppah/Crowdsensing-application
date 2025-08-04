package com.crowdsensing

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import kotlin.math.roundToInt

object Sensors {
    private const val ALPHA = 0.8f
    private val accelLin = FloatArray(3)
    private val accelGravity = FloatArray(3)
    private val accelLinear = FloatArray(3)
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)

    private var hasGravity = false
    private var hasMagnet = false


    fun sensorAccelerometer(event: SensorEvent): String {
        accelGravity[0] = ALPHA * accelGravity[0] + (1 - ALPHA) * event.values[0]
        accelGravity[1] = ALPHA * accelGravity[1] + (1 - ALPHA) * event.values[1]
        accelGravity[2] = ALPHA * accelGravity[2] + (1 - ALPHA) * event.values[2]
        hasGravity = true
        accelLin[0] = event.values[0] - accelGravity[0]
        accelLin[1] = event.values[1] - accelGravity[1]
        accelLin[2] = event.values[2] - accelGravity[2]

        return "Accelerometer:\nX: ${accelLinear[0]}\nY: ${accelLinear[1]}\nZ: ${accelLinear[2]}"
    }

    fun sensorGyroscope(event: SensorEvent): String {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        return "Gyroscope:\nX: $x\nY: $y\nZ: $z"
    }

    fun sensorProximity(event: SensorEvent): String {
        val distance = event.values[0]
        return "Proximity: $distance cm"
    }

    fun sensorMagnetometer(event: SensorEvent) {
        geomagnetic[0] = event.values[0]
        geomagnetic[1] = event.values[1]
        geomagnetic[2] = event.values[2]
        hasMagnet = true
    }

    fun getCompassReading(): String {
        if (!hasGravity || !hasMagnet) return "Compass: Unavailable"

        val R = FloatArray(9)
        val I = FloatArray(9)

        return if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(R, orientation)
            val azimuthRad = orientation[0]
            val azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
            val normalized = (azimuthDeg + 360) % 360
            val direction = getCompassDirection(normalized)

            "Compass: ${normalized.roundToInt()}° ($direction)"
        } else {
            "Compass: Unable to calculate"
        }
    }

    private fun getCompassDirection(azimuth: Float): String {
        return when (azimuth) {
            in 337.5..360.0, in 0.0..22.5 -> "North"
            in 22.5..67.5 -> "Northeast"
            in 67.5..112.5 -> "East"
            in 112.5..157.5 -> "Southeast"
            in 157.5..202.5 -> "South"
            in 202.5..247.5 -> "Southwest"
            in 247.5..292.5 -> "West"
            in 292.5..337.5 -> "Northwest"
            else -> "Unknown"
        }
    }

    fun reset() {
        hasGravity = false
        hasMagnet = false
    }
}
