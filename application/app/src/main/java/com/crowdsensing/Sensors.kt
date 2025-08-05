package com.crowdsensing

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import com.crowdsensing.sensor.SensorResult
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

    enum class SensorType(val androidSensorType: Int) {
        GYROSCOPE(Sensor.TYPE_GYROSCOPE),
        ACCELEROMETER(Sensor.TYPE_ACCELEROMETER),
        PROXIMITY(Sensor.TYPE_PROXIMITY),
        MAGNETIC_FIELD(Sensor.TYPE_MAGNETIC_FIELD)
    }

    fun sensorAccelerometer(event: SensorEvent): SensorResult {
        accelGravity[0] = ALPHA * accelGravity[0] + (1 - ALPHA) * event.values[0]
        accelGravity[1] = ALPHA * accelGravity[1] + (1 - ALPHA) * event.values[1]
        accelGravity[2] = ALPHA * accelGravity[2] + (1 - ALPHA) * event.values[2]
        hasGravity = true

        accelLin[0] = event.values[0] - accelGravity[0]
        accelLin[1] = event.values[1] - accelGravity[1]
        accelLin[2] = event.values[2] - accelGravity[2]

        gravity[0] = accelGravity[0]
        gravity[1] = accelGravity[1]
        gravity[2] = accelGravity[2]

        val display = "Accelerometer:\nX: %.2f\nY: %.2f\nZ: %.2f".format(accelLin[0], accelLin[1], accelLin[2])
        return SensorResult(display, accelLin.copyOf())
    }

    fun sensorGyroscope(event: SensorEvent): SensorResult {
        val values = event.values
        val display = "Gyroscope:\nX: %.2f\nY: %.2f\nZ: %.2f".format(values[0], values[1], values[2])
        return SensorResult(display, values.copyOf())
    }

    fun sensorProximity(event: SensorEvent): SensorResult {
        val distance = event.values[0]
        return SensorResult("Proximity: %.2f cm".format(distance), floatArrayOf(distance))
    }

    fun sensorMagnetometer(event: SensorEvent): SensorResult {
        geomagnetic[0] = event.values[0]
        geomagnetic[1] = event.values[1]
        geomagnetic[2] = event.values[2]
        hasMagnet = true

        return SensorResult("Magnetometer:\nX: %.2f\nY: %.2f\nZ: %.2f".format(geomagnetic[0], geomagnetic[1], geomagnetic[2]), geomagnetic.copyOf())
    }

    fun getCompassReading(): SensorResult {
        if (!hasGravity || !hasMagnet) return SensorResult("Compass: Unavailable", floatArrayOf())

        val R = FloatArray(9)
        val I = FloatArray(9)

        return if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(R, orientation)
            val azimuthRad = orientation[0]
            val azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
            val normalized = (azimuthDeg + 360) % 360
            val direction = getCompassDirection(normalized)

            SensorResult("Compass: ${normalized.roundToInt()}° ($direction)", floatArrayOf(normalized))
        } else {
            SensorResult("Compass: Unable to calculate", floatArrayOf())
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
