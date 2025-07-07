package com.example.edzzzreloj

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.style.LineBackgroundSpan.Standard
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity(),
    CoroutineScope by MainScope(),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {

    lateinit var conectar: Button
    var activityContext: Context? = null
    private val CHEK_MESSAJE = "holi"
    private val deviceConnected: Boolean = false
    private val PAYLOAD_PATH = "/APP_OPEN"
    lateinit var nodeID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityContext = this
        conectar = findViewById(R.id.boton)

        conectar.setOnClickListener {
            if (!deviceConnected) {
                val tempAct: Activity = activityContext as MainActivity
                getNodes(tempAct)
            }
        }
    }

    private fun getNodes(context: Context) {
        launch(Dispatchers.Default) {
            val nodeList = Wearable.getNodeClient(context).connectedNodes
            try {
                val nodes = Tasks.await(nodeList)
                for (node in nodes) {
                    Log.d("NODO", node.toString())
                    Log.d("NODE", "El id del nodo es: ${node.id}")
                }
            } catch (exception: Exception) {
                Log.d("Error en el nodo", exception.toString())
            }
        }
    }

    override fun onDataChanged(p0: DataEventBuffer) {
        // TODO: manejar eventos de datos
    }

    override fun onMessageReceived(ME: MessageEvent) {
    Log.d("onMessageReceived", ME.toString())
        Log.d("onMessageReceived", "ID del nodo ${ME.sourceNodeId}")
        Log.d("onMessageReceived", "Payload: ${ME.path}")
        val message=String(ME.data, StandardCharsets.UTF_8)
        Log.d("onMessageReceived", ME.toString())
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
        // TODO: manejar cambios de capacidades
    }

    override fun onPause() {
        super.onPause()
        try {
            Wearable.getDataClient(activityContext!!).removeListener(this)
            Wearable.getMessageClient(activityContext!!).removeListener(this)
            Wearable.getCapabilityClient(activityContext!!).removeListener(this)
        }catch (e: Exception){
            e.printStackTrace()


        }
    }

    override fun onResume() {
        super.onResume()
        try {
            Wearable.getDataClient(activityContext!!).addListener(this)
            Wearable.getDataClient(activityContext!!).addListener(this)
            Wearable.getCapabilityClient(activityContext!!)
                .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE)
        }catch (e: Exception){
            e.printStackTrace()
        }



    }
}
