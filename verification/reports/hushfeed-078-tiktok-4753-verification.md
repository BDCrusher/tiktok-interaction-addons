# TikTok 47.5.3 / HushFeed 0.78.0 — local compatibility port

Completed 2026-10-23. Personal version **0.7.0-beta.1**, local only. Source completion, static build checks and simulated tests pass; physical-device acceptance remains pending.

## Exact inputs and delivery

- Original global TikTok `com.zhiliaoapp.musically`, **47.5.3 / 2024705030**, minSDK 23, target/compile SDK 36, ARM64/ARMv7; 476,733,983 bytes. SHA-256 `6670ca3f93bfea379658cad3c27f05d3a380b23a0d82569cce684b7453e8ff3e`.
- Android SDK 36 apksigner verified v1/v2/v3. Original signer SHA-256 `9041803e91bcb814b4b4399fb5c85a91640b755e5e8ba76813814bf4cf2ab5ba`. The supplied APK matches HushFeed's published fixture exactly; the original was preserved.
- External [HushFeed v0.78.0](https://github.com/SysAdminDoc/HushFeed/releases/tag/v0.78.0), source `3b6254c304b52136633606037f4cda74c2371a88`, 6,644,966 bytes; SHA-256 `d87ec98c85d5870c0604bf972372436d7ff20d282c1f8ad6c46614e6e9046417`. Published checksum, GitHub asset digest and release receipt agree. Published October 23 UTC / October 22 Phoenix. The 138-patch global TikTok bundle is the tested input; unreleased main and PineDrama are outside this claim.
- Personal delivery: `dist/TikTok-Interaction-Addons-0.7.0-beta.1.mpp`, **141,232 bytes**, SHA-256 `5e2a1d7c77320c4066bf17268a98e59539721baa3f5353e5bc2ac47b17e629ae`. Generated metadata, payload audit, local source ZIP and SHA256SUMS are alongside it.
- Use **Manager 2.0.0+ / patcher 2.0.0**. Local diagnostics use official patcher tag v2.0.0 at `8c7ff9f29d4cf6395fcbe42a4ceddd034d71fecf`, Gradle plugin 1.3.4 at `a230c33f7eca1e18b07bf1114ca850421520f98d`, Smali `4220ae75f7`, JDK 17 and SDK 36. Old dependency checkouts/releases remain intact.

## What changed

This ports real native bindings rather than widening a target label. Exact package/version/code/signer metadata remains limited to the inspected original.

| Native contract | Inspected 47.5.3 binding |
| --- | --- |
| Collection response / popup / row | `X.0rky.accept`, `X.0rl3`, `X.0rj7` |
| Popup motion / row feedback / hit test | `LJJLL`, `LJJLJLI`, `LJJZZI` |
| List cap / pending flag | `X.035Z.LLFF`, `X.0AwQ.LIZJ/LIZIZ` |
| Native collection page / Rx consumer | `X.0Fgc.d0`, `X.0Fgg`, `X.02sL`, `X.02sd` and validated scheduler/response signatures |
| Cover/title binding / cover inflater | `j4(Object)`, `BaseMusicCoverAssem.lq` |
| Disc touch / shared window dispatch | `X.07Hy.onTouch`, `X.03dx.dispatchTouchEvent`, delegate `X.04Tz` |
| Edge guard | Structural EdgeSpeedupAbility + 300ms Handler timer; resolves `X.0B5S` |
| Music facade / chosen platform / add dispatcher | `X.09AS.LJJJJJ`, `LJIIL`, `LJJIIZI` |
| Lifecycle / entrance / platform label | `X.058M`, `X.02HR`, `X.09AI/09AJ`, `X.0lRJ.LIZIZ` |
| Native sound save / Favorites haptic | `MusicCollectViewModel.hP2`, `X.0kMm(Context).LIZ(0)` |

The save action's coroutine/state branch was inspected: a newly initialized ViewModel takes the native add path. Actual save success outside the sound-page lifecycle is still a phone acceptance item. The nested music-service DEX confirms native provider song-ID, access and availability filtering; the add-on preserves that authenticated path without reading private credential values. Native detail queries and matched-ID/status checks remain.

HushFeed SettingsMenuPreference, SettingsUi and HushfeedPause source contracts remain unchanged across the compared releases; the published bundle was inspected too. Settings argument-preserving wrappers, separate feature selections and preference files remain. Popup renderer fallback, native rows/loaders, inline count slider, current disc bounds, native feedback and pause/hidden-control behavior are retained.

## Completed checks

**83 JUnit/Robolectric tests** pass: 2 patch metadata, 34 Collections, 37 Sound, 10 common. Seven Python release-helper tests pass. All three Android API lint tasks pass. Tests simulate API 28 under Java 17; SDK 36/37 Robolectric execution and Android 17 device behavior are not claimed.

The Windows apostrophe path exposed Gradle's Java test-worker argument-file parsing problem. The retained local launcher executes the same compiled tests using Gradle-exported classpaths, system properties and module working directories. It does not alter expectations. Homes/caches/temp writes stay inside the TikTok checkout; existing shared tools/JDK/SDK are read in place.

Payload audit: **78 unique own classes**, 33 Collections / 33 Sound / 12 common. Exactly two selectable patches with one unnamed shared dependency. No duplicate, foreign, integration-stub or test classes injected. Feature-only combinations confirm the other feature's runtime is absent.

| Selection | Source order | External patches | Result | Supplemental Sound audit |
| --- | --- | ---: | --- | --- |
| both-all | hush-first | 138 | PASS: patch execution and compiled DEX/resources | 9 roles PASS |
| both-all | addon-first | 138 | PASS: patch execution and compiled DEX/resources | 9 roles PASS |
| collections-minimal | hush-first | 1 | PASS: patch execution and compiled DEX/resources | n/a |
| sound-minimal | addon-first | 1 | PASS: patch execution and compiled DEX/resources | 9 roles PASS |
| missing | hush-first | 0 | PASS: missing dependency rejected | n/a |
| sound-missing | addon-first | 0 | PASS: missing dependency rejected | n/a |
| duplicate | addon-first | 4 | PASS: duplicate add-on rejected | n/a |
| sound-duplicate | hush-first | 4 | PASS: duplicate add-on rejected | n/a |

`both-all` uses all 138 named external patches with their default options. It is a stress check, not an installation recommendation or universal runtime option claim. Minimal selections use HushFeed Settings only. Negative cases verify explicit missing Settings and duplicate-selection failures.

Patcher 2's STRIP_FAST result contains only changed entries. An initial minimal audit falsely marked an unchanged host field unresolved. The verifier was corrected to inspect the **complete compiled DEX directory**, retaining strict duplicate/reference checks; the entire final matrix above was then rerun. An earlier stale Rx interface assertion was also updated to the inspected `X.02sd`. Initial failed logs remain under ignored `build/port-4753`; they are not final-candidate success evidence.

The complete final reports are `port9-patches-0.78.0-*.json`. All eight match the exact three input/bundle hashes above. `port9-final-evidence.json` records selections, source orders, scratch paths and pins. Small raw unit/lint/combination/native logs are retained in `port9-local-checks/`. Nine native roles survive in both all-patch orders and Sound alone: cover/title bind/unbind, view creation, native touch, early window dispatch, edge guard and Favorites width observation.

## Source continuity and retained storage

Canonical source: `C:\Morphe\App's\TikTok\Add-ons`. The private root Git repository has no committed baseline; authored changes remain local. Public dev was observed at `ac50c2cd6a045add6fec2f374b3c5daa212313f0` (0.6.0-beta.1), main at `72fc72f7e60c4902768f59518ba82eb87c66cc55` (0.5.1-beta.1). No push/feed/release operation was performed.

Pre-port authored source is retained at `build/port-4753/pre-port-source.zip`, SHA-256 `7045524be846dd23432b8af57dda2bb4b07126e5fe480656146751717da74344`. Native bytecode/resource research, initial failed logs and completed compiled runs remain in ignored `build/port-4753` and `build/verification`. Cache state is in `build/local-build`. These generated directories can consume many GiB and are regenerable from the pinned inputs; no cleanup was performed. Historical deliveries/reports are preserved. No outside-app indexes were changed under the user's TikTok-only write boundary.

## Installation and remaining acceptance

Import the **local 0.7.0-beta.1 .mpp**, replacing the older personal source, and patch the original 47.5.3 APK with HushFeed 0.78.0 Settings plus either/both add-ons in one Expert-mode run. Public feeds still offer older versions. Do not select two add-on versions or repatch an already modified APK. See ../../BUILDING.md and ../../README.md for reproduction and local import instructions.

Settings: **HushFeed → Your feed → BDCrusher's Settings → Collection popup / Sound button actions**, or Personal add-ons when no feed routes exist. Sound remains opt-in. Keep Favorites hold-speed remapping off for the collection hold. Hidden controls, pause/safe mode and Turn off haptics remain authoritative.

No vendor/prepatched APK was distributed, signed or installed; no device was tested. Check actual ART loading/settings, native short taps/holds/cancellation/scrolling, no duplicate/unfavorite saves, account/provider linking and save outcomes, dynamic-feature initialization, themes/AMOLED/custom colors, haptics, rotation, large fonts/RTL and TalkBack. A dedicated accessible sound-disc long-click action remains unimplemented. These beta limitations are not patch-application failures. Phone acceptance and any public publication are separate next steps.
