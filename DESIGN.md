# Prism Glass v0.5 True Glass

## Why the approach changed
v0.1-v0.4 faked glass with `fill`/`fillGradient` rectangles. Alpha and rim tweaks can never
produce refraction or a blurred backdrop, so every attempt looked like a white plate or a
dark panel. v0.5 renders glass on the GPU.

## Pipeline (client only)
1. `GlassBackdrop` copies the already drawn frame once per screen render into a sharp texture
   and a dual-Kawase blurred texture (1/2, 1/4, 1/8 resolution passes).
2. `glass.fsh` draws a rounded-box SDF quad: edge refraction with slight chromatic dispersion,
   Fresnel rim light (cool top-left, warm bottom-right), sheen bands, top halo, edge-hugging
   twinkles, grain, floating shadow. Slots are etched overlays; buttons use a smaller bevel.
3. `FrostedInventoryScreen` drops vanilla's full-screen blur (overrides `renderBlurredBackground`
   and `renderTransparentBackground`; `renderBackground` is NOT overridden because it calls `renderBg`).
4. Any shader or GL failure calls `GlassShaders.fail()` and the v0.4 flat painter takes over.

## Unchanged
Slot positions, clicks, shift-click, crafting and the recipe book are vanilla (the screen
only replaces cosmetic background). Preferences format is unchanged.

## Licensing
Algorithms are public techniques (dual Kawase blur, SDF, Schlick-style falloff). No code from
other mods was copied; ReBlured (MIT), Blur and Blur+ were only reviewed as prior art.

## Known limits
- Shader packs (Iris/Oculus) are untested.
- HUD hearts/hotbar are part of the captured backdrop, so they show through glass.
- Verified in a Forge dev client only; modtest (ATLauncher) check is pending the user.
