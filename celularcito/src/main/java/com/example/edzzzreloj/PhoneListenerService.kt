package com.example.edzzzreloj

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class PhoneListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/heart-rate") {
            val heartRate = String(messageEvent.data).toIntOrNull()
            if (heartRate != null) {
                Log.d("PhoneListener", "Ritmo cardíaco recibido: $heartRate bpm")
                // Aquí puedes guardar el dato o mostrarlo
            } else {
                Log.e("PhoneListener", "Dato recibido no válido")
            }
        }
    }
}
