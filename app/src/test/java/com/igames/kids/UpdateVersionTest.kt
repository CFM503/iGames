package com.igames.kids

import com.igames.kids.core.update.UpdateChannel
import com.igames.kids.core.update.UpdateManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateVersionTest {

    @Test
    fun testVersionComparison() {
        // Newer patch
        assertTrue(UpdateManager.isNewerVersion("1.0.0", "v1.0.1"))
        assertTrue(UpdateManager.isNewerVersion("1.0.0", "1.0.1"))

        // Newer minor
        assertTrue(UpdateManager.isNewerVersion("1.0.0", "v1.1.0"))

        // Newer major
        assertTrue(UpdateManager.isNewerVersion("1.0.0", "v2.0.0"))

        // Same version
        assertFalse(UpdateManager.isNewerVersion("1.0.0", "v1.0.0"))
        assertFalse(UpdateManager.isNewerVersion("1.0.0", "1.0.0"))

        // Older version
        assertFalse(UpdateManager.isNewerVersion("1.1.0", "v1.0.9"))
        assertFalse(UpdateManager.isNewerVersion("2.0.0", "v1.9.9"))
    }

    @Test
    fun testMirrorUrlsBuilder() {
        val raw = "https://github.com/pzeus/iGames/releases/download/v1.0.0/iGames-release.apk"

        // AUTO channel: should include high-speed mirrors and raw fallback
        val autoList = UpdateManager.buildMirrorUrls(raw, UpdateChannel.AUTO, "")
        assertEquals(3, autoList.size)
        assertEquals("https://ghproxy.net/$raw", autoList[0])
        assertEquals("https://gh-proxy.com/$raw", autoList[1])
        assertEquals(raw, autoList[2])

        // DIRECT channel: only raw
        val directList = UpdateManager.buildMirrorUrls(raw, UpdateChannel.DIRECT, "")
        assertEquals(1, directList.size)
        assertEquals(raw, directList[0])

        // CUSTOM channel: custom prefix
        val customList = UpdateManager.buildMirrorUrls(raw, UpdateChannel.CUSTOM, "https://my-proxy.worker.dev/")
        assertEquals(2, customList.size)
        assertEquals("https://my-proxy.worker.dev/$raw", customList[0])
        assertEquals(raw, customList[1])

        // Blank input
        val emptyList = UpdateManager.buildMirrorUrls("", UpdateChannel.AUTO, "")
        assertTrue(emptyList.isEmpty())
    }
}
