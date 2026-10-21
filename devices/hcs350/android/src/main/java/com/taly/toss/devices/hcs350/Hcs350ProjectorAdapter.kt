package com.taly.toss.devices.hcs350

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import com.taly.toss.core.capabilities.ProjectorCapabilities
import com.taly.toss.core.capabilities.Support
import com.taly.toss.core.projector.AspectRatio
import com.taly.toss.core.projector.KeystoneGeometry
import com.taly.toss.core.projector.Orientation
import com.taly.toss.core.projector.PictureMode
import com.taly.toss.core.projector.ProjectionSource
import com.taly.toss.core.projector.ProjectorControl
import com.taly.toss.core.projector.ProjectorState

/**
 * Adapter for the HCS350 / h713m-tuna_p3 projector firmware.
 *
 * It deliberately uses the highest-level OEM interfaces we confirmed on hardware:
 * - HDMI: exported AwLiveTV activity with input_source=HDMI1.
 * - Focus: Android ZOOM_IN/ZOOM_OUT key path, delegated to a minimal privileged bridge.
 * - Keystone: OEM SurfaceFlinger geometry path, delegated to the same privileged bridge.
 *
 * No GPIO, sysfs, remount, root shell or vendor register access belongs here.
 */
class Hcs350ProjectorAdapter(
    context: Context,
    private val privilegedBridge: Hcs350PrivilegedBridge = UnavailableHcs350PrivilegedBridge
) : ProjectorControl {
    private val appContext = context.applicationContext
    private var current = ProjectorState(
        focus = 50, // UI estimate only; the OEM motor exposes relative movement.
        source = ProjectionSource.TOSS
    )

    override fun capabilities() = ProjectorCapabilities(
        focus = Support.SUPPORTED,
        keystone = Support.SUPPORTED,
        brightness = Support.UNKNOWN,
        zoom = Support.UNKNOWN,
        orientation = Support.UNKNOWN,
        aspectRatio = Support.UNKNOWN,
        hdmiInput = Support.SUPPORTED,
        pictureModes = Support.UNKNOWN
    )

    override fun state(): ProjectorState = current

    override fun setFocus(value: Int): Result<Unit> =
        Result.failure(UnsupportedOperationException("HCS350 focus is relative; use adjustFocus(delta)"))

    override fun adjustFocus(delta: Int): Result<Unit> = runCatching {
        require(delta != 0) { "Focus delta must not be zero" }
        val keyCode = if (delta > 0) KeyEvent.KEYCODE_ZOOM_IN else KeyEvent.KEYCODE_ZOOM_OUT
        repeat(kotlin.math.abs(delta)) {
            privilegedBridge.injectKey(keyCode).getOrThrow()
        }
        current = current.copy(focus = ((current.focus ?: 50) + delta).coerceIn(0, 100))
    }

    override fun setKeystone(value: Int): Result<Unit> =
        Result.failure(UnsupportedOperationException("HCS350 uses four-corner keystone geometry"))

    override fun setKeystoneGeometry(geometry: KeystoneGeometry): Result<Unit> =
        privilegedBridge.setKeystoneGeometry(geometry)

    override fun setSource(source: ProjectionSource): Result<Unit> = runCatching {
        when (source) {
            ProjectionSource.HDMI -> openHdmi1()
            ProjectionSource.TOSS -> Unit
        }
        current = current.copy(source = source)
    }

    private fun openHdmi1() {
        val intent = Intent().apply {
            setClassName(AW_LIVE_TV_PACKAGE, AW_LIVE_TV_ACTIVITY)
            putExtra(INPUT_SOURCE_EXTRA, HDMI_1)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        // Explicit components can be launched even when Android package visibility hides
        // them from resolve/query APIs. ActivityNotFoundException is captured by setSource().
        appContext.startActivity(intent)
    }

    override fun setBrightness(value: Int) = unsupported("brightness")
    override fun setZoom(value: Int) = unsupported("zoom")
    override fun setPictureMode(mode: PictureMode) = unsupported("picture mode")
    override fun setOrientation(orientation: Orientation) = unsupported("orientation")
    override fun setAspectRatio(aspectRatio: AspectRatio) = unsupported("aspect ratio")

    private fun unsupported(name: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("HCS350 $name mapping is not confirmed yet"))

    companion object {
        const val AW_LIVE_TV_PACKAGE = "com.softwinner.awlivetv"
        const val AW_LIVE_TV_ACTIVITY = "com.softwinner.awlivetv.MainActivity"
        const val INPUT_SOURCE_EXTRA = "input_source"
        const val HDMI_1 = "HDMI1"
    }
}

interface Hcs350PrivilegedBridge {
    fun injectKey(keyCode: Int): Result<Unit>
    fun setKeystoneGeometry(geometry: KeystoneGeometry): Result<Unit>
}

object UnavailableHcs350PrivilegedBridge : Hcs350PrivilegedBridge {
    private fun unavailable(): Result<Unit> = Result.failure(
        IllegalStateException("TOSS privileged hardware bridge is not installed")
    )

    override fun injectKey(keyCode: Int) = unavailable()
    override fun setKeystoneGeometry(geometry: KeystoneGeometry) = unavailable()
}

object Hcs350Detector {
    fun isCurrentDevice(): Boolean =
        Build.MODEL.equals("HCS350", ignoreCase = true) ||
            Build.DEVICE.equals("h713m-tuna_p3", ignoreCase = true) ||
            (Build.HARDWARE.equals("sun50iw12p1", ignoreCase = true) &&
                Build.MANUFACTURER.equals("Allwinner", ignoreCase = true))
}
