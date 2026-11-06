# Changelog

All notable changes to **Isai** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.4.0-tonearm-motion] - 2026-11-06
### Added
- Phase 2 / Prompt B: Tonearm motion physics, pointer gestures, and `FakePlayer` simulation.
- `TonearmState` state machine managing needle drop bounce with spring physics (`dampingRatio = 0.55f, stiffness = 200f`), parking animation (`tween(600, FastOutSlowIn)`), touch scale-up ($1.0 \to 1.03$), and deny shake ($\pm 3^\circ$, 3 oscillations over 300ms) when touched without an active track.
- `TonearmInteractiveContainer` gesture detector:
  - $\ge 56\text{dp}$ touch target mapped to headshell center in screen space.
  - Live scrubbing with pointer-to-angle conversion, clamped to $[\alpha_{\text{rest}}, \alpha_{\text{inner}}]$.
  - Haptic feedback tick (`TextHandleMove`) every 5% progress step and tactile pulse (`LongPress`) on crossing disc boundary.
  - Smooth settling into track-follow mode over 150ms upon release on the record.
  - Lifted stop (pause or reset to 0:00) on release off record or fling towards rest ($> 1500\text{ px/s}$).
  - Tap toggle (< 8dp movement) toggling playback.
  - Accessibility semantics (`Role.Slider`, `progressBarRangeInfo`, and custom actions).
- `FakePlayer` timeline simulation: 3:37 duration, 60fps coroutine ticker, play/pause, seek, and `LiftBehavior` (`PAUSE` vs `STOP`).
- Upgraded `TonearmLabScreen` featuring interactive headshell drag, live elapsed/remaining labels, deny shake toggle, and real-time scrollable event log.
- Unit tests in `TonearmStateTest` validating needle bounce, track follow mode, drag override, and playback seeking.

## [0.3.0-tonearm] - 2026-11-06
### Added
- Phase 2 / Prompt A: Tonearm shape, geometry and Canvas drawing.
- Pure Kotlin `TonearmGeometry` solving circle-circle intersection for scale-invariant outer/inner groove tracking ($\alpha_{\text{outer}} \approx 90^\circ$, $\alpha_{\text{inner}} \approx 124^\circ$, $\alpha_{\text{rest}} \approx 62^\circ$).
- `Tonearm` composable with pure Canvas drawing across all 11 parts in normalized AU space.
- `TonearmColors` supporting chrome/metal gradient themes in Dark and Light.
- `TonearmLabScreen` interactive debug environment.
- Pure JVM JUnit tests in `TonearmGeometryTest`.

## [0.2.0-theme-nav] - 2026-11-06
### Added
- Phase 1: Design system tokens and navigation shell.
- `IsaiTheme` with dark (`#0B0B0E`) and light (`#F3F4FF`) palettes, dynamic color support, and `LocalIsaiColors`.
- Dedicated Tonearm and Vinyl hardware metal tokens.
- Reusable UI components with Dark and Light previews.
- Type-safe `@Serializable` navigation graph and `MainScaffold`.

## [0.1.0-setup] - 2026-11-06
### Added
- Phase 0: Project setup and architectural skeleton (`com.isai.player`).
- Gradle Kotlin DSL build configuration with version catalog (`gradle/libs.versions.toml`).
- AndroidManifest with strictly zero internet permissions.
- Vector adaptive app icon and Core SplashScreen API integration.
