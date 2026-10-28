# Favorites / Sound opaque AMOLED color repair - 0.7.1-beta.1

Local candidate completed 2026-10-28 in response to the phone report that both popups look translucent orange rather than AMOLED black. The user said all installed versions were latest. Public release checks confirmed personal 0.7.0-beta.1 and HushFeed 0.79.0; the actual phone's full installed configuration was not independently captured.

## Root cause and change

The 0.7.0 shared background reader carried over `a24` (`0x7f0603e5`) as UISheetFlat1. Actual original 47.5.3 resources show:

| Theme path | Dark value | Light value |
| --- | --- | --- |
| Old a24 -> aid / axo | translucent orange #47FE7C02 / #57FE7C02 | translucent orange #1FFE7C02 / #33FE7C02 |
| Correct a27 (0x7f0603e8) -> aig / axr | native opaque dark sheet; HushFeed AMOLED rewrites it to #FF000000 | #FFFFFFFF |

The orange RGB luminance is above the old dark-surface threshold, so the reader skipped the applied AMOLED property and used the translucent orange as the popup fill. This directly reproduces the reported color. The prior research only checked the old token's entry name, which was insufficient to establish its semantic role after the APK version change.

`PopupStyle.sheetColor` now resolves inspected `a27` and accepts only opaque colors. Missing/invalid/translucent values use an opaque day/night fallback. The existing dark-only applied AMOLED/custom-color handling remains. Collections' old duplicate resolver delegates to the same shared reader. Both actual popup engines already use PopupStyle.background; no setting migration, gesture, native save, provider, loader, geometry or haptic change was needed. Native orange resources remain unchanged for their intended uses elsewhere.

## Exact candidate and inputs

- Personal **TikTok-Interaction-Addons-0.7.1-beta.1.mpp**, 141,051 bytes; SHA-256 `7fcf12041e61c1bff39a81bae424c4471812b97691625dbb84350b458e390489`. Local only, not pushed or published. Public feed/release remains 0.7.0-beta.1.
- Original global TikTok **47.5.3 / 2024705030**, com.zhiliaoapp.musically, minSDK23, ARM64/ARMv7. SHA-256 `6670ca3f93bfea379658cad3c27f05d3a380b23a0d82569cce684b7453e8ff3e`; original signer `9041803e91bcb814b4b4399fb5c85a91640b755e5e8ba76813814bf4cf2ab5ba`. The retained original/signature identity matches the published HushFeed fixture.
- HushFeed **0.79.0**, source `02a4c5c5d1fd1123bdcd1f0fa8e6a59f8d8056a7`; bundle SHA-256 `a25056e58abb44ffcd49fd5fa38f9fa315ac9553a113f15c86ea4c9509b069da`.
- Patcher **2.0.0** at `8c7ff9f29d4cf6395fcbe42a4ceddd034d71fecf`, Gradle plugin 1.3.4 at `a230c33f7eca1e18b07bf1114ca850421520f98d`, Smali 4220ae75f7, JDK17/SDK36. Use Manager 2.0.0+ / patcher 2.0.0.

## Verification

Four resource-backed host-theme regression scenarios fail with the old reader, each returning exact orange decimal 1207860226 (#47FE7C02): AMOLED black, light white, custom dark and invalid translucent sheet fallback. They all pass after the fix. These are Robolectric simulations using the inspected native IDs/values, not phone screenshots.

**87 unit/simulated tests pass** (2 patch, 34 Collections, 37 Sound, 14 common), plus seven release-helper tests. All three minSDK23 Android API lint tasks pass. Robolectric uses API28; SDK36/37 and Android17 phone execution are not claimed. Payload audit remains 78 isolated own classes (33 Collections / 33 Sound / 12 common), no foreign/compile-only/test classes and no duplicate payload classes.

| Selection | Source order | External patches | Result | Sound native audit |
| --- | --- | ---: | --- | --- |
| both-all | hush-first | 140 | PASS: patch execution and compiled DEX/resources | 9 roles PASS |
| both-all | addon-first | 140 | PASS: patch execution and compiled DEX/resources | 9 roles PASS |
| collections-minimal | hush-first | 1 | PASS: patch execution and compiled DEX/resources | n/a |
| sound-minimal | addon-first | 1 | PASS: patch execution and compiled DEX/resources | 9 roles PASS |
| missing | hush-first | 0 | PASS: missing dependency rejected | n/a |
| sound-missing | addon-first | 0 | PASS: missing dependency rejected | n/a |
| duplicate | addon-first | 4 | PASS: duplicate add-on rejected | n/a |
| sound-duplicate | hush-first | 4 | PASS: duplicate add-on rejected | n/a |

All eight current-hash `theme10-patches-0.79.0-*.json` reports match the exact three inputs above. Both all-patch runs use all 140 global TikTok patches with default options, excluding PineDrama-only Allow photo comments; this does not establish every runtime setting combination. Each feature alone excludes the other payload. Missing Settings and duplicate selections fail explicitly. Both all-patch orders and Sound alone preserve all nine native hook roles.

AAPT2 inspection of both current compiled resource APKs confirms all four dark `aig` variants are **#FF000000**, both light `axr` entries are **#FFFFFFFF**, and the native orange token remains intact. Direct DEX inspection of the delivered settings.mpe confirms a27 / 0x7f0603e8 and alpha validation, with no old a24 numeric ID in the background resolver. This verifies actual compiled resources/payload as well as source tests.

Machine evidence: `theme-tint-071-evidence.json`; raw tests, before-fix failures, current combination/native logs and resource/DEX probes: `theme-tint-local-checks/`. Build/reproduce using BUILDING.md's confined local launchers. Original APK and upstream bundle were preserved. Canonical source remains Add-ons; no outside-TikTok writes, upstream source edits, phone signing/installation or publication were performed.

## Delivery and remaining work

Import the local **0.7.1-beta.1 .mpp** as the personal source, replacing the old personal source instead of selecting both. Repatch the original APK with HushFeed 0.79.0 Settings and either/both add-ons in one Expert-mode run; select AMOLED dark theme for pure black or its desired custom dark color. Preserve the existing signing key. Source ZIP, metadata, payload audit and checksums are alongside the .mpp under dist. The remote source does not yet supply this repair.

Settings remain HushFeed > Your feed > BDCrusher's Settings. No new toggle is needed. **Phone appearance acceptance remains pending**: verify pure-black Favorites/song surfaces, custom dark color, light mode, corners/text/pressed rows, and existing tap/hold/scroll/cancel/save behavior. Prior provider/account, ART and accessibility limitations remain; a dedicated accessible sound-disc long-click action is still unimplemented.

Before-fix source backup is retained under build/theme-tint-20261028/pre-fix-source.zip (SHA-256 `45220ed3b7964e7f222367b5e3576cff5995a0a3983788d7df6ba56d3e8e5789`). Reports use a new theme10 prefix to preserve old 0.7 evidence. Retained full compiled runs in build/verification and caches in build/local-build consume multiple GiB; no cleanup was requested. Next steps are a phone check of this exact candidate and, if requested, a separately authorized GitHub/Morphe prerelease publication.
