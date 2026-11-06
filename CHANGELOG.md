# Changelog

All notable changes to **Isai** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3.0-tonearm] - 2026-11-06
### Added
- Phase 2 / Prompt A: Tonearm shape, geometry and Canvas drawing.
- Pure Kotlin `TonearmGeometry` solving circle-circle intersection for scale-invariant outer/inner groove tracking ($\alpha_{\text{outer}} \approx 90^\circ$, $\alpha_{\text{inner}} \approx 124^\circ$, $\alpha_{\text{rest}} \approx 62^\circ$).
- `Tonearm` composable with pure Canvas drawing across all 11 parts in normalized AU space: shadow with blur mask filter, counterweight rod, 3-groove counterweight, pivot base with highlight dot, 3-stroke arm tube, bearing collar, tube end cap, rotated headshell with accent stripe, finger lift, cartridge, and stylus tip landing precisely at $T=(0, 100)$.
- `TonearmColors` supporting chrome/metal gradient themes in Dark and Light.
- `TonearmLabScreen` interactive debug environment with angle/lift sliders, live angle/progress readouts, and visual debug overlay for pivot, stylus travel arc, groove limits, and 56dp hit target.
- Pure JVM JUnit tests in `TonearmGeometryTest` verifying angle tolerances, round-trip mapping, clamping, and scale invariance ($R=100$ vs $R=400$).

## [0.2.0-theme-nav] - 2026-11-06
### Added
- Phase 1: Design system tokens and navigation shell.
- `IsaiTheme` with dark (`#0B0B0E`) and light (`#F3F4FF`) palettes, dynamic color support, and `LocalIsaiColors`.
- Dedicated Tonearm and Vinyl hardware metal tokens (`metalLight`, `metalMid`, `metalDark`, `headshell`, `cartridge`).
- Geometric sans typography scale and rounded card/dial shapes.
- Dynamic runtime theme switching (`SYSTEM`, `DARK`, `LIGHT`) backed by `SettingsRepositoryStub`.
- Reusable UI components with Dark and Light previews: `TrackRow`, `AlbumCard`, `SectionHeader`, `EmptyState`, `LoadingState`, `ErrorState`, `MiniPlayer`, `IsaiTopBar`, `IsaiBottomBar`, and `IsaiButtons`.
- Type-safe `@Serializable` navigation graph (`Home`, `Library`, `Favorites`, `Search`, `AlbumDetail`, `ArtistDetail`, `PlaylistDetail`, `NowPlaying`, `Settings`, `Onboarding`, `LabsDashboard`).
- `MainScaffold` with persistent tab navigation, floating `MiniPlayer`, and animated slide-up/down for Now Playing.

## [0.1.0-setup] - 2026-11-06
### Added
- Phase 0: Project setup and architectural skeleton (`com.isai.player`).
- Gradle Kotlin DSL build configuration with version catalog (`gradle/libs.versions.toml`).
- AndroidX Media3, Hilt, Room, Compose BOM, Navigation Compose, Glance, and Palette dependencies.
- AndroidManifest with playback service definitions, `WAKE_LOCK`, `POST_NOTIFICATIONS`, and strictly zero internet permissions.
- Vector adaptive app icon (vinyl disc + note motif) and monochrome themed icon.
- Android 12+ Core SplashScreen API integration (`Theme.Isai.Splash`).
- Developer Labs navigation routing stub (`LabsNavGraph.kt`).
- Detekt static analysis configuration and GitHub Actions CI workflow.
