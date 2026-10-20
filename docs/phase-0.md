# TOSS Phase 0 — Device Survey

Phase 0 exists to understand the real target hardware before TOSS depends on it. It is intentionally conservative and should avoid modifying the device.

## 1. Device identity

Collect and verify:
- commercial model vs actual manufacturer/board
- SoC and GPU
- RAM and storage
- Android version / SDK
- build fingerprint
- kernel version

The exact HY320 SoC/board is **not confirmed** until this survey is performed.

## 2. Recovery and backup

Before any future firmware modification, map:
- partition layout
- bootloader state
- AVB / verified boot
- secure boot where determinable
- A/B vs A-only layout
- dynamic partitions
- recovery/download modes
- unique/calibration/device-specific partitions

A backup is useful only if a viable restore path exists. Do not overwrite bootloader, calibration, MAC/DRM/device-unique data while experimenting.

## 3. Android capabilities

Test rather than assume:
- Device Owner / DevicePolicyManager
- ADB root availability
- SELinux state
- privileged permissions
- WRITE_SECURE_SETTINGS feasibility
- Treble
- package installation/control capabilities

Device Owner is a backend capability, not a TOSS dependency.

## 4. OEM mapping

Inventory:
- packages and APK paths
- processes
- Android services
- HAL/HIDL/AIDL services where visible
- vendor executables and libraries
- init services
- relevant properties
- JNI/native libraries
- sysfs and `/dev` interfaces where accessible

Do not disable the OEM launcher before establishing whether it also handles special remote keys, source switching, focus, keystone or lifecycle services.

## 5. Projector controls

Map user action to implementation for:
- focus +/-
- keystone
- HDMI/source
- picture modes
- brightness where applicable
- orientation
- audio

Suggested investigation pattern:

```text
physical/user action
  -> logcat / getevent / dumpsys observation
  -> OEM package/service
  -> Intent/Binder/JNI/sysfs/vendor API
  -> hardware behavior
```

Prefer reusing the OEM interface over reimplementing a hardware driver.

## 6. Media acceleration

Compare OEM playback with standard Android playback. Inspect available codec configuration and determine whether hardware decoding is actually used for H.264, HEVC, VP8, VP9 and AV1 where relevant.

Measure CPU load, dropped frames, synchronization and thermal behavior. Do not assume Media3, VLC or the OEM player is best until tested.

## 7. Thermal / cooling

Map the thermal system before debloating anything:
- thermal zones/sensors
- kernel thermal policy
- vendor thermal daemons
- cooling devices
- fan control only if it actually exists
- LED/projector thermal coupling
- throttling behavior

Never assume cooling is software PWM-controlled. Never disable a thermal/cooling-related service without understanding its role.

## 8. Input

Use `getevent -l` as an investigation tool to map IR/Bluetooth/USB input devices and special keys. Collect KeyLayout files from system/vendor where accessible.

Preferred runtime integration order:
1. standard Android KeyEvent
2. OEM Broadcast/Intent
3. OEM service
4. privileged device adapter if necessary

A normal TOSS app should not be designed around directly reading `/dev/input/event*`.

## 9. Security

Before using personal accounts on an unknown white-label firmware, inspect:
- listening services
- outbound network behavior
- privileged packages
- unknown vendor daemons
- OTA/update components
- debug configuration

Treat the target as untrusted until the basic audit is complete.

## 10. Performance baseline

Capture enough data to compare later TOSS builds:
- memory/PSS
- cold start
- UI frame time/jank
- CPU
- storage
- temperature/throttling
- video playback behavior

Compose vs Views/Leanback will be decided by benchmark on real hardware rather than preference alone.

## Phase 0 exit criteria

Phase 0 is sufficiently complete to start TOSS 0.1 when:
- target identity is confirmed
- basic recovery risks are documented
- core Android capabilities are known
- critical OEM/projector paths are mapped enough to avoid breaking them
- input/media/thermal baselines exist
- the device profile contains evidence for major conclusions

Firmware modification is **not** required to exit Phase 0.
