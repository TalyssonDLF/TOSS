package com.taly.toss.core.projector

import com.taly.toss.core.capabilities.ProjectorCapabilities

enum class PictureMode { CINEMA, GAME, ECO, STANDARD }
enum class ProjectionSource { TOSS, HDMI }
enum class Orientation { NORMAL, INVERTED, MIRRORED }
enum class AspectRatio { AUTO, RATIO_16_9, RATIO_4_3 }

data class KeystoneGeometry(
    val leftBottomX: Float,
    val leftBottomY: Float,
    val leftTopX: Float,
    val leftTopY: Float,
    val rightTopX: Float,
    val rightTopY: Float,
    val rightBottomX: Float,
    val rightBottomY: Float
)

data class ProjectorState(
    val focus: Int? = null,
    val keystone: Int? = null,
    val brightness: Int? = null,
    val zoom: Int? = null,
    val pictureMode: PictureMode? = null,
    val source: ProjectionSource? = null,
    val orientation: Orientation? = null,
    val aspectRatio: AspectRatio? = null
)

interface ProjectorControl {
    fun capabilities(): ProjectorCapabilities
    fun state(): ProjectorState
    fun setFocus(value: Int): Result<Unit>

    /**
     * Relative focus movement. Real projector motors generally expose direction/steps,
     * not a trustworthy absolute 0..100 position.
     */
    fun adjustFocus(delta: Int): Result<Unit> =
        setFocus(((state().focus ?: 50) + delta).coerceIn(0, 100))

    fun setKeystone(value: Int): Result<Unit>

    /** Exact four-corner geometry for adapters that expose the OEM geometry pipeline. */
    fun setKeystoneGeometry(geometry: KeystoneGeometry): Result<Unit> =
        Result.failure(UnsupportedOperationException("Four-corner keystone is not implemented by this adapter"))

    fun setBrightness(value: Int): Result<Unit>
    fun setZoom(value: Int): Result<Unit>
    fun setPictureMode(mode: PictureMode): Result<Unit>
    fun setSource(source: ProjectionSource): Result<Unit>
    fun setOrientation(orientation: Orientation): Result<Unit>
    fun setAspectRatio(aspectRatio: AspectRatio): Result<Unit>
}
