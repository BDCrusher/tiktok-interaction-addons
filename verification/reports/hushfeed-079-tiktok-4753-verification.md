# HushFeed 0.79.0 — unchanged personal 0.7.0-beta.1

Verified 2026-10-25. Both Enhanced collection popup and Sound button actions pass local patch execution and compiled DEX/resource checks against published HushFeed 0.79.0. No runtime or patch implementation change was needed. Phone acceptance remains pending.

## Exact inputs

- Original global TikTok `com.zhiliaoapp.musically`, **47.5.3 / 2024705030**, minSDK23, ARM64/ARMv7. SHA-256 `6670ca3f93bfea379658cad3c27f05d3a380b23a0d82569cce684b7453e8ff3e`. Matches the upstream release receipt; signer `9041803e91bcb814b4b4399fb5c85a91640b755e5e8ba76813814bf4cf2ab5ba`. Original identity/signature evidence is retained in the 47.5.3 fixture report.
- [HushFeed v0.79.0](https://github.com/SysAdminDoc/HushFeed/releases/tag/v0.79.0), source `02a4c5c5d1fd1123bdcd1f0fa8e6a59f8d8056a7`, published 2026-10-24. Bundle 6,624,200 bytes; SHA-256 `a25056e58abb44ffcd49fd5fa38f9fa315ac9553a113f15c86ea4c9509b069da`. GitHub digest, SHA256SUMS and release receipt agree.
- Upstream receipt SHA-256 `4e1af5722a22795f2104da74c71d98135ea6bf8e52f5fd4b83ab9bc31fb09f55`; checksum-file SHA-256 `6455df913502fbbe443e623d3ad413c65e6ab0b481d113d40a72c7e6005aebaf`. Inputs stay ignored under verification/inputs/hushfeed-0.79.0.
- Personal **TikTok-Interaction-Addons-0.7.0-beta.1.mpp**, 141,232 bytes, SHA-256 `5e2a1d7c77320c4066bf17268a98e59539721baa3f5353e5bc2ac47b17e629ae`. Identical to the 0.78-tested bundle; its 83 unit/simulated tests, seven helper tests, three API lint tasks and payload checks still apply. Original public source CI passed at `a437ca61668e77dbcd743f728401680503c2bdf2`.
- Local patcher 2.0.0 `8c7ff9f29d4cf6395fcbe42a4ceddd034d71fecf`, Gradle plugin 1.3.4 `a230c33f7eca1e18b07bf1114ca850421520f98d`, Smali `4220ae75f7`, JDK17/SDK36. Use **Manager 2.0.0+ / patcher 2.0.0**.

## Completed verification

| Selection | Source order | External patches | Result | Native Sound audit |
| --- | --- | ---: | --- | --- |
| both-all | hush-first | 140 | PASS: patch execution and compiled DEX/resources | 9 roles PASS |
| both-all | addon-first | 140 | PASS: patch execution and compiled DEX/resources | 9 roles PASS |
| collections-minimal | hush-first | 1 | PASS: patch execution and compiled DEX/resources | n/a |
| sound-minimal | addon-first | 1 | PASS: patch execution and compiled DEX/resources | 9 roles PASS |
| missing | hush-first | 0 | PASS: missing dependency rejected | n/a |
| sound-missing | addon-first | 0 | PASS: missing dependency rejected | n/a |
| duplicate | addon-first | 4 | PASS: duplicate add-on rejected | n/a |
| sound-duplicate | hush-first | 4 | PASS: duplicate add-on rejected | n/a |

All eight reports match the exact three input hashes above. All-patch runs select 140 global TikTok patches with default options; they do not prove every runtime configuration. The verifier now filters global TikTok compatibility entries and records exclusion of PineDrama-only Allow photo comments. This diagnostic correction changes no injected payload. Feature-only combinations confirm absence of the other add-on runtime. Missing Settings and duplicate selection are explicitly rejected.

Complete compiled DEX, including unchanged originals, is audited for duplicates/references. Combined payload: 78 isolated own classes (33 Collections, 33 Sound, 12 common), one shared settings payload, argument-preserving settings wrappers, native renderer/stock retry and Favorites haptics. Nine Sound lifecycle/touch/priority/width roles survive both all-patch orders and Sound alone. The structurally found edge listener remains X.0B5S.

Release-pinned SettingsMenuPreference, SettingsUi, TikTokPreferenceFragment and HushfeedPause contents are unchanged from 0.78.0, ignoring final newline normalization. Actual published-bundle execution checks the settings/UI/pause signatures too. Native authenticated loaders/detail/provider paths, preferences, gestures and pause/visibility behavior remain.

Evidence: `hush079-final-evidence.json`, eight `port9-patches-0.79.0-*.json` files and `hush079-local-checks/*.txt`. Private upstream/source comparison records are in build/hush079-20261025; compiled scratch remains in build/verification and consumes multiple GiB. Old source, inputs, checkouts and reports are preserved; no cleanup was performed.

## Morphe test and limitations

Publication of the exact 0.7 beta was explicitly requested. Separate publication receipts verify the immutable release and dev feed; this report does not itself assert publication completed. Enable Pre-release patches and Experimental app versions, then repatch the original 47.5.3 APK with HushFeed 0.79.0 Settings and either/both personal add-ons in one Expert-mode run. Do not select two add-on versions; retain the signing key used for the existing installation.

Settings remain **HushFeed → Your feed → BDCrusher's Settings**, with Personal add-ons fallback. Sound remains opt-in at patch selection. Keep Favorites hold-speed remapping off for the collection hold. Hidden controls, pause/safe mode and haptic suppression remain authoritative.

No app was signed/installed. ART loading, actual settings/rendering, taps/holds/scroll/cancellation, native saves/account linking/provider eligibility, haptic strength, themes, rotation, fonts/RTL and TalkBack need device acceptance. A dedicated accessible sound-disc long-click action remains unimplemented. Static success does not prove provider saves or universal option compatibility.
