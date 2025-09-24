package com.crowdsensing.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.crowdsensing.Sensors
import com.crowdsensing.Sensors.SensorType
import com.google.android.gms.location.*

class SensorController(
    private val context: Context,
    private val listener: SensorDataListener
) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensorListeners = mutableMapOf<SensorType, SensorEventListener>()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private var locationCallback: LocationCallback? = null

    interface SensorDataListener {
        fun onSensorData(sensorType: SensorType, data: Any)
    }

    val lastRecordedTime = mutableMapOf<SensorType, Long>()

    fun startSensors(sensorTypes: Set<SensorType>, rate: Long) {
        for (type in sensorTypes) {
            when (type) {
                SensorType.GPS -> {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                    ) continue

                    val request = LocationRequest.Builder(rate * 1000)
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .build()

                    locationCallback = object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            val now = System.currentTimeMillis()
                            val lastTime = lastRecordedTime[SensorType.GPS] ?: 0L

                            if (now - lastTime >= rate * 1000) {
                                lastRecordedTime[SensorType.GPS] = now
                                for (location in result.locations) {
                                    listener.onSensorData(SensorType.GPS, Sensors.sensorGPS(location))
                                }
                            }
                        }
                    }
                    fusedLocationClient.requestLocationUpdates(
                        request,
                        locationCallback!!,
                        Looper.getMainLooper()
                    )
                }

                SensorType.MAGNETIC_FIELD -> {
                    val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                    val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                    if (accSensor == null || magSensor == null) continue

                    val listenerCombined = object : SensorEventListener {
                        override fun onSensorChanged(event: SensorEvent) {
                            when (event.sensor.type) {
                                Sensor.TYPE_ACCELEROMETER -> Sensors.sensorAccelerometer(event)
                                Sensor.TYPE_MAGNETIC_FIELD -> Sensors.sensorMagnetometer(event)
                            }

                            val now = System.currentTimeMillis()
                            if (now - (lastRecordedTime[SensorType.MAGNETIC_FIELD] ?: 0L) >= rate * 1000) {
                                lastRecordedTime[SensorType.MAGNETIC_FIELD] = now
                                Sensors.getCompassReading()?.let {
                                    listener.onSensorData(SensorType.MAGNETIC_FIELD, it)
                                }
                            }
                        }

                        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                    }

                    sensorManager.registerListener(listenerCombined, accSensor, SensorManager.SENSOR_DELAY_UI)
                    sensorManager.registerListener(listenerCombined, magSensor, SensorManager.SENSOR_DELAY_UI)
                    sensorListeners[SensorType.MAGNETIC_FIELD] = listenerCombined
                }

                else -> {
                    val androidType = type.androidSensorType ?: continue
                    val sensor = sensorManager.getDefaultSensor(androidType) ?: continue

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
                                    else -> null
                                }
                                data?.let { listener.onSensorData(type, it) }
                            }
                        }

                        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                    }

                    sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_UI)
                    sensorListeners[type] = sensorListener
                }
            }
        }
    }


    private fun startAndroidSensor(type: SensorType, rate: Long) {
        val androidType = type.androidSensorType ?: return
        val sensor = sensorManager.getDefaultSensor(androidType) ?: return
        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event ?: return
                val now = System.currentTimeMillis()
                val lastTime = lastRecordedTime[type] ?: 0L

                if (now - lastTime >= rate * 1000) {
                    lastRecordedTime[type] = now
                    val data: Any? = when (type) {
                        else -> startAndroidSensor(type, rate)
                    }
                    data?.let { listener.onSensorData(type, it) }
                }}
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(sensorListener, sensor, 10000000)
        sensorListeners[type] = sensorListener
    }

    fun stopSensors() {
        sensorListeners.values.forEach {
            sensorManager.unregisterListener(it)
        }
        sensorListeners.clear()

        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
    } }
