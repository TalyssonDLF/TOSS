package com.taly.toss.core.projector

import com.taly.toss.core.capabilities.ProjectorCapabilities
import com.taly.toss.core.capabilities.Support

/** Development backend. Never touches projector hardware. */
class MockProjectorControl : ProjectorControl {
    private var current = ProjectorState(
        focus = 50,
        keystone = 0,
        brightness = 72,
        zoom = 100,
        pictureMode = PictureMode.CINEMA,
        source = ProjectionSource.TOSS,
        orientation = Orientation.NORMAL,
        aspectRatio = AspectRatio.AUTO
    )

    override fun capabilities() = ProjectorCapabilities(
        focus = Support.SUPPORTED,
        keystone = Support.SUPPORTED,
        brightness = Support.SUPPORTED,
        zoom = Support.SUPPORTED,
        orientation = Support.SUPPORTED,
        aspectRatio = Support.SUPPORTED,
        hdmiInput = Support.SUPPORTED,
        pictureModes = Support.SUPPORTED
    )
    override fun state() = current
    override fun setFocus(value: Int) = update { copy(focus = value.coerceIn(0, 100)) }
    override fun setKeystone(value: Int) = update { copy(keystone = value.coerceIn(-20, 20)) }
    override fun setBrightness(value: Int) = update { copy(brightness = value.coerceIn(0, 100)) }
    override fun setZoom(value: Int) = update { copy(zoom = value.coerceIn(50, 150)) }
    override fun setPictureMode(mode: PictureMode) = update { copy(pictureMode = mode) }
    override fun setSource(source: ProjectionSource) = update { copy(source = source) }
    override fun setOrientation(orientation: Orientation) = update { copy(orientation = orientation) }
    override fun setAspectRatio(aspectRatio: AspectRatio) = update { copy(aspectRatio = aspectRatio) }
    private fun update(block: ProjectorState.() -> ProjectorState): Result<Unit> = runCatching { current = current.block() }
}
