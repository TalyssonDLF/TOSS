# TOSS Apps

Planned user-facing applications:

- **TOSS Home** — projector launcher/home experience
- **TOSS Settings** — simplified system/projector settings
- **TOSS Diagnostics** — performance, thermal, media, input, network and storage diagnostics
- **TOSS Cinema** — media experience; backend chosen only after hardware-decoding tests
- **TOSS Control** — tablet companion/controller

The Android application skeleton is intentionally not locked to Compose or Views yet. Phase 0 will establish the real device constraints, then a small on-device benchmark will choose the projector UI implementation based on RAM, cold start, frame time and D-pad jank.

The approved visual baseline is a dark cinematic UI with deep violet/purple as the primary accent, translucent/near-black rounded cards, purple focus glow and minimal chrome.
