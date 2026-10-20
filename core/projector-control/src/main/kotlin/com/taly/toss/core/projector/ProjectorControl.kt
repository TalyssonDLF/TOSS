package com.taly.toss.core.projector

import com.taly.toss.core.capabilities.ProjectorCapabilities

enum class PictureMode { CINEMA, GAME, ECO, STANDARD }
enum class ProjectionSource { TOSS, HDMI }
enum class Orientation { NORMAL, INVERTED, MIRRORED }
enum class AspectRatio { AUTO, RATIO_16_9, RATIO_4_3 }

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
    fun setKeystone(value: Int): Result<Unit>
    fun setBrightness(value: Int): Result<Unit>
    fun setZoom(value: Int): Result<Unit>
    fun setPictureMode(mode: PictureMode): Result<Unit>
    fun setSource(source: ProjectionSource): Result<Unit>
    fun setOrientation(orientation: Orientation): Result<Unit>
    fun setAspectRatio(aspectRatio: AspectRatio): Result<Unit>
}
