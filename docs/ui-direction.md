# TOSS UI Direction

Status: approved baseline for the projector experience.

## Identity

TOSS should feel cinematic, quiet and purpose-built for projection rather than like a generic Android tablet or a Google TV clone.

### Core palette

| Token | Baseline |
|---|---|
| `toss.purple` | `#975BFF` |
| `toss.purpleBright` | `#B889FF` |
| `toss.background` | `#08070D` |
| `toss.surface` | near-black translucent |
| `toss.textPrimary` | `#F6F4FB` |
| `toss.textMuted` | `#B1ABBF` |

The values are an initial implementation baseline, not yet a final design-token contract.

## Projector Home

- no TOSS wordmark in the upper-left;
- no greeting copy;
- minimal status in the upper-right;
- upper region can carry atmospheric/media artwork;
- `Continuar assistindo` is the main hero;
- primary shortcuts form a horizontal row;
- recommendations/content follow below;
- projector controls and settings remain immediately reachable;
- focused elements use violet border/glow, not a large layout shift.

## Interaction

Projector UI is D-pad-first. Every interactive element must have a deterministic focus path, strong focused state and usable target at 1280×720. Touch support is secondary on the projector.

Tablet TOSS Control will share the same visual language but use a touch-first information architecture rather than mirroring the projector screen.
