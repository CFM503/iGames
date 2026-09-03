package com.igames.kids

import com.igames.kids.games.trafficlight.model.TrafficLightConfig
import com.igames.kids.games.trafficlight.model.TrafficLightState
import com.igames.kids.games.trafficlight.model.TrafficLightStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrafficLightControllerTest {

    @Test
    fun testDefaultConfig() {
        val config = TrafficLightConfig()
        assertEquals(30, config.redDuration)
        assertEquals(3, config.yellowDuration)
        assertEquals(30, config.greenDuration)
        assertEquals(TrafficLightStyle.CLASSIC_3_LAMP, config.style)
        assertTrue(config.isSoundEnabled)
        assertTrue(config.isVoiceEnabled)
        assertEquals(false, config.isTickSoundEnabled)
        assertEquals(63, config.totalCycleDuration)
    }

    @Test
    fun testStateBooleans() {
        val red = TrafficLightState.RED
        assertTrue(red.isRed)
        assertEquals(false, red.isGreen)
        assertEquals(false, red.isYellow)

        val yellow = TrafficLightState.YELLOW
        assertTrue(yellow.isYellow)
        assertEquals(false, yellow.isRed)
        assertEquals(false, yellow.isGreen)

        val green = TrafficLightState.GREEN
        assertTrue(green.isGreen)
        assertEquals(false, green.isRed)
        assertEquals(false, green.isYellow)

        val greenBlink = TrafficLightState.GREEN_BLINK
        assertTrue(greenBlink.isGreen)
    }

    @Test
    fun testCycleNextManualStateVehicle() {
        val config = TrafficLightConfig(style = TrafficLightStyle.CLASSIC_3_LAMP)
        val controller = com.igames.kids.games.trafficlight.engine.TrafficLightController(
            scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined),
            initialConfig = config
        )

        // Starts on RED
        assertEquals(TrafficLightState.RED, controller.currentState.value)

        // Click screen 1: Red -> Green
        controller.cycleNextManualState()
        assertTrue(controller.isManualMode.value)
        assertEquals(TrafficLightState.GREEN, controller.currentState.value)
        assertEquals(30, controller.remainingSeconds.value)

        // Click screen 2: Green -> Yellow
        controller.cycleNextManualState()
        assertEquals(TrafficLightState.YELLOW, controller.currentState.value)
        assertEquals(3, controller.remainingSeconds.value)

        // Click screen 3: Yellow -> Red
        controller.cycleNextManualState()
        assertEquals(TrafficLightState.RED, controller.currentState.value)
        assertEquals(30, controller.remainingSeconds.value)

        // Restore auto mode
        controller.switchToAutoMode()
        assertEquals(false, controller.isManualMode.value)
    }

    @Test
    fun testCycleNextManualStatePedestrian() {
        val config = TrafficLightConfig(style = TrafficLightStyle.PEDESTRIAN)
        val controller = com.igames.kids.games.trafficlight.engine.TrafficLightController(
            scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined),
            initialConfig = config
        )

        // Starts on RED
        assertEquals(TrafficLightState.RED, controller.currentState.value)

        // Click screen 1: Red -> Green (no yellow)
        controller.cycleNextManualState()
        assertTrue(controller.isManualMode.value)
        assertEquals(TrafficLightState.GREEN, controller.currentState.value)

        // Click screen 2: Green -> Red (no yellow)
        controller.cycleNextManualState()
        assertEquals(TrafficLightState.RED, controller.currentState.value)
    }
}
