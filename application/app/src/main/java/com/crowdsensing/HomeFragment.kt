package com.crowdsensing

import android.Manifest
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class HomeFragment : Fragment(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null
    private var accelerometer: Sensor? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var gyroscopeData: TextView

    private val accelGravity = FloatArray(3)
    private val accelLin = FloatArray(3)
    private val alpha = 0.8f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        gyroscopeData = view.findViewById(R.id.textViewGyro)

        sensorManager = requireContext().getSystemService(SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        return view
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE -> gyro(event)
            Sensor.TYPE_ACCELEROMETER -> accelerometer(event)
        }
    }

    private fun gyro(event: SensorEvent) {
        val (x, y, z) = event.values
        gyroscopeData.text = "Gyroscope:\nX: $x\nY: $y\nZ: $z"
    }

    private fun accelerometer(event: SensorEvent) {
        accelGravity[0] = alpha * accelGravity[0] + (1 - alpha) * event.values[0]
        accelGravity[1] = alpha * accelGravity[1] + (1 - alpha) * event.values[1]
        accelGravity[2] = alpha * accelGravity[2] + (1 - alpha) * event.values[2]

        accelLin[0] = event.values[0] - accelGravity[0]
        accelLin[1] = event.values[1] - accelGravity[1]
        accelLin[2] = event.values[2] - accelGravity[2]

        gyroscopeData.text = accelLin[0].toString()
        gyroscopeData.text = accelLin[1].toString()
        gyroscopeData.text = accelLin[2].toString()
    }

    override fun onResume() {
        super.onResume()
        gyroscope?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

