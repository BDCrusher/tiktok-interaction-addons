# Changelog

## [0.10.0-beta.1] - 2026-11-24

- Port Collections and Sound to global TikTok 47.9.3 and HushFeed 0.84.0.
- Use HushFeed's native renderer and pinned header inside the Collection popup and Sound settings pages. Keep the inline 1–20 collection slider.
- Initialize TikTok's music collection state before dispatching the native add action, and surface the result when TikTok's save job completes.

## [0.9.0-beta.1] - 2026-11-19

- Port Favorites and Sound to original global TikTok 47.8.3 and HushFeed 0.83.0.
- Open BDCrusher's Settings in place using HushFeed's expandable section. In list view, the installed add-ons appear under a fixed heading. Each row opens its feature controls.
- Use HushFeed's independent settings theme for feature pages and keep feed popups on TikTok's live AMOLED, dark or light palette.
- Build for Morphe Patcher 2.4.0. HushFeed requires Manager 2.6.0 or newer.

## [0.8.0-beta.1] - 2026-11-15

- Port Favorites and Sound to original global TikTok 47.7.3 and HushFeed 0.82.0.
- Follow the new live sheet color token, including HushFeed's runtime AMOLED switch.
- Make the record menu TikTok-only, drop music-app lookups, and use the same native 200/500 ms timing choice as Favorites.
- Build for Morphe Patcher 2.3.0. See the compatibility report for checked toolchains and device limits.

## [0.7.1-beta.1] - 2026-10-28

- Fix the translucent orange Favorites and Sound menu fill: read inspected 47.5.3 UISheetFlat1 a27 instead of the old a24 token.
- Require opaque surface colors and preserve black AMOLED, custom dark colors and light mode. Share the validated resolver across both features.
- Add four regression tests that reproduce the exact native orange value before the fix and pass afterward. Phone appearance acceptance remains pending.

## [0.7.0-beta.1] - 2026-10-23

- Verified the unchanged bundle against HushFeed 0.79.0 on 2026-10-25: all 140 global TikTok patches in both source orders, each feature alone and explicit negative cases. Exclude PineDrama-only selections in the diagnostic.

- Port both independent add-ons to original global TikTok 47.5.3 / 2024705030 and HushFeed 0.78.0.
- Update inspected native collections, loader/Rx, sound binding, Activity, music-service, save-action and haptic contracts.
- Use pinned Morphe patcher 2.0.0; preserve settings, gestures, preferences and isolated payloads.
- Audit complete DEX output under STRIP_FAST and provide confined local builds plus direct JVM test execution for Windows apostrophe paths.
- Local beta only; device acceptance and publication remain separate.

## [0.6.0-beta.1] - 2026-10-19

- Port both independent add-ons to original global TikTok 47.4.3 / 2024704030 and external HushFeed 0.76.0.
- Bind collection rendering in the new inlined response consumer; keep native collection order, callbacks and pagination.
- Abort a failed enhanced construction before show and retry stock exactly once in a separate argument-preserving frame.
- Update native disc containers, lifecycle, edge-speed guard, song-service forwarding and Favorites haptics; keep the inline slider and existing preferences.
- Preserve the existing BDCrusher's Settings home placement and five-argument UI contracts.
- Published HushFeed and unreleased main are distinguished. Device acceptance and any public publication are separate steps; see COMPATIBILITY.md.

## [0.5.1-beta.1] - 2026-10-15

- Initial GitHub-hosted Morphe source with automatic source update checks and a one-tap add link.
- Compatible settings integration for HushFeed 0.74.0: five-argument menu constructor, no removed badge calls, flat home placement.
- Enhanced collection popup and Sound button actions remain independently selectable in one bundle.
- Existing inline slider, native feedback, sound-detail lookup, tap replay and preferences are retained.
- Local evidence: 79 unit/simulated tests, API lint, four combined builds, nine-hook audits and repeatable bundle hash. Phone acceptance is still pending.

## [0.5.0-beta.1] - 2026-10-11

- Inline collection slider with immediate saving/native feedback, native sound-detail lookup and shared Favorites popup styling.
- Full-window short-tap replay and popup-origin hover correction.
- Historical target HushFeed 0.71.1; not compatible with HushFeed 0.74.0 settings.
