# HushFeed 0.74 settings compatibility — 0.5.1-beta.1

## Reported failure and repair

The user reported Enhanced collection popup failing finalization with a missing/ambiguous SettingsMenuPreference constructor. Reported stack: HushFeed 0.74.0, add-on 0.5.0-beta.1, TikTok 47.1.4, 79 selected patches, Manager 1.36.0-dev.5, patcher 1.15.2-dev.1, Samsung SM-S948U / Android 17 API 37. The exact 79 patch names/options were not supplied; a stress selection is not a reproduction of that exact choice set.

The tagged upstream source confirms that the menu constructor now takes five arguments instead of six; its active-count argument and setActiveCount method are gone. The home menu now uses flat section_* routes rather than the retired Customize/hub_* tree. This add-on updates compile-only signatures, runtime calls, patch-time validation and placement together; it does not remove the safety check or modify HushFeed.

BDCrusher's Settings appears immediately after Feed screen under Your feed. If missing, it follows another available feed route; if no feed routes exist, Personal add-ons precedes Hushfeed's own group. Placement uses stable route keys, not translated feed headings. Existing header/search/quick-setup negative orders are preserved. New headings are added after renumbering so binary insertion sees sorted original rows. Changed counts refresh in our own summary text instead of the removed badge API. Preference files/defaults and both feature engines are unchanged.

## Exact inputs

| Item | Identity |
| --- | --- |
| Add-on | `TikTok-Interaction-Addons-0.5.1-beta.1.mpp` |
| Add-on SHA-256 | `900c0bd68a27a6014aac3d8ed7cfdc7a41757998e4c8475c245d3523e6f6e054` |
| HushFeed | [v0.74.0](https://github.com/SysAdminDoc/HushFeed/releases/tag/v0.74.0), commit `bd6fb96912fd79f849b7692fb6136cc851d209c3`, 135 named patches |
| External bundle SHA-256 | `dba873d7c7ad8624803eb4c9b3627664e4b9ed146f2fd5eae741ddef6426e7bf` |
| Release receipt SHA-256 | `a1ba5544e3b50a8a397e807ba0371ecd97f8b6f8a10c74cbcfdc49da3ac9c5b0` |
| Original input | `com.zhiliaoapp.musically`, 47.1.4 / 2024701040, ARM64/ARMv7, minSDK 23 |
| APK SHA-256 | `4226ed5d3031b68208c29f62d80ac421b281fc74201bc4163d98e442d0991a40` |
| Original signer SHA-256 (recorded fixture identity) | `9041803e91bcb814b4b4399fb5c85a91640b755e5e8ba76813814bf4cf2ab5ba` |

The original local APK was initially absent, then restored by the user at the same Downloads path, with identical SHA-256 and 472,239,331 bytes. Its historical signer identity is retained; this turn's byte identity check establishes the same fixture. A failed diagnostic launch before restoration created an empty run directory but did not execute combined patching. That is not counted as a completed verification run.

Publisher SHA256SUMS, GitHub release asset digest and receipt agree on the external bundle. Fixtures stay ignored under verification/inputs; neither HushFeed nor TikTok is included in delivery. Upstream snapshots are unchanged.

## Automated evidence and limits

**79 tests pass**, zero failures/errors: 31 Collections, 37 Sound, ten common UI/style, one register-wrapper regression. All three runtime NewApi lint checks pass at minSDK 23. New tests cover the modern five-argument/no-badge compile-only contract; flat route placement/idempotence; missing Feed screen fallback independent of translated headings; Personal add-ons placement with Settings-only selection; summary refresh and unchanged preferences. An insertion-order regression found during development was fixed before freezing the bundle. An earlier test-only variable shadowing compile error was also corrected; neither development attempt is presented as a passing build.

Payload audit: **75 unique own runtime classes**, 30 Collections / 33 Sound / 12 common, no duplicate/foreign/compile-only/test classes. Exactly two selectable patches with one unnamed shared dependency. No HushFeed implementation is bundled.

A forced `:patches:generateMetadata --rerun-tasks` rebuild reproduced the exact bundle SHA-256 recorded above. This is repeatability with this pinned local toolchain, not a cross-platform/tool-version guarantee.

JVM/Robolectric tests simulate native interactions; they do not exercise ART, account-specific destinations, real haptics, playlist saves or TikTok servers. The feature behavior/remaining acceptance checklists in SOUND_BUTTON.md and the historical 0.5 report remain relevant. This repair does not add the separately discussed video-recognition feature.

## Combined compilation

Current machine reports use `flat7-hushfeed-0.74.0-*.json` and exact add-on/input/external hashes. They check DEX/resources, no duplicate classes, unselected payload absence, native/public API reference resolution, safe argument wrappers, five-argument constructor/no retired badge reference, and native feedback/detail/width bindings. Supplemental NativeHookAudit checks nine Sound lifecycle/touch/priority/width sites. No historical run is presented as current-hash evidence.

| Selection | Source order | Compiled result |
| --- | --- | --- |
| Both add-ons + all 135 HushFeed patches | Add-on first | [PASS](flat7-hushfeed-0.74.0-both-all-addon-first.json), 75 own classes; nine-hook audit PASS |
| Both add-ons + all 135 HushFeed patches | HushFeed first | [PASS](flat7-hushfeed-0.74.0-both-all-hush-first.json), 75 own classes; nine-hook audit PASS |
| Sound + HushFeed Settings only | Add-on first | [PASS](flat7-hushfeed-0.74.0-sound-minimal-addon-first.json), 45 own classes, no Collections; nine-hook audit PASS |
| Collections + HushFeed Settings only | HushFeed first | [PASS](flat7-hushfeed-0.74.0-collections-minimal-hush-first.json), 42 own classes, no Sound |

All-patches selections mean 135 named HushFeed patches with default patch-time options, not every runtime option enabled and not a recommendation to install everything. It is not the user's exact unspecified 79-patch selection. Default runtime option behavior, custom accent/AMOLED colors and all possible combinations are not exhaustively tested. Missing/duplicate-selection negative runs remain historical rather than current-hash reruns.

Local diagnostic tools remain pinned as documented in BUILDING.md, including patcher 1.15.0. Published HushFeed used patcher 1.15.1; the user's development Manager/patcher and Android 17 have not been executed locally. No Manager UI import, phone installation, ART validation or physical-device acceptance is claimed.

## Installation and phone follow-up

Replace the old local add-on source with 0.5.1-beta.1; never select both copies. In one Morphe Expert-mode run, select original TikTok 47.1.4, HushFeed 0.74.0 Settings plus your usual HushFeed choices, and either/both personal add-ons. Sound remains opt-in at patch selection. Retain the signing key/preferences; do not uninstall as a troubleshooting step for this constructor error.

Confirm the main HushFeed settings page opens, BDCrusher's Settings is under Your feed after Feed screen (or Personal add-ons fallback), both independently selected pages open/back correctly and changed summaries refresh. Verify existing enabled states/counts persist, the inline slider/haptics, native short disc taps, holds, hover and real native destination saving. Hidden controls, Pause/safe mode and device/HushFeed haptic suppression remain respected. Report any new error verbatim, along with selected patch names/options.

Other HushFeed versions/PineDrama are not verified for this add-on. No vendor or prepatched APK is distributed; no signing, installation, upstream mutation or public publishing occurred.

## Debug storage

All earlier scratch is retained as requested. The four completed current runs are:

- `both-all-addon-first-9017970609794892169`
- `both-all-hush-first-10089019392220663049`
- `sound-minimal-addon-first-7052589652202236122`
- `collections-minimal-hush-first-1828131480271377177`

Source packaging excludes build scratch and vendor fixtures. No cleanup is authorized or performed. The final read-only storage inventory is recorded below.

Complete inventory: **22,020,125,075 logical bytes (20.508 GiB), 532,645 files**, zero skipped links/read errors. Earlier 16.476 GiB of retained scratch is included; this update adds about 4.03 GiB plus an empty failed-launch directory. Nothing was deleted. Scratch is regenerable from the pinned tools and exact fixtures; cleanup still requires renewed authorization.
