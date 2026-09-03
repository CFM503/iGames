package com.igames.kids.core.util

import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object SystemUIHelper {
    /**
     * Enters full screen immersive sticky mode:
     * Completely hides status bar and navigation bar.
     * Swiping from edges temporarily reveals transparent bars without shifting layout.
     */
    fun enterFullScreen(activity: Activity?) {
        val window = activity?.window ?: return
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    /**
     * Exits full screen mode, restoring standard system bars.
     */
    fun exitFullScreen(activity: Activity?) {
        val window = activity?.window ?: return
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.show(WindowInsetsCompat.Type.systemBars())
    }
}
