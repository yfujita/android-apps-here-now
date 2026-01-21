package io.github.yfujita.herenow.data.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SensorService(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val pressureSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    /**
     * Returns a Flow that emits pressure values in hPa (hectopascals).
     * Emits null if the device does not have a pressure sensor.
     */
    fun getPressureFlow(): Flow<Float?> =
        callbackFlow {
            if (pressureSensor == null) {
                trySend(null)
                close()
                return@callbackFlow
            }

            val listener =
                object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        if (event?.sensor?.type == Sensor.TYPE_PRESSURE) {
                            // values[0] is atmospheric pressure in hPa (millibar)
                            trySend(event.values[0])
                        }
                    }

                    override fun onAccuracyChanged(
                        sensor: Sensor?,
                        accuracy: Int,
                    ) {
                        // No-op
                    }
                }

            sensorManager.registerListener(listener, pressureSensor, SensorManager.SENSOR_DELAY_UI)

            awaitClose {
                sensorManager.unregisterListener(listener)
            }
        }
}
