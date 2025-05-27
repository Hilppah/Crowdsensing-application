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
import androidx.compose.ui.text.style.TextForegroundStyle.Unspecified.alpha
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class HomeFragment : Fragment(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null
    private var accelerometer: Sensor? = null
    private var proximity:Sensor? = null
    private var magnetometer: Sensor? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private lateinit var gyroscopeData: TextView


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
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
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        return view
    }

    override fun onSensorChanged(event: SensorEvent?) {

    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun gps(event: SensorEvent) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude

                    gyroscopeData.text = "Latitude: $latitude\nLongitude: $longitude"
                } ?: run {
                    gyroscopeData.text = "Location is not available"
                }
            }
        return gps(event)
    }

    private fun gyro(event: SensorEvent) {
        if (event != null) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            event.accuracy
            gyroscopeData.text = "Gyroscope:\nX: $x\nY: $y\nZ: $z"
        }
        return gyro(event)
    }

    /* private fun accelerometer (event: SensorEvent){
        if (event!= null){
            accelGravity[0] = alpha * accelGravity[0] + (1 - alpha) * event.values[0]
            accelGravity[1] = alpha * accelGravity[1] + (1 - alpha) * event.values[1]
            accelGravity[2] = alpha * accelGravity[2] + (1 - alpha) * event.values[2]
            accelLin[0] = event.values[0] - accelGravity[0]
            accelLin[1] = event.values[1] - accelGravity[1]
            accelLin[2] = event.values[2] - accelGravity[2]
            this.binding.tvAccelerometerX.text = accelLin[0].formatValue()
            this.binding.tvAccelerometerY.text = accelLin[1].formatValue()
            this.binding.tvAccelerometerZ.text = accelLin[2].formatValue()
        }
        return accelerometer
    } */

    override fun onResume() {
        super.onResume()
        gyroscope?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}

