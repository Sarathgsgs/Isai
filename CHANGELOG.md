# Changelog

All notable changes to **Isai** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
