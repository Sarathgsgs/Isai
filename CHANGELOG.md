# Changelog

All notable changes to **Isai** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.10.0-nowplaying-integration] - 2026-11-06
### Added
- Phase 3 / Prompt H: Full Now Playing integration with live VinylStage, interactive seekbar, ControlDial, QueueBottomSheet, and live MiniPlayer.
- `NowPlayingViewModel`: Unified ViewModel decoupling scrub preview from playback ticks, mapping tracks to `StageTrack`, managing favorite toggle, and dispatching tonearm needle actions.
- `NowPlayingScreen`: Complete Now Playing screen integrating `VinylStage` (vinyl disc rotation, tonearm tracking, needle bounce, headshell tap toggle, horizontal disc swipe), seekbar with elapsed and remaining time labels, favorite heart button, and queue modal sheet trigger.
- `ControlDial`: Hardware dial component with shuffle toggle, skip previous (with 3-second restart rule), play/pause with spring scale animation, skip next, and 3-state repeat mode cycling (`OFF` $\to$ `ALL` $\to$ `ONE`).
- `QueueBottomSheet`: Material 3 modal bottom sheet displaying the playing queue, highlighting active song with animated equalizer icon, instant seek on tap, individual track removal, and clear queue action.
- Live `MiniPlayer`: Connected in `MainScaffold` directly to `PlayerController` with real album artwork, title, artist, progress line, and responsive play/pause and skip controls.
- Unit tests in `NowPlayingViewModelTest` verifying scrub preview decoupling, lift behavior honoring `STOP` vs `PAUSE`, and favorite toggling.

## [0.9.0-playback-engine] - 2026-11-06
### Added
- Phase 3 / Prompt G: AndroidX Media3 playback engine, `PlaybackService`, `PlayerController` facade, and audio automation.
- `PlaybackService`: Foreground `MediaSessionService` with ExoPlayer, audio focus management, becoming noisy handling (auto-pause on headphone disconnect), local wake lock, and system MediaStyle notifications with album art and compact actions.
- `PlayerController`: Single source of truth UI facade singleton managing `MediaController` connection, exposing `StateFlow<PlayerState>`, and running a zero-allocation 200ms position ticker active strictly during playback.
- Complete playback controls: play, pause, toggle, seek, 3-second skip-previous restart rule, next with queue wrap-around, shuffle, and 3-state repeat mode cycling (`OFF` $\to$ `ALL` $\to$ `ONE`).
- Queue management: `playTrack`, `playNext`, `addToQueue`, `removeFromQueue`, `reorderQueue`, and `clearQueue`.
- `QueueStore`: Automatic playback queue and position persistence to Room (`QueueDao`) and cold launch restoration.
- `PlayHistoryTracker`: Automatic playback logger recording songs to `HistoryRepository` once played for $\ge 30\text{ seconds}$ (or $\ge 50\%$ for short tracks).
- `SleepTimer`: Background countdown timer supporting 15/30/45/60 min presets and "End of current track", with a smooth 5-second exponential volume fade-out.
- Unit tests: `PlayerStateTest`, `PlayHistoryTrackerTest`, and `SleepTimerTest`.

## [0.8.0-onboarding-scanner] - 2026-11-06
### Added
- Phase 3 / Prompt F: Storage Access Framework folder selection, high-speed recursive scanner, and onboarding.
- `FolderScanner`: Direct `DocumentsContract.buildChildDocumentsUriUsingTree` child cursor querying eliminating slow `DocumentFile` Binder roundtrips, with queue-based recursion and audio format filtering.
- `ArtCache`: RAM-safe embedded album art decoder with `inSampleSize` downsampling to maximum $512 \times 512\text{ px}$ JPEG and atomic file caching.
- `MetadataExtractor`: Bounded `MediaMetadataRetriever` parser with compound track/disc number splitting (`"03/12"` $\to 3$), release year extraction, and filename sanitization fallback for untitled tracks.
- `LibraryScanner`: High-throughput incremental diff engine comparing `lastModified` timestamps to skip unmodified tracks, soft-flagging missing tracks (`isMissing = true`), and updating Room via batched insertions.
- `OnboardingScreen`: Interactive folder picker with SAF permission persistence, real-time scanning progress bar, track counter, completion summary, and automatic transition to Home.
- `OnboardingViewModel`: Connects SAF folder selection to `LibraryScanner` and `SettingsRepository`.
- Start destination routing in `MainScaffold`: automatically routes first-time users to `Onboarding` and returning users to `Home`.
- Unit tests in `FolderScannerTest` and `MetadataExtractorTest`.

## [0.7.0-data-layer] - 2026-11-06
### Added
- Phase 3 / Prompt E: Room database architecture, DAOs, repositories, and typed DataStore settings.
- `Fnv1a`: 64-bit Fowler-Noll-Vo deterministic URI hashing engine for stable, collision-resilient primary keys without auto-increment dependencies.
- Room entities with foreign key cascades:
  - `TrackEntity` (with indexed sorting columns, metadata, art cache path, folder path, missing track flag, and forward-compatible fields for Lyrics and M3U).
  - `PlaylistEntity` and `PlaylistTrackEntity` (composite foreign keys with cascade deletion and explicit position ordering).
  - `FavoriteEntity` (cascade on track deletion).
  - `PlayHistoryEntity` (timestamped playback logging).
  - `QueueItemEntity` (queue restoration on cold start).
- Aggregate projections for derived grouping: `AlbumProjection`, `ArtistProjection`, `FolderProjection`, and `PlaylistWithCount`.
- Comprehensive Room DAOs:
  - `TrackDao` with 8 sort variants, missing track pruning, and search.
  - `AlbumDao`, `ArtistDao`, `FolderDao` with group-by projections.
  - `FavoriteDao` with atomic toggle.
  - `PlaylistDao` with track ordering, reordering, and cascades.
  - `HistoryDao` and `QueueDao`.
- Clean domain models (`Track`, `Album`, `Artist`, `Folder`, `Playlist`, `QueueSnapshot`, `HistoryEntry`) and bidirectional mappers.
- Production `SettingsRepository` backed by AndroidX DataStore Preferences managing `themeMode`, `liftBehavior`, `selectedFolderUri`, `selectedFolderName`, `dynamicAccentColor`, and `sleepTimer`.
- Hilt DI modules: `DatabaseModule`, `RepositoryModule`, and `DataStoreModule`.
- Full unit test suite: `Fnv1aTest`, `TrackEntityMappingTest`, `IsaiDatabaseTest`, and `SettingsRepositoryTest`.

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
