package com.crowdsensing.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.crowdsensing.Sensors
import com.crowdsensing.Sensors.SensorType

class SensorController(
    context: Context,
    private val listener: SensorDataListener
) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensorListeners = mutableMapOf<SensorType, SensorEventListener>()

    interface SensorDataListener {
        fun onSensorData(sensorType: SensorType, result: SensorResult)
    }

    fun startSensors(sensorTypes: Set<SensorType>, samplingRateSeconds: Long) {
        val samplingRateUs = (samplingRateSeconds * 1_000_000L).toInt()

        for (type in sensorTypes) {
            val sensor = sensorManager.getDefaultSensor(type.androidSensorType) ?: continue

            val sensorListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event ?: return
                    val result = when (type) {
                        SensorType.ACCELEROMETER -> Sensors.sensorAccelerometer(event)
                        SensorType.GYROSCOPE -> Sensors.sensorGyroscope(event)
                        SensorType.PROXIMITY -> Sensors.sensorProximity(event)
                        SensorType.MAGNETIC_FIELD -> {
                            Sensors.sensorMagnetometer(event)
                            Sensors.getCompassReading()
                        }
                    }
                    listener.onSensorData(type, result)
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(sensorListener, sensor, samplingRateUs)
            sensorListeners[type] = sensorListener
        }
    }

    fun stopSensors() {
        sensorListeners.values.forEach {
            sensorManager.unregisterListener(it)
        }
        sensorListeners.clear()
    }
}
