# Anime Platform — Project Plan

> Living document. Update it whenever a decision is made, changed, or an open question gets answered.
> Last updated: 2026-09-23

## 1. Vision

A mobile streaming platform for independent animation, owned and run by a single creator. She publishes animated series and episodes; everyone else watches. Think a personal Crunchyroll or a creator-owned Vimeo OTT channel: her work, her brand, no algorithm or third-party platform rules in between.

Tagline (current, from the Welcome screen): *"Independent anime, made and shared without the usual limits."*

## 2. Users and roles

| Role | Who | Can do |
|---|---|---|
| **Creator / Admin** | The creator (one account) | Everything viewers can, plus create/edit/delete series and episodes, upload video |
| **Viewer** | Anyone with the app | Browse, watch, resume playback |

Only the creator can publish. This must be enforced **on the server**, not just by hiding the upload screen in the app. Hiding UI is a convenience; the backend is the security boundary.

## 3. Current status

What exists on `feature/animation-prototype` today:

- Kotlin Multiplatform + Compose Multiplatform project targeting Android and iOS, with all UI in `shared/commonMain`.
- MVVM: `HomeViewModel`, `SeriesDetailViewModel`, `PlayerViewModel`, and a `ContentRepository` interface backed by `FakeContentRepository`.
- Four screens: Welcome, Home (featured banner, continue watching, series grid), Series Detail (episode list with progress), Player.
- Series/Episode model with seed data from Blender open films (Archive.org mirrors; attribution in seed data). Caminandes is a real 3-episode series, so auto-next can be demoed; the other films are 1-episode series.
- Coil 3 for network images; type-safe navigation routes (`@Serializable`).
- Shared `VideoPlayer` composable with common controls (play/pause, scrubbing, fullscreen landscape) over platform engines: Media3 ExoPlayer on Android, AVPlayer on iOS (`expect`/`actual` in `ui/player/`).
- Watch progress is local-only (`WatchProgressRepository`, persisted with multiplatform-settings: SharedPreferences / NSUserDefaults). Saved every 5 s, on pause, on finish, and on leaving the player; episodes count as watched at 95%.
- Player resumes from saved progress, shows a 5-second "Up next" countdown at the end of an episode (Cancel / Play now), and switches episodes in place so fullscreen is kept.
- Three swappable design directions (Neon Night, Ink, Studio) behind a debug Design lab: Unbounded display + Space Grotesk body (OFL), motion tokens that honor reduced-motion, and texture overlays (glow / halftone / grain).
- Viewer chrome is art-forward: full-bleed Home hero with parallax, poster cards (2:3) with press scale, shimmer skeletons, series detail backdrop (blur on Android 12+ and iOS, gradient fallback below), cover-derived accent via kmpalette, and shared-element poster transitions.
- Player: custom controls, 10s double-tap seek, Skip Intro (seeded on Caminandes Ep. 1), end-of-episode card, immersive landscape.
- Admin mode (Phase 4): long-press the "Anime Platform" title on Home to toggle it; tap the "Admin ×" chip to leave. Admins can create/edit/delete series (cover from the photo picker), add/edit/delete episodes (video + optional thumbnail from the picker; duration read automatically; thumbnail falls back to the series cover). Deletes ask for confirmation; deleting a series removes its episodes.
- **Admin mode is UI-only, not a security boundary.** The flag lives in local settings (`AdminSession`). Real write protection comes from Supabase RLS in Phase 5.
- **Admin edits are not persisted yet.** `FakeContentRepository` is in memory, so created/edited content resets when the app restarts. Picked files are device-local URIs (`content://` on Android, a temp-dir copy on iOS) and only play on the device that picked them. Phase 5 replaces both with uploads.
- Unit tests (`./gradlew :shared:testAndroidHostTest`): `PlayerViewModelProgressTest` (save/resume/completion rules), `FakeContentRepositoryTest` (write paths, cascade delete, ordering), `EpisodeEditorViewModelTest` (next-number default, duration probe, cover fallback, edit).
- Verified on the Android emulator end to end: Home banner and grid, playback, scrubbing, fullscreen in/out, Back exits fullscreen, auto-next from Ep. 1 to Ep. 2, progress surviving a force-quit, continue watching, and resume. Admin flows too: toggle on/off, create series with a picked cover, add an episode with a picked video (duration detected, plays from the local URI), edit episode, delete episode, edit series, delete series. On the iPhone 17 Pro simulator, AXe verified images, resume, playback, scrubbing, fullscreen entry, auto-next while fullscreen, and the full admin CRUD flow including PHPicker video selection, duration probing, and playback from the temporary copy. Fullscreen exit still needs one manual tap-through; the overlay now applies landscape safe-area insets so the control is outside the system gesture region.

### Build notes

- **Coil is pinned to 3.5.0** because it's built against the same Skiko (0.144.6) as Compose Multiplatform 1.11.1. Coil 3.6.x uses Skiko 0.150.1. When upgrading Compose, move Coil to the release whose Skiko matches (check the `skiko` dependency in Coil's iOS `.pom`).
- Timestamps use `kotlin.time.Instant` / `kotlin.time.Clock` (standard library); `kotlinx-datetime` isn't needed.
- `MainActivity` declares `configChanges` for orientation/size so entering fullscreen doesn't recreate the Activity and restart the video. `PlayerViewModel` keeps the live position, so other recreations resume in place.
- Back handling uses `NavigationBackHandler` from `org.jetbrains.androidx.navigationevent:navigationevent-compose` (Compose's older `BackHandler` is deprecated).
- Seed licenses come from each Archive.org item's metadata (CC BY 3.0), except Elephants Dream, which uses its official CC BY 2.5. Double-check against Blender Studio's pages before any public demo.

### Resolved issues (from old `main` prototype)

1. ~~Out-of-memory risk from `ImageBitmap`~~ — removed with `Post` / Upload flow.
2. ~~Data lost on rotation~~ — state in ViewModels via `StateFlow`.
3. ~~Stale edit form~~ — Upload screen removed.
4. ~~Back stack grows~~ — `popUpTo` on welcome entry; single-top navigation elsewhere.
5. ~~Main-thread decoding~~ — Coil handles image loading off the UI thread.
6. ~~Double insets~~ — removed redundant `safeContentPadding()`.

## 4. Decisions

Each decision records what was decided, why, and its status. **Confirmed** means agreed with the project owners; **Proposed** means recommended on the prototype branch and awaiting sign-off.

| # | Decision | Status | Rationale |
|---|---|---|---|
| D1 | Kotlin Multiplatform + Compose Multiplatform for Android and iOS | Confirmed | Already in place; one shared UI codebase for both platforms |
| D2 | Content is **animation (video) only**, no comics or image posts | Confirmed | Stated by the owners |
| D3 | One creator publishes; all other users are viewers | Confirmed | It's her platform |
| D4 | Content is organized as **Series → Episodes** | Proposed | Standard for animated shows; supports standalone shorts as one-episode series |
| D5 | MVVM: ViewModels + repository interface between UI and data | Proposed | Survives configuration changes; lets the data source move from fake to real backend without touching screens |
| D6 | Prototype runs on a **fake repository with seed data** | Proposed | Makes the app demoable before any backend exists |
| D7 | Seed content: Blender Studio open films (Creative Commons) | Proposed | Real animation, legally usable, publicly hosted. Attribution per each film's license |
| D8 | Backend: **Supabase** (Postgres, auth, storage, row-level security) | Proposed | Has a Kotlin Multiplatform SDK (`supabase-kt`); RLS enforces "only the creator can write" in one policy |
| D9 | Video hosting: a dedicated video service (**Mux** or **Cloudflare Stream**), not raw file storage | Proposed | Handles transcoding, adaptive streaming, and thumbnails; the app only stores a playback ID/URL |
| D10 | Video player: shared `VideoPlayer` composable via `expect`/`actual`, Media3 (ExoPlayer) on Android, AVPlayer on iOS | Proposed | Native players give the best playback, battery, and format support |
| D11 | Image loading via **Coil 3** | Proposed | Multiplatform, handles downsampling and caching; fixes the out-of-memory issue |
| D12 | Type-safe navigation routes (`@Serializable`) | Proposed | Cleaner argument passing (series ID, episode ID) than string routes |
| D13 | Prototype work happens on branch `feature/animation-prototype` and merges via PR | Proposed | Keeps `main` untouched until the owners review |

## 5. Architecture

```
shared/src/commonMain/kotlin/com/anime/app/
├── App.kt                  # Theme, NavHost, bottom bar
├── navigation/             # Type-safe route definitions
├── model/                  # Series, Episode, WatchProgress
├── data/
│   ├── ContentRepository.kt       # Interface
│   ├── FakeContentRepository.kt   # Seed data for the prototype
│   ├── AdminSession.kt            # Local admin-mode flag (UI only)
│   └── (later) SupabaseContentRepository.kt
├── ui/
│   ├── home/               # HomeScreen + HomeViewModel
│   ├── series/             # SeriesDetailScreen + ViewModel
│   ├── player/             # PlayerScreen + VideoPlayer (expect)
│   ├── admin/              # Series/episode editors, MediaPlatform (expect: picked-file URL, duration probe)
│   └── welcome/
└── theme/

androidMain/  → VideoPlayer actual (Media3)
iosMain/      → VideoPlayer actual (AVPlayer)
```

Data flows one direction: screens observe `StateFlow`s from ViewModels, user actions call ViewModel functions, ViewModels call the repository.

## 6. Data model (draft)

```kotlin
data class Series(
    val id: String,
    val title: String,
    val description: String,
    val coverUrl: String,
    val genres: List<String>,
    val status: SeriesStatus,        // ONGOING, COMPLETED, HIATUS
    val createdAt: Instant,
)

data class Episode(
    val id: String,
    val seriesId: String,
    val number: Int,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val videoUrl: String,            // Later: playback ID from the video service
    val durationSeconds: Int,
    val publishedAt: Instant,
)

data class WatchProgress(            // Stored locally per viewer
    val episodeId: String,
    val positionSeconds: Int,
    val completed: Boolean,
)
```

## 7. Roadmap

### Phase 1 — Foundation
- [x] Create `feature/animation-prototype` branch
- [x] Add ViewModels and `ContentRepository` interface
- [x] Fix surviving bugs (back stack growth, double insets, rotation state); skipped ImageBitmap/stale-edit fixes removed with Phase 2
- [x] Add Coil 3; switch to type-safe routes
- [x] Verify iOS framework compiles (`linkDebugFrameworkIosSimulatorArm64`)

### Phase 2 — Model and seed data
- [x] Implement Series/Episode model
- [x] `FakeContentRepository` seeded with Blender open films (Archive.org mirrors; attribution in seed data)
- [x] Replace `Post` and the old Stories/Upload screens

### Phase 3 — Viewer experience
- [x] Home: featured banner, continue watching, series grid
- [x] Series detail: cover, description, episode list with progress, tap episode to play
- [x] Player: play/pause, scrubbing, fullscreen landscape
- [x] Player: resume position (persisted locally), auto-next episode with countdown
- [x] System Back in fullscreen exits fullscreen
- [ ] Full iOS simulator run of the player (required to close Phase 3)
- [ ] Nice to have: higher-resolution cover art (Archive.org thumbnails are small and letterboxed)

### Phase 4 — Admin mode
- [x] Hidden toggle to enter admin mode (placeholder for real auth): long-press the Home title
- [x] Create/edit/delete series
- [x] Add episode: pick video (`FileKitType.Video`) and thumbnail, enter metadata; duration probed via `MediaMetadataRetriever` / `AVURLAsset`
- [x] Edit/delete episodes
- [x] iOS tap-through of the admin flows (PHPicker video pick and playback of the temp-dir copy)

### Production design interlude
- [x] Theme module with tokens, 3 swappable directions, OFL fonts, reduced-motion
- [x] Debug component gallery / Design lab
- [x] Home: full-bleed hero, parallax, continue watching, poster grid, skeletons
- [x] Series detail: backdrop blur + gradient fallback, dominant color, shared element, episode list
- [x] Player: custom chrome, double-tap seek, Skip Intro, up-next card, immersive fullscreen
- [x] Welcome logo motion, designed empty/error states, light haptics
- [ ] Replace the working "Anime Platform" name and geometric mark after brand direction is approved
- [ ] Replace low-resolution Archive.org covers with production artwork

### Phase 5 — Backend (after owner review)
- [ ] Supabase project, tables, RLS policy restricting writes to the creator's user ID
- [ ] Auth: creator sign-in (OAuth provider TBD)
- [ ] Video service integration with direct, resumable uploads
- [ ] `SupabaseContentRepository` replaces the fake

## 8. Open questions

These need answers from the creator before or during Phase 3–5.

- **Publishing device:** Will she upload from her phone or a computer? If a computer, a web admin dashboard may beat in-app uploads.
- **Content shape:** Series with episodes, standalone shorts, or both?
- **Viewer interaction:** Comments, likes, follows? Or watch-only?
- **Accounts for viewers:** Do viewers sign in at all, or is watching anonymous?
- **Monetization:** Free, paid subscription, tips, or undecided? This affects backend and app store rules.
- **Video specs:** Typical episode length and file size? Drives hosting cost estimates.
- **Branding:** Final app name, logo, and visual direction.
- **Platforms:** Is iOS required at launch? Does anyone on the team have a Mac?
- **Alternatives:** Has she considered a white-label service (Uscreen, Vimeo OTT)? A custom app is justified if ownership, learning, or a specific experience matters more than speed to launch.

## 9. Out of scope for the prototype

- Real authentication and authorization
- Payments and subscriptions
- Push notifications
- Comments and social features
- Offline downloads
- Analytics

## 10. References

- **Product inspiration:** Crunchyroll (browsing and playback UX), YouTube channel pages, Vimeo OTT and Uscreen (creator-owned video apps), Patreon (creator-first model)
- **Architecture:** JetBrains KMP samples and app template, Now in Android (architecture layering)
- **Backend:** Supabase docs, `supabase-kt`, row-level security guides
- **Video:** Mux and Cloudflare Stream docs on direct uploads and playback; Media3 (ExoPlayer) and AVPlayer docs
- **Seed content:** Blender Studio open movies and their license pages
