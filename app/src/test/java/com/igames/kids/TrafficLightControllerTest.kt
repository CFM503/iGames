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
}
