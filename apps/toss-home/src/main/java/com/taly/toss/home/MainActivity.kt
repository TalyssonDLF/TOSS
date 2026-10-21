package com.taly.toss.home

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import com.taly.toss.core.projector.MockProjectorControl
import com.taly.toss.core.projector.ProjectionSource
import com.taly.toss.core.projector.ProjectorControl
import com.taly.toss.devices.hcs350.Hcs350Detector
import com.taly.toss.devices.hcs350.Hcs350ProjectorAdapter

class MainActivity : Activity() {
    private lateinit var home: TossHomeView
    private lateinit var apps: AppsView
    private lateinit var quickPanel: QuickPanelView
    private lateinit var projector: ProjectorControlView
    private lateinit var projectorControl: ProjectorControl
    private lateinit var appCatalog: AppCatalog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectorControl = if (Hcs350Detector.isCurrentDevice()) Hcs350ProjectorAdapter(this) else MockProjectorControl()
        appCatalog = AppCatalog(this)
        val installed = appCatalog.load()
        val youtube = appCatalog.findYouTube(installed)

        val root = FrameLayout(this)
        home = TossHomeView(
            context = this,
            youtubeIcon = youtube?.let { appCatalog.icon(it) },
            onYouTube = { youtube?.let { appCatalog.launch(it) } ?: Result.failure(IllegalStateException("app não instalado")) },
            onHdmi = { projectorControl.setSource(ProjectionSource.HDMI) },
            onApps = { openApps() },
            onProjector = { openProjector() }
        )
        root.addView(home, FrameLayout.LayoutParams(-1, -1))

        apps = AppsView(this, appCatalog) { closeApps() }.apply { visibility = View.GONE }
        root.addView(apps, FrameLayout.LayoutParams(-1, -1))
        quickPanel = QuickPanelView(this).apply { visibility = View.GONE }
        root.addView(quickPanel, FrameLayout.LayoutParams(-1, -1))
        projector = ProjectorControlView(this, projectorControl) { closeProjector() }.apply { visibility = View.GONE }
        root.addView(projector, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)
    }

    private fun openApps() { quickPanel.visibility=View.GONE; projector.visibility=View.GONE; apps.refresh(); apps.visibility=View.VISIBLE; apps.requestFocus() }
    private fun closeApps() { apps.visibility=View.GONE; home.requestFocus() }
    private fun openProjector() { quickPanel.visibility=View.GONE; apps.visibility=View.GONE; projector.visibility=View.VISIBLE; projector.invalidate(); projector.requestFocus() }
    private fun closeProjector() { projector.visibility=View.GONE; home.requestFocus() }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (event.keyCode == KeyEvent.KEYCODE_PROG_RED) { openProjector(); return true }
            if (event.keyCode == KeyEvent.KEYCODE_MENU || event.keyCode == KeyEvent.KEYCODE_SETTINGS) {
                if (projector.visibility == View.VISIBLE || apps.visibility == View.VISIBLE) return true
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
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }
}
