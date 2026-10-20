# TOSS — Taly Open Smart System

TOSS is an experimental Android-based smart display and projector platform.

The project does **not** begin by replacing the device firmware. Its first goal is to orchestrate the existing OEM Android system through a clean abstraction layer, preserving the vendor kernel, drivers, HALs and proprietary hardware services whenever possible.

## Vision

> Transform OEM Android devices into a consistent TOSS experience without replacing the parts the manufacturer already made work.

TOSS is being designed as a platform rather than a one-off custom ROM. Projector-specific behavior is isolated behind capability detection and per-device adapters.

## Architecture

```text
TOSS Home / Settings / Cinema / Diagnostics
                    |
                TOSS Core
                    |
       +------------+-------------+
       |            |             |
 SystemControl ProjectorControl MediaControl
       |            |             |
       +------ Capability Layer ---+
                    |
              Device Adapter
                    |
 Android APIs / OEM services / vendor interfaces
                    |
          Stock Android firmware
                    |
         Kernel + HAL + drivers
```

A companion tablet application, **TOSS Control**, will communicate with the host through the TOSS Control Protocol. Wi-Fi/local network is the primary transport; Bluetooth may provide basic/fallback control where supported.

## Current status

**Phase 0 — Device Survey / architecture foundation**

The first target is a Magcubic HY320 Mini projector. The exact board/SoC revision is intentionally considered **unknown until measured on the real device**.

No firmware flashing is part of Phase 0.

## Phase 0 goals

1. Device identity
2. Recovery and backup mapping
3. Android capabilities
4. OEM services / HAL / Binder / JNI / sysfs mapping
5. Projector interfaces: focus, keystone, HDMI, display and audio
6. Hardware media decoding
7. Thermal and cooling behavior
8. Input / remote / KeyLayout behavior
9. Security and network audit
10. Performance baseline

See [`docs/phase-0.md`](docs/phase-0.md).

## Repository layout

```text
apps/       TOSS user-facing applications (added after hardware survey/benchmark)
core/       capability-driven platform contracts
 devices/   per-device evidence, research and adapters
 docs/       architecture and engineering documentation
 tools/      development and device-survey tooling
```

## Engineering rules

- Facts, observations, inferences and unknowns must remain distinguishable.
- `true`, `false` and `null` are intentionally different capability states: confirmed yes, confirmed no, and unknown.
- Device Owner is a capability/backend, not a platform dependency.
- Do not disable OEM components before mapping their responsibilities.
- Do not assume the commercial model name identifies the board revision.
- Do not flash or modify firmware before a viable recovery path and backup strategy are understood.
- Prefer standard Android APIs, then Device Owner where useful, then explicit OEM interfaces, then privileged integration only if necessary. Firmware modification is last.

## UI direction

The projector interface uses a dark cinematic visual language with deep violet/purple as the primary accent, large translucent rounded surfaces and a strong focus state optimized for D-pad navigation at 1280×720.

The tablet interface shares the TOSS design system but is not a mirrored projector UI. It is a touch-first control surface for remote navigation, keyboard/touchpad, Now Playing, projector controls, sources, media, files and diagnostics.

## Safety

Phase 0 tooling is intended to be read-only wherever possible. Potentially destructive operations such as bootloader unlocking, partition writes, firmware flashing, disabling critical OEM services, SELinux changes or thermal-control modifications are explicitly outside the initial survey.

---

TOSS is currently an experimental project. Licensing and redistribution strategy for TOSS code and any vendor-dependent integration will be defined before public releases containing distributable software.
