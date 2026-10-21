# TOSS — Taly Open Smart System

> An experimental study in turning low-cost Android projector hardware into a lightweight, controlled and measurable smart-display platform.

TOSS is an open engineering and research project built around a simple question:

**How far can inexpensive Android projector hardware be improved when the system is designed specifically around its real limitations?**

Rather than starting from a generic Android ROM or rewriting hardware support that already works, TOSS studies the device from the bottom up: hardware, boot chain, Android framework, OEM services, media pipeline, projector controls, memory usage, thermal behavior and user experience.

The current research target is an **HCS350 / H713-class Android projector** with approximately 1 GB of RAM. The project intentionally treats the original firmware as both a working reference and a source of hardware knowledge.

TOSS is not an attempt to replace Linux, Android or vendor drivers for the sake of doing so. The objective is to determine the **minimum stable runtime** required by the hardware and build the TOSS experience on top of it.

---

## Research goals

TOSS currently investigates four main areas:

1. **Hardware reverse engineering** — understand how the projector actually exposes HDMI, focus, keystone, display, media, audio, input, networking and thermal management.
2. **Android/OEM dependency mapping** — determine which framework, vendor and OEM components are critical, replaceable, optional or still unknown.
3. **Resource-constrained system design** — reduce unnecessary RAM, CPU, I/O and background activity without sacrificing hardware acceleration or stability.
4. **A purpose-built TV/projector UX** — replace the fragmented OEM interface with a lightweight TOSS shell designed for D-pad, television-distance viewing and companion-device control.

The project favors **measurement over assumptions**. Components are not removed merely because they look unnecessary; their responsibilities and dependencies must first be observed and tested.

---

## Design philosophy

The long-term target is internally referred to as **TOSS Runtime**.

Instead of building another heavy Android distribution, TOSS aims to preserve the parts that already solve difficult hardware problems:

- Linux kernel and board support
- vendor drivers and HALs
- SurfaceFlinger and the display/GPU pipeline
- MediaCodec / Codec2 / OMX and hardware video decoding
- AudioFlinger and vendor audio HAL
- OEM HDMI pipeline
- Wi-Fi and Bluetooth stacks
- Android input infrastructure
- thermal and cooling management
- projector-specific hardware services that prove necessary

TOSS then replaces or consolidates the user-facing and orchestration layers wherever doing so provides a measurable benefit.

```text
┌───────────────────────────────────────────────┐
│                   TOSS UI                     │
│ Home │ Apps │ Cinema │ Files │ Settings       │
└───────────────────────┬───────────────────────┘
                        │
              TOSS System Services
       ┌────────────────┼────────────────┐
       │                │                │
 Hardware Service   Remote Service   Diagnostics
       │                │                │
───────┴────────────────┴────────────────┴────────
                 Android Framework
              minimum required runtime
────────────────────────────────────────────────
               OEM / Vendor Runtime
 Display │ Media │ Audio │ HDMI │ Projector
 Network │ Bluetooth │ Input │ Thermal
────────────────────────────────────────────────
              Linux / Allwinner BSP
────────────────────────────────────────────────
                     Hardware
```

A central rule is:

> **TOSS controls. Existing hardware pipelines execute.**

For example, TOSS should not decode video in software when the SoC already provides hardware decoding, nor replace a stable HDMI pipeline merely to make it "more custom".

---

## Current hardware study

Measurements collected from the current HCS350 research unit indicate a platform in the following class:

```text
SoC / platform    Allwinner H713-class / sun50iw12p1
CPU               4× ARM Cortex-A53
Android           Android 11 / API 30
Kernel            Linux 5.4.99
Userspace         32-bit ARM
RAM available     ~932 MiB
Storage           ~8 GB eMMC class
Display stack     Android + Allwinner/vendor pipeline
```

These values describe the current research device and should not be assumed to apply to every projector sold under a similar commercial name.

The limited memory budget is a deliberate part of the experiment. TOSS is being designed around the hardware rather than assuming additional RAM or faster hardware will solve architectural problems.

---

## What has been discovered so far

### HDMI

The projector uses an OEM/Allwinner television-input path. TOSS can launch the existing HDMI path rather than implementing capture or rendering itself.

### Focus

Physical focus controls ultimately reach the projector's motor-control path. Android key events corresponding to focus increase/decrease have been identified during experimentation. The remaining work is to expose this safely through the privileged TOSS hardware layer rather than granting unnecessary privileges to the UI.

### Keystone

The OEM implementation has revealed a vendor-specific display path used to apply keystone geometry. This is treated as an implementation backend, not as a stable public Android API. Integration must therefore be guarded by device identification, bounds checking and hardware testing.

### Media and audio

The existing Android/vendor media and audio stacks are considered assets rather than targets for replacement. Hardware video decoding and the native audio pipeline should remain available to applications running under TOSS.

### Remote control

TOSS includes work on local-network companion control. The intended architecture separates network-facing code from privileged hardware control:

```text
Phone / Tablet
      │
      ▼
TOSS Remote Service
  unprivileged
      │
      ▼
 typed Binder / AIDL
      │
      ▼
TOSS Hardware Service
  privileged, minimal
      │
      ▼
Android / OEM hardware paths
```

The network parser must never become a generic privileged command executor.

---

## Component classification

One of the core research outputs is a component matrix for the stock firmware.

Every package, native process and service begins as **UNKNOWN** until there is enough evidence to classify it.

| Class | Meaning |
| --- | --- |
| `CRITICAL` | Required for system or hardware operation; never remove casually |
| `OEM_REQUIRED` | Vendor component with a demonstrated hardware responsibility |
| `ANDROID_REQUIRED` | Required Android framework/runtime component |
| `REPLACEABLE` | Function can be provided by TOSS while retaining required backend services |
| `OPTIONAL` | Demonstrated unnecessary for the intended TOSS runtime |
| `UNKNOWN` | Responsibility or dependency graph is not yet understood |

The rule is intentionally conservative:

**UNKNOWN components stay enabled.**

A component becomes a removal candidate only after observation, dependency mapping, reversible testing and regression testing.

---

## Performance methodology

Optimization is not measured by how much software can be deleted. It is measured by whether the resulting system is faster, more predictable and still correct.

The project tracks or plans to track:

- process PSS/RSS and total memory pressure
- available RAM and swap/ZRAM activity
- idle and active CPU utilization
- startup and application-launch latency
- rendering performance and jank
- video dropped frames
- storage/I/O behavior
- thermal zones and throttling
- long-running stability
- behavior under repeated application switching

Four representative workloads form the initial benchmark loop:

```text
HOME → YOUTUBE → HDMI → LOCAL MEDIA
```

Measurements from the original firmware provide the baseline. Later TOSS Runtime experiments can then be compared against the same workload rather than relying on subjective impressions.

---

## Resource strategy

TOSS is deliberately optimized for low-memory hardware.

The Home interface uses lightweight Android drawing/UI primitives and short event-driven animations. Expensive permanent effects, continuous animation loops, large decoded bitmaps and unnecessary background work are avoided.

Features should be initialized on demand whenever possible. When a memory-heavy application such as a streaming client is in the foreground, TOSS Home should be disposable and inexpensive to recreate. Small system services that must remain available are kept independent from the Home UI.

Image and icon caches are expected to be bounded by memory rather than allowed to grow indefinitely.

Background work such as indexing, update checks or thumbnail generation should yield to foreground media playback and thermal pressure.

The goal is not to maximize "free RAM". The goal is to minimize unnecessary memory pressure and avoid pathological swapping, process churn and latency.

---

## Repository architecture

```text
apps/
  toss-home/          projector user interface
  toss-control/       companion controller

core/
  capabilities/       capability contracts
  projector-control/  projector control abstraction

devices/
  hcs350/             device-specific evidence and integration

docs/                 architecture and research notes
tools/                survey, development and diagnostic tooling
```

As the project evolves, privileged hardware integration and the remote server are intended to remain separate components/processes rather than becoming part of a monolithic launcher.

TOSS intentionally avoids premature multi-device abstraction. Device-specific interfaces are isolated where useful, but a generalized adapter ecosystem should be driven by evidence from additional real hardware rather than hypothetical devices.

---

## Development phases

### Phase 0 — Device survey

Identify the real board, firmware, partitions, hardware interfaces, Android services, media stack, thermal behavior and recovery constraints.

### Phase 1 — TOSS Shell

Build the lightweight TOSS user experience while delegating hardware operations to known Android/OEM paths. Develop local companion-control prototypes and validate navigation and lifecycle behavior.

### Phase 2 — TOSS System

Introduce a minimal privileged Hardware Service, separate Remote Service, native TOSS settings and diagnostics. Integrate focus, keystone, display controls and global input through narrow typed interfaces.

This phase also establishes the component matrix and performance baseline required for safe runtime reduction.

### Phase 3 — TOSS Runtime / firmware research

Only after recovery is proven and dependencies are mapped, investigate an incrementally reduced version of the existing firmware.

The preferred direction is **reduction of the known-working vendor image**, not blindly replacing it with generic AOSP and attempting to reconstruct proprietary Allwinner/OEM hardware support afterward.

---

## Safety and recovery

Firmware experimentation is deliberately conservative.

The project does **not** treat successful ADB root access as a recovery strategy. Before destructive firmware experiments, the research process must understand the device's boot chain, partition layout, unique device data and practical restore path.

Operations avoided during early research include:

- arbitrary GPIO/sysfs writes
- disabling unknown native or thermal services
- uncontrolled SurfaceFlinger/vendor transactions
- permanent removal of OEM components before dependency testing
- partition writes without a tested recovery path
- exposing unrestricted privileged shell execution through TOSS
- leaving development ADB/root interfaces exposed in a production configuration

Thermal and cooling services are considered safety-critical until proven otherwise and are not optimization targets.

---

## UI research

TOSS also explores what a purpose-built interface for inexpensive projector hardware can look like.

The current direction uses a dark cinematic visual language with violet accents, large television-distance typography, horizontal content surfaces and highly visible D-pad focus states.

The companion interface is not intended to mirror the projector screen. It acts as a second control surface for navigation, keyboard/touchpad input, projector controls, sources, media and diagnostics.

Visual complexity is constrained by the same performance rules as the rest of the project: appearance should not require expensive continuous rendering on the projector.

---

## What TOSS is not

TOSS is not currently:

- a generic Android ROM for arbitrary devices;
- an attempt to replace working vendor drivers with incomplete open-source substitutes;
- a claim that every OEM component is bloatware;
- a root shell exposed behind a custom launcher;
- a benchmark based solely on free-memory screenshots;
- a finished consumer firmware.

It is a **systems-engineering study** of how much control, performance and UX consistency can be extracted from constrained Android projector hardware through measurement, reverse engineering and careful reuse of the existing hardware stack.

---

## Research principles

- Measure before optimizing.
- Observe before modifying.
- Preserve working hardware acceleration.
- Keep privileged code minimal.
- Keep network-facing code unprivileged.
- Prefer typed capabilities over arbitrary shell commands.
- Treat unknown dependencies as important until proven otherwise.
- Optimize for foreground experience, not synthetic free-RAM numbers.
- Make every destructive experiment recoverable.
- Let the real hardware determine the architecture.

---

## Status

TOSS is experimental and under active development. Interfaces, architecture and device-specific findings may change as additional hardware evidence is collected.

The repository is intended to document not only working code, but also the **engineering process**: observations, hypotheses, measurements, failed approaches, dependency mapping and the reasoning behind architectural decisions.

That makes the HCS350 implementation both a functional TOSS target and a reproducible case study in constrained Android systems engineering.

---

**TOSS — Taly Open Smart System**  
*Build less. Measure more. Reuse what already works.*
