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
        fun onSensorData(sensorType: SensorType, data: Any)
    }

    val lastRecordedTime = mutableMapOf<SensorType, Long>()

    fun startSensors(sensorTypes: Set<SensorType>, rate: Long) {
        for (type in sensorTypes) {
            val sensor = sensorManager.getDefaultSensor(type.androidSensorType) ?: continue
            val sensorListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event ?: return

                    val now = System.currentTimeMillis()
                    val lastTime = lastRecordedTime[type] ?: 0L

                    if (now - lastTime >= rate * 1000) {
                        lastRecordedTime[type] = now

                        val data: Any? = when (type) {
                            SensorType.ACCELEROMETER -> Sensors.sensorAccelerometer(event)
                            SensorType.GYROSCOPE -> Sensors.sensorGyroscope(event)
                            SensorType.PROXIMITY -> Sensors.sensorProximity(event)
                            SensorType.MAGNETIC_FIELD -> {
                                Sensors.sensorMagnetometer(event)
                                Sensors.getCompassReading()
                            }
                        }

                        data?.let { listener.onSensorData(type, it) }
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(sensorListener, sensor, 10000000)
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
