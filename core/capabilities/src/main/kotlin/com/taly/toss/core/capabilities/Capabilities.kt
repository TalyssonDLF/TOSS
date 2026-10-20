package com.taly.toss.core.capabilities

enum class Support { SUPPORTED, UNSUPPORTED, UNKNOWN }

data class ProjectorCapabilities(
    val focus: Support = Support.UNKNOWN,
    val keystone: Support = Support.UNKNOWN,
    val brightness: Support = Support.UNKNOWN,
    val zoom: Support = Support.UNKNOWN,
    val orientation: Support = Support.UNKNOWN,
    val aspectRatio: Support = Support.UNKNOWN,
    val hdmiInput: Support = Support.UNKNOWN,
    val pictureModes: Support = Support.UNKNOWN
)

data class SystemCapabilities(
    val volume: Support = Support.UNKNOWN,
    val sleep: Support = Support.UNKNOWN,
    val wifi: Support = Support.UNKNOWN,
    val bluetooth: Support = Support.UNKNOWN,
    val deviceOwner: Support = Support.UNKNOWN
)

data class MediaCapabilities(
    val hardwareH264: Support = Support.UNKNOWN,
    val hardwareHevc: Support = Support.UNKNOWN,
    val hardwareVp8: Support = Support.UNKNOWN,
    val hardwareVp9: Support = Support.UNKNOWN,
    val hardwareAv1: Support = Support.UNKNOWN
)

data class DeviceCapabilities(
    val projector: ProjectorCapabilities = ProjectorCapabilities(),
    val system: SystemCapabilities = SystemCapabilities(),
    val media: MediaCapabilities = MediaCapabilities()
)
