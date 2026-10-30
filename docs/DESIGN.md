# Design System — "Midnight Atelier"

**Idea:** a watchmaker's workshop at night. Deep ink surfaces let the faces glow; one violet light source; coral as the single warm accent (the seconds hand, the heart); cream text like paper under a lamp.

## Palette
| Token | Hex | Use |
|---|---|---|
| Ink | `#0B0B12` | background |
| Ink Raised / Card | `#15152A` / `#1C1C33` | surfaces |
| Violet | `#7C5CFF` (soft `#9D86FF`) | primary, logo arc |
| Coral | `#FF5E5B` | secondary, favourites, errors, seconds hand |
| Mint `#5BE7C4` · Amber `#FFC857` · Sky `#4CC9F0` | tertiary / per-face accents |
| Cream | `#F7F3EA` (muted `#B8B4AE`) | text |

Light mode swaps Ink for warm Paper (`#FCFBF8`) and keeps the same accents.

## Type
Serif display (editorial, slightly tight tracking) for screen titles and the wordmark; sans for everything else. On the watch, system fonts via `SYNC_TO_DEVICE` so faces respect user font settings.

## Logo
An open **C** that is also a watch bezel, with two hands at 10:08 and a coral centre cap. Provided as adaptive icon (fore/back/monochrome vectors), SVG, and 192/512/1024 PNG.

## Components
* **FaceCard** – 24 dp radius, circular live preview drawn with Canvas (always shows the real time), heart overlay, status pills.
* **Pill** – 1 px outlined, uppercase 11 sp label; carries OS requirement, installed/active state.
* **Face detail** – accent-tinted hero gradient derived from the face's own colour, so each page feels like that face.

## Watch
Material 3 for Wear with `TransformingLazyColumn`; one idea per screen; status card with circular progress during transfer; destructive action is a small outlined icon button.

## Watch faces
Each face ships 5 colour themes (ColorConfiguration), 3 designer Flavors, and at least one complication slot. Ambient mode: seconds hidden, photo dimmed with a 0.5 s eased transition (WFF v4), everything else stays legible on black.
