# TOSS Architecture

## Product definition

TOSS is a middleware-oriented Android platform for smart projectors/displays. The stock firmware remains responsible for board-specific kernel, display pipeline, drivers, HALs and proprietary services. TOSS provides a consistent UX and a capability-driven integration layer above it.

## Core layers

```text
                    User Experiences
      Home / Settings / Cinema / Diagnostics
                           |
                        TOSS Core
                           |
      +--------------------+--------------------+
      |                    |                    |
SystemControl       ProjectorControl       MediaControl
      |                    |                    |
      +------------- Capability Layer ----------+
                           |
                     Device Adapter
                           |
      +--------------------+--------------------+
      |                    |                    |
Android APIs        OEM interfaces         Vendor layer
                Intent/Binder/JNI/etc.    HAL/sysfs/etc.
      +--------------------+--------------------+
                           |
                    Stock Android
                           |
               Kernel + vendor + drivers
```

## Backend escalation order

TOSS should use the least invasive mechanism that satisfies a capability:

1. Standard HOME / Android APIs
2. Device Owner where supported and useful
3. Explicit OEM interfaces (Intent, Broadcast, Binder, service APIs)
4. Privileged/system integration only when required
5. Firmware modification only as a last resort

Accessibility is not a foundational system-control backend. Direct `/dev/input` reading is a reverse-engineering/debug technique, not the default application architecture.

## Capability model

Device capabilities have three base discovery states:

- `true`: confirmed available
- `false`: confirmed unavailable
- `null`: unknown / not yet tested

Research notes should distinguish:

- **CONFIRMED** — directly verified
- **OBSERVED** — behavior directly seen but mechanism may be unknown
- **INFERRED** — engineering hypothesis supported by evidence
- **UNKNOWN** — not established

Runtime capability detection should be preferred where feasible. `device.json` documents known characteristics but must not become an unquestioned source of truth after OEM firmware changes.

## Device adapters

Board/vendor-specific integration belongs under `devices/<device>/adapter/`. The rest of TOSS should depend on contracts such as `ProjectorControl`, not concrete HY320 paths or vendor package names.

Examples of anti-patterns:

- `if (model == "HY320")` spread through UI code
- hard-coded sysfs paths in TOSS Home
- direct vendor Binder calls from presentation code
- assuming every product sold as HY320 uses the same SoC/board

## TOSS Control

A companion tablet interface communicates with the projector host through a versioned TOSS Control Protocol.

```text
TOSS Control (tablet)
        |
 Wi-Fi / local network
        |
TOSS Control Protocol
        |
     TOSS Core
        |
 ProjectorControl
        |
   Device Adapter
```

Wi-Fi/local networking is the primary transport for state synchronization, touchpad/keyboard, media browsing and file transfer. Bluetooth is a capability-dependent secondary transport for pairing or basic controls.

The tablet never talks directly to an HY320-specific adapter.

## UI baseline

Projector:
- target baseline: 1280×720
- D-pad/focus-first navigation
- dark cinematic surfaces
- violet/purple primary accent
- large rounded translucent/near-black cards
- strong violet glow for focused selection
- minimal top-right status area
- no unnecessary top-left branding/greeting on Home

Tablet:
- same design language
- touch-first layout
- remote/D-pad + touchpad + keyboard
- Now Playing
- focus/keystone/brightness/picture controls where capabilities exist
- source/app/media/file controls
- diagnostics and power/sleep controls where available

## Firmware policy

A modified ROM is optional, not an architectural destination. TOSS 0.1 should be useful on stock firmware. If a later device requires privileged integration, changes should be minimal and preserve vendor/HAL/kernel behavior wherever possible.
