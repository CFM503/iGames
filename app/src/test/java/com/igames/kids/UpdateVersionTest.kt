package com.igames.kids

import com.igames.kids.core.update.UpdateManager
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
}
