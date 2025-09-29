package com.crowdsensing

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.location.Location
import com.crowdsensing.model.Session
import java.time.Instant

object Sensors {
    private const val ALPHA = 0.8f
    private val accelLin = FloatArray(3)
    private val accelGravity = FloatArray(3)
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasMagnet = false

    enum class SensorType(val androidSensorType: Int?) {
        GYROSCOPE(Sensor.TYPE_GYROSCOPE),
        ACCELEROMETER(Sensor.TYPE_ACCELEROMETER),
        PROXIMITY(Sensor.TYPE_PROXIMITY),
        MAGNETIC_FIELD(Sensor.TYPE_MAGNETIC_FIELD),
        GPS(null)
    }

    fun sensorAccelerometer(event: SensorEvent): Session.AccelerometerData {
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

        return Session.AccelerometerData(
            accelX = accelLin[0].toDouble(),
            accelY = accelLin[1].toDouble(),
            accelZ = accelLin[2].toDouble(),
            timestamp = Instant.now()
        )
    }

    fun sensorGyroscope(event: SensorEvent): Session.GyroscopeData {
        return Session.GyroscopeData(
            gyroX = event.values[0].toDouble(),
            gyroY = event.values[1].toDouble(),
            gyroZ = event.values[2].toDouble(),
            timestamp = Instant.now()
        )
    }

    fun sensorProximity(event: SensorEvent): Session.ProximityData {
        return Session.ProximityData(
            proximity = event.values[0].toDouble(),
            timestamp = Instant.now()
        )
    }

    fun sensorMagnetometer(event: SensorEvent) {
        geomagnetic[0] = event.values[0]
        geomagnetic[1] = event.values[1]
        geomagnetic[2] = event.values[2]
        hasMagnet = true
    }

    fun sensorGPS(location: Location): Session.GPSData {
        return Session.GPSData(
            latitude = location.latitude,
            longitude = location.longitude,
            timestamp = Instant.now()
            )}

    fun getCompassReading(): Session.CompassData? {
        if (!hasGravity || !hasMagnet) return null

        val R = FloatArray(9)
        val I = FloatArray(9)

        return if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(R, orientation)
            val azimuthRad = orientation[0]
            val azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
            val normalized = ((azimuthDeg + 360f) % 360f).toDouble()

            Session.CompassData(
                compassData = normalized,
                timestamp = Instant.now()
            )
        } else {
            null
        }
    }

    fun reset() {
        hasGravity = false
        hasMagnet = false
        accelGravity.fill(0f)
        accelLin.fill(0f)
        gravity.fill(0f)
        geomagnetic.fill(0f)
    }
    }
