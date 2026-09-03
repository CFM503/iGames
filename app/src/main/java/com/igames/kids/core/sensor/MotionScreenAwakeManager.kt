package com.igames.kids.core.sensor

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import kotlin.math.abs

/**
 * Automatically manages screen awake state based on device movement:
 * - Keeps screen awake (FLAG_KEEP_SCREEN_ON) when child is holding or moving the phone.
 * - Releases awake flag after 30 seconds of stillness, allowing phone to sleep naturally.
 */
class MotionScreenAwakeManager(
    private val activity: Activity,
    private val stationaryTimeoutMs: Long = 30_000L // 30 seconds of stillness before allowing sleep
) : SensorEventListener {

    private val sensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val handler = Handler(Looper.getMainLooper())
    private var isScreenKeptOn = false

    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var isInitialized = false

    // Sensitivity threshold for motion detection (filtering out microscopic sensor noise)
    private val motionThreshold = 0.6f

    private val timeoutRunnable = Runnable {
        setScreenAwake(false)
        Log.d("MotionAwakeManager", "Device stationary, released KEEP_SCREEN_ON")
    }

    fun start() {
        // Initial state: keep screen on when user enters screen
        setScreenAwake(true)
        scheduleStationaryCheck()

        accelerometer?.let { sensor ->
            sensorManager?.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        handler.removeCallbacks(timeoutRunnable)
        setScreenAwake(false)
        isInitialized = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        if (!isInitialized) {
            lastX = x
            lastY = y
            lastZ = z
            isInitialized = true
            return
        }

        val deltaX = abs(x - lastX)
        val deltaY = abs(y - lastY)
        val deltaZ = abs(z - lastZ)

        lastX = x
        lastY = y
        lastZ = z

        // If movement exceeds threshold, device is being moved/held
        if (deltaX > motionThreshold || deltaY > motionThreshold || deltaZ > motionThreshold) {
            if (!isScreenKeptOn) {
                setScreenAwake(true)
                Log.d("MotionAwakeManager", "Motion detected, enabled KEEP_SCREEN_ON")
            }
            // Reset timeout timer
            scheduleStationaryCheck()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    private fun scheduleStationaryCheck() {
        handler.removeCallbacks(timeoutRunnable)
        handler.postDelayed(timeoutRunnable, stationaryTimeoutMs)
    }

    private fun setScreenAwake(awake: Boolean) {
        if (isScreenKeptOn == awake) return
        isScreenKeptOn = awake

        activity.runOnUiThread {
            try {
                if (awake) {
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            } catch (e: Exception) {
                Log.e("MotionAwakeManager", "Failed to update screen awake flags", e)
            }
        }
    }
}
