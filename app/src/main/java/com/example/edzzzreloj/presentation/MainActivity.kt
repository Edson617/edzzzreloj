package com.example.edzzzreloj.presentation
import android.net.Uri
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.edzzzreloj.R
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import java.nio.charset.StandardCharsets
import android.content.Intent


class MainActivity : ComponentActivity(),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener,
    SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var nodeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val boton: Button = findViewById(R.id.boton)
        boton.setOnClickListener {
            Toast.makeText(this, "Abriendo monitor de ritmo cardíaco...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, Clase2::class.java)
            startActivity(intent)
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        heartRateSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_HEART_RATE -> {
                val heartRate = event.values[0]
                Log.d("Sensor", "Heart Rate: $heartRate")
                enviarAlTelefono("/heart_rate", heartRate.toString())
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val aceleracion = event.values
                Log.d("Sensor", "Accelerometer: ${aceleracion.joinToString()}")
                enviarAlTelefono("/accelerometer", aceleracion.joinToString())
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun enviarAlTelefono(path: String, mensaje: String) {
        nodeId?.let {
            Wearable.getMessageClient(this).sendMessage(
                it,
                path,
                mensaje.toByteArray(StandardCharsets.UTF_8)
            ).addOnSuccessListener {
                Log.d("Mensaje", "Enviado exitosamente: $mensaje")
            }.addOnFailureListener {
                Log.e("Mensaje", "Error al enviar: ${it.message}")
            }
        } ?: Log.e("Mensaje", "No se encontró el nodeId")
    }

    private fun obtenerNodeId() {
        val nodeClient = Wearable.getNodeClient(this)
        val tarea = nodeClient.connectedNodes
        Thread {
            try {
                val nodes = Tasks.await(tarea)
                if (nodes.isNotEmpty()) {
                    nodeId = nodes[0].id
                    Log.d("NodeID", "Nodo conectado: $nodeId")
                } else {
                    Log.e("NodeID", "No se encontraron nodos conectados")
                }
            } catch (e: Exception) {
                Log.e("NodeID", "Error al obtener nodo: ${e.message}")
            }
        }.start()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        Wearable.getDataClient(this).removeListener(this)
        Wearable.getMessageClient(this).removeListener(this)
        Wearable.getCapabilityClient(this).removeListener(this)
    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
        Wearable.getMessageClient(this).addListener(this)
        Wearable.getCapabilityClient(this)
            .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE)
    }

    override fun onDataChanged(p0: DataEventBuffer) {}
    override fun onMessageReceived(me: MessageEvent) {
        val mensaje = String(me.data, StandardCharsets.UTF_8)
        Log.d("onMessageReceived", "Recibido: $mensaje desde ${me.sourceNodeId}")
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {}
}
