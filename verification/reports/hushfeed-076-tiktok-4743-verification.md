# TikTok 47.4.3 / HushFeed 0.76.0 port — 2026-10-19

## Scope and identities

Two independent selectable patches, Enhanced collection popup and Sound button actions, in TikTok Interaction Add-ons **0.6.0-beta.1**. The user supplied the new original APK and authorized publication of the verified beta to the existing GitHub source. HushFeed remains external and unchanged. No signing, device installation, data clearing or vendor/prepatched APK distribution.

| Input | Identity |
| --- | --- |
| Original APK | `com.zhiliaoapp.musically`, 47.4.3 / 2024704030, 474,921,201 bytes |
| APK SHA-256 | `af7cbe2b00d33ff1339730f6abb8ed7e387a83b27ff2b16748b8f41e6fc87e0b` |
| Original signer SHA-256 | `9041803e91bcb814b4b4399fb5c85a91640b755e5e8ba76813814bf4cf2ab5ba` |
| APK platform/ABI | minSDK 23, targetSDK 36, ARM64/ARMv7 |
| Published HushFeed source | `b68741ca02898c2003751da75df34b44b8943daa`, tag v0.76.0 |
| HushFeed bundle SHA-256 | `da75bf32fc0c8f2839657a16975b31a0a5a3b142b7e3bf9970c58657df27f0c0` |
| Release receipt SHA-256 | `d9dbea1d078974c03874fd6f1e26c39701e74488b2b3f7c9867fbb5af0e649ba` |
| Candidate bundle | `TikTok-Interaction-Addons-0.6.0-beta.1.mpp`, 141,201 bytes |
| Candidate SHA-256 | `c65ae77f2f1add0dcdd6ca9462e204717b0a860639b9b5c68029e59ce7891d10` |

The APK hash matches HushFeed's release receipt. HushFeed's GitHub asset digest, receipt and checksum file agree. Original APK signature verification succeeds; apksigner also emits its standard notices for unprotected META-INF metadata. Inputs and native dynamic-feature archive copies remain ignored, local fixtures.

Local toolchain remains JDK 17, SDK 36/build-tools 36.0.0, Gradle 9.7.1, patcher 1.15.0 and the existing pinned plugin/Smali composites. HushFeed publishes using patcher 1.15.1 and requires Manager 1.34.0+. This is not a device test of the user's Android 17/development Manager combination.

## Inspected source and host boundaries

[Published release](https://github.com/SysAdminDoc/HushFeed/releases/tag/v0.76.0) supports 135 global TikTok patches on 47.4.3 only. Its PineDrama target is not supported by these add-ons. Reference checkout `Projects/TikTok/Research/HushFeed-0.76.0` is pinned to the release commit and remains clean.

Main was inspected at `7d2ecc5eeb8a489a55f79f1d2e6604a8c83084db`, 20 commits ahead. In the public settings fragment it adds search-index refresh on resume/pause, without altering the constructor or hook signatures used here. Main also has unrelated seekbar/font/LIVE/download/privacy changes. They are not in the tested published bundle and are not silently advertised as verified.

| Boundary | 47.4.3 binding / evidence |
| --- | --- |
| Collection construction | `X.0xe4.accept(Object)`, trace `QuickFavoriteManager@2914.showQuickFavoriteDialog$1`; native construction is inlined, no old Qk renderer |
| Collection cap / More | `X.035u.LLFF(Iterable,int)` and native size>=4 branch; validate 25-register native frame, dialog v6, original items v4, count v10 and content root v5 |
| Stock fallback | Original inlined body becomes public synthetic bdcrusherRenderResponse; a two-register accept frame calls runtime response; enhanced installation failure aborts before show and retries once under stock suppression |
| Native collection loader | `FavoriteServiceImpl.LJIILL` constructs `X.0HxR`, field LLJJIII / `X.0HxW.h0`; native Rx scheduler/consumer/disposable descriptors ported together |
| Native popup | `X.0xe6`, list LLJLILLLLZIIL, root LLJLL, LJJZ touch, LJJLJLI pressed, static LJJZZI hit, dismiss |
| Row highlight | `X.0xcR.LLJJIJI` view; native tags and save/More/create callbacks remain stock |
| Disc/title lifecycle | VideoMusicCoverAssem/VideoMusicTitleAssem.l4(Object); native onViewCreated after original body; cover/title unbind |
| Current item / disc containers | ReusedAssem.getItem reads cellProxy; cover qa() confirms primary LLLILZLLLI and alternate LLLIZZ |
| Disc touch / edge priority | `X.07oo.onTouch`; structural EdgeSpeedupAbility + 300ms timer fingerprint resolves `X.0AM7.onTouch`; native Activity/custom dispatcher `X.03uJ` / `X.04UE` |
| Native haptics | `X.0hUu(Context).LIZ(0)`, the routine used by native Favorites |
| External music destinations | `X.0Ady.LJJJJJ(Music)` forwards to IMusicAddToDspCoreService; implementation in libdex_df_music_dsp.so checks native songId, provider/access and availability, preserving native filtering |
| Native add-song context/action | `X.09IJ.LIZ(VideoItemParams)`, VideoMusicTitleAssem.mq(...), `X.0Ady.LJJIJ(...)`; no borrowed keys, private auth extraction or new provider API |
| Native detail client | Existing MusicDetailApi.DetailApi.queryMusic(String,int), MusicDetail.music/matchedPgcMusicInfo and explicit matched sound IDs retained |
| Theme | attr/a24 still resolves to 0x7f0603e5; retain active host palette and applied HushFeed AMOLED/custom color |
| Settings | Existing five-argument SettingsMenuPreference and flat section routes retained; argument-preserving createMasterMenu/onResume wrappers and public-contract checks |

The native sound-service implementation was inspected from the APK's ZIP-shaped ARM64 dynamic feature under ignored `build/research-4743`. It still obtains TT2DSPSongInfos, rejects empty song IDs, then calls native provider/access and availability checks. The add-on passes filtered native objects opaquely; it does not read their credential fields.

## Tests and compiled combinations

**83 JVM/Robolectric tests pass**: patch wrapper/compatibility guards 2, Collections 34, Sound 37, common settings/style 10. New tests cover target metadata and inlined rendering recovery: exactly one stock retry, original arguments retained, no continuation into the failed show frame, unrelated native exceptions not retried. Existing slider, pagination/cancellation, gestures, short-tap replay, native detail-match validation, settings and palette tests remain. Seven export/release-helper tests pass. All three API lint tasks pass.

Payload audit: **78 unique own classes**, Collections 33 / Sound 33 / common 12, exactly two selectable patches. Compile-only/HushFeed/test classes and duplicate own classes are excluded.

Completed final-bundle matrix:

| Selection | Source order | Compiled DEX/resources and references | Sound native-hook audit |
| --- | --- | --- | --- |
| Both add-ons + all 135 HushFeed patches, default options | HushFeed first | PASS | PASS: nine roles |
| Both add-ons + all 135 HushFeed patches, default options | Add-on first | PASS | PASS: nine roles |
| Collections + HushFeed Settings only | HushFeed first | PASS | Not applicable |
| Sound + HushFeed Settings only | Add-on first | PASS | PASS: nine roles |
| Collections without HushFeed Settings | HushFeed first | PASS: explicit dependency rejection | Not applicable |
| Duplicate Collections selection | Add-on first | PASS: explicit duplicate rejection | Not applicable |

Each final `port8-patches-0.76.0-*.json` must identify the candidate hash above. Earlier `preliminary-port8-*` uses development bundle `ff3ac302ecf3ea5ccc5bbed09bbe96ee3e2ac5c152d22f364c65ec17b60fa633`, not the final candidate; it passed an initial all-135 compiled combination and nine-hook audit but is not a substitute for the final matrix. A second `preliminary-guard-port8-*` run used `d5b9f0c89397b78582e0a855b050908cdb3d408d847ce3bf17619f611dccc20e` and passed the same checks before the duplicate-selection guard was moved ahead of original-body fingerprints. Its reverse-order run was stopped before completion; all final cases are rerun against the final candidate hash.

Compiled checks resolve injected host/shared method and field references, reject duplicate classes, verify selected-only payloads, single settings installation and argument-preserving wrappers. Native audits check nine independent lifecycle/touch/edge/dispatch/width roles after combined compilation. The width observer now follows content installation rather than the retired Qk wrapper. These checks do not establish general ART type-flow correctness or physical-device behavior.

## Limits, storage and next acceptance checks

No physical phone validation was performed. Check For You and search/sound-opened videos, short taps, disc holds versus edge speed, actual account-specific provider availability/saving, collection counts/pagination/scroll cancellation, pause/safe mode, hidden controls, AMOLED/custom backgrounds, fonts/RTL/TalkBack, rotation and vibration. Existing runtime conflicts remain: Favorites hold-speed remapping owns the same Favorites gesture; hidden controls stay hidden and Turn off haptics may suppress feedback. Stock provider data may legitimately offer no external destination.

All generated verification scratch and logs are retained under ignored build/verification and build/4743-* for debugging, as previously requested. Nothing from that scratch is included in source/release exports. It can be regenerated from the pinned inputs and commands in BUILDING.md; do not recursively remove the project or input directories to free space.

Publication, if completed, has its own exact source commit/CI/release/feed/download evidence. Never infer that a prepared local bundle has already updated Morphe's remote source.
