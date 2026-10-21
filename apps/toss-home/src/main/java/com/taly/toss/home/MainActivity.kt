package com.taly.toss.home

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import com.taly.toss.core.projector.MockProjectorControl
import com.taly.toss.core.projector.ProjectorControl
import com.taly.toss.devices.hcs350.Hcs350Detector
import com.taly.toss.devices.hcs350.Hcs350ProjectorAdapter

class MainActivity : Activity() {
    private lateinit var home: TossHomeView
    private lateinit var quickPanel: QuickPanelView
    private lateinit var projector: ProjectorControlView
    private lateinit var projectorControl: ProjectorControl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectorControl = if (Hcs350Detector.isCurrentDevice()) {
            Hcs350ProjectorAdapter(this)
        } else {
            MockProjectorControl()
        }

        val root = FrameLayout(this)
        home = TossHomeView(this)
        root.addView(home, FrameLayout.LayoutParams(-1, -1))
        quickPanel = QuickPanelView(this).apply { visibility = View.GONE }
        root.addView(quickPanel, FrameLayout.LayoutParams(-1, -1))
        projector = ProjectorControlView(this, projectorControl) { closeProjector() }.apply {
            visibility = View.GONE
        }
        root.addView(projector, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)
    }

    private fun openProjector() {
        quickPanel.visibility = View.GONE
        projector.visibility = View.VISIBLE
        projector.invalidate()
        projector.requestFocus()
    }

    private fun closeProjector() {
        projector.visibility = View.GONE
        home.requestFocus()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (event.keyCode == KeyEvent.KEYCODE_PROG_RED) {
                openProjector()
                return true
            }
            if (event.keyCode == KeyEvent.KEYCODE_MENU || event.keyCode == KeyEvent.KEYCODE_SETTINGS) {
                if (projector.visibility == View.VISIBLE) return true
                quickPanel.visibility = if (quickPanel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                if (quickPanel.visibility == View.VISIBLE) quickPanel.requestFocus() else home.requestFocus()
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // HCS350's Android 11 framework can return a null DecorView when
            // WindowInsetsController is queried before setContentView(). The
            // legacy immersive flags are stable on this firmware and also work
            // across the minimum supported Android versions.
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }
}
