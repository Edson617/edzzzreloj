package com.example.edzzzreloj.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.edzzzreloj.R
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class Clase2 : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private var texto: TextView? = null

    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ventana2)

        texto = findViewById(R.id.txtSensor)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BODY_SENSORS),
                PERMISSION_REQUEST_CODE
            )
        } else {
            startHeartRateMonitoring()
        }
    }

    private fun startHeartRateMonitoring() {
        if (heartRateSensor == null) {
            Toast.makeText(this, "Sensor de ritmo cardíaco no disponible", Toast.LENGTH_LONG).show()
        } else {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onResume() {
        super.onResume()
        heartRateSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {
            val heartRate = event.values[0].toInt()
            texto?.text = "Ritmo cardíaco: $heartRate bpm"
            Log.d("Clase2", "HR detectado: $heartRate bpm")
            sendHeartRateToPhone(heartRate)
        }
    }

    private fun sendHeartRateToPhone(heartRate: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val nodeClient = Wearable.getNodeClient(this@Clase2)
                val nodes = nodeClient.connectedNodes.await()

                for (node in nodes) {
                    Wearable.getMessageClient(this@Clase2).sendMessage(
                        node.id,
                        "/heart-rate",  // Ruta que usará el teléfono para identificar el mensaje
                        heartRate.toString().toByteArray()
                    ).addOnSuccessListener {
                        Log.d("Clase2", "Mensaje enviado: $heartRate bpm")
                    }.addOnFailureListener {
                        Log.e("Clase2", "Fallo al enviar mensaje", it)
                    }
                }
            } catch (e: Exception) {
                Log.e("Clase2", "Error en la comunicación", e)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Puedes dejarlo vacío
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startHeartRateMonitoring()
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
