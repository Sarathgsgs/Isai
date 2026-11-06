# Changelog

All notable changes to **Isai** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.6.0-vinyl-stage] - 2026-11-06
### Added
- Phase 2 / Prompt D: VinylStage unified assembly, gesture arbitration, and `VinylStageLabScreen`.
- `StageTrack` data model and `SwipeDirection` enum for horizontal disc navigation.
- `StageBackground` composable rendering ambient blurred/darkened album art (Dark theme) or subtle accent gradient (Light theme).
- `VinylStage` composable harmonizing vinyl disc rotation physics, tonearm tracking, and needle drop bounce:
  - Synchronized playback rules: disc spins only when playing and not scrubbing (`isPlaying && !isScrubbing`).
  - Play triggers immediate spin-up with ~700ms tonearm lower animation; pause parks tonearm first followed by disc spin-down ~150ms later.
  - Tonearm lifts during track changes and smoothly lowers if playback continues.
  - Multi-touch gesture arbitration ensuring tonearm hit target takes strict priority over horizontal track swiping.
  - Horizontal swipe on vinyl disc to skip tracks with directional slide transition (left/right) and velocity threshold ($> 800\text{ px/s}$).
- `VinylStageLabScreen` interactive testbed featuring 5 sample tracks, external notification pause simulation, seekbar scrub sync, lift behavior toggle (`PAUSE` vs `STOP`), theme toggle, live event logger, and real-time FPS counter.
- Unit tests in `VinylStageTest` verifying synchronization rules, 3-second track restart threshold, track index wrap-around, and fallback seed generation.

## [0.5.0-vinyl-disc] - 2026-11-06
### Added
- Phase 2 / Prompt C: `VinylDisc` component, 33⅓ RPM rotation physics, and `VinylDiscLabScreen`.
- `VinylDiscState` physics engine with 500ms ease-in spin-up, 900ms ease-out spin-down, angle retention on pause, and frame hitch clamping ($\le 50\text{ms}$).
- `VinylDisc` composable featuring 3 distinct layers:
  - Layer 1 (Static): Ambient radial accent glow (Dark) or soft drop shadow (Light).
  - Layer 2 (Rotating): Full-bleed circular album art covering the entire disc, 12% black depth scrim, 48 concentric groove rings with deterministic pseudo-random alpha, 3 track gap bands, outer rim highlight, and center spindle hole.
  - Layer 3 (Static): Stationary sweep gradient sheen highlights ($20^\circ \dots 50^\circ$ and $200^\circ \dots 230^\circ$) plus faint radial vignette.
- `VinylColors` theme-aware color token holder.
- `VinylFallbackArt` generating deterministic dual-hue diagonal gradients and initials from track title/artist hash.
- `VinylDiscLabScreen` debug dashboard featuring real-time FPS counter, angle/speed readouts, fallback preset switcher, and reduced motion mode.
- Pure JVM unit tests in `VinylDiscStateTest` validating 33⅓ RPM physics, speed decay, $360^\circ$ wrap-around, and 50ms hitch damping.

## [0.4.0-tonearm-motion] - 2026-11-06
### Added
- Phase 2 / Prompt B: Tonearm motion physics, pointer gestures, and `FakePlayer` simulation.
- `TonearmState` state machine managing needle drop bounce with spring physics (`dampingRatio = 0.55f, stiffness = 200f`), parking animation (`tween(600, FastOutSlowIn)`), touch scale-up ($1.0 \to 1.03$), and deny shake ($\pm 3^\circ$, 3 oscillations over 300ms) when touched without an active track.
- `TonearmInteractiveContainer` gesture detector with live scrubbing and haptics.
- `FakePlayer` timeline simulation.
- Upgraded `TonearmLabScreen`.

## [0.3.0-tonearm] - 2026-11-06
### Added
- Phase 2 / Prompt A: Tonearm shape, geometry and Canvas drawing.
- Pure Kotlin `TonearmGeometry` solving circle-circle intersection for scale-invariant outer/inner groove tracking.
- `Tonearm` composable with pure Canvas drawing across all 11 parts in normalized AU space.
- `TonearmColors` and `TonearmLabScreen`.
- Pure JVM JUnit tests in `TonearmGeometryTest`.

## [0.2.0-theme-nav] - 2026-11-06
### Added
- Phase 1: Design system tokens and navigation shell.
- `IsaiTheme` with dark and light palettes, dynamic color support, and `LocalIsaiColors`.
- Reusable UI components with Dark and Light previews.
- Type-safe `@Serializable` navigation graph and `MainScaffold`.

## [0.1.0-setup] - 2026-11-06
### Added
- Phase 0: Project setup and architectural skeleton (`com.isai.player`).
- Gradle Kotlin DSL build configuration with version catalog (`gradle/libs.versions.toml`).
- AndroidManifest with strictly zero internet permissions.
- Vector adaptive app icon and Core SplashScreen API integration.
