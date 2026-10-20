# TOSS Core

This directory will contain platform contracts and capability models shared by TOSS experiences.

Planned domains:

```text
core/
├── capabilities/
├── system-control/
├── projector-control/
├── media-control/
└── control-protocol/
```

The implementation language/module layout will be finalized after Phase 0 and the Compose vs Views benchmark. UI technology must not leak into these contracts.

## Initial contracts

### ProjectorControl

Conceptual operations, all capability-gated:
- focus adjustment
- keystone adjustment
- source selection
- brightness/picture mode
- orientation
- projector-specific status

### SystemControl

Conceptual operations:
- app launch/navigation
- Wi-Fi/Bluetooth state where permitted
- volume/audio
- storage/system status
- sleep/power actions where permitted

### MediaControl

Abstracts media playback capability and backend selection. Standard Android MediaCodec/Media3 behavior must be benchmarked against OEM/vendor decoding before choosing the default player backend.

### Capability Layer

The UI asks whether an operation is supported before exposing or invoking it. A capability can be confirmed available, confirmed unavailable or unknown.

### Control Protocol

The tablet companion talks to TOSS Core through a versioned local protocol, never directly to a device adapter. Transport and authentication details will be specified separately.
