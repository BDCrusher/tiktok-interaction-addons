# Compatibility report — 0.5.1-beta.1

## Current HushFeed 0.74.0 settings repair

Current bundle: `TikTok-Interaction-Addons-0.5.1-beta.1.mpp`, SHA-256 `900c0bd68a27a6014aac3d8ed7cfdc7a41757998e4c8475c245d3523e6f6e054`. Exactly two selectable patches, one unnamed common dependency, 75 unique own runtime classes (30 Collections / 33 Sound / 12 common), no injected compile-only/HushFeed/test classes.

The user's 0.5.0 bundle failed patch finalization against HushFeed 0.74.0 because its SettingsMenuPreference constructor lost the integer changed-count argument. That release also removed setActiveCount and replaced the old Customize/hub routes with flat section routes. This fix updates the compiled contract and validation together, refreshes counts in our own summary text and places BDCrusher's Settings after Feed screen under Your feed. If that page is absent, another available Your feed route is used; if all are absent, Personal add-ons is placed before Hushfeed's group. Existing preferences/defaults and both feature engines are unchanged. The safety checks are retained, not bypassed.

External requirement: **HushFeed 0.74.0**, [release](https://github.com/SysAdminDoc/HushFeed/releases/tag/v0.74.0), source `bd6fb96912fd79f849b7692fb6136cc851d209c3`; bundle SHA-256 `dba873d7c7ad8624803eb4c9b3627664e4b9ed146f2fd5eae741ddef6426e7bf`. The publisher's checksum, GitHub asset digest and release receipt agree. Original TikTok remains 47.1.4 / 2024701040, package `com.zhiliaoapp.musically`, SHA-256 `4226ed5d3031b68208c29f62d80ac421b281fc74201bc4163d98e442d0991a40`; the user restored the missing local APK with identical bytes.

**79 unit/simulated tests pass**, with all three runtime NewApi lint checks. Completed compiled combinations and artifact evidence are in the [0.74 compatibility report](verification/reports/hushfeed-074-settings-verification.md). Only current-hash `flat7-hushfeed-0.74.0-*.json` records concern this release; old reports are historical. Local verification uses pinned patcher 1.15.0; HushFeed was published using 1.15.1. The user's Manager 1.36.0-dev.5, patcher 1.15.2-dev.1, Android 17 and physical device have not been exercised locally. No universal option/runtime compatibility or phone acceptance is claimed.

Use the new add-on source instead of 0.5.0, selecting HushFeed Settings and either/both personal patches against the original APK in one run. Retain your signing key/preferences. Other HushFeed versions and PineDrama are not claimed supported by this add-on. No upstream files were changed; no APK was signed, installed, published or distributed. Previously documented gesture/provider/accessibility limitations remain.

## Historical 0.5.0-beta.1 — incompatible with HushFeed 0.74.0 settings

## Current inline slider, native detail and sound feedback repair

Current bundle: `TikTok-Interaction-Addons-0.5.0-beta.1.mpp`, SHA-256 `9e933d46410e130db944e10495fc4d0750d15c0ae5cc6be4664acce89599c186`. Exactly two selectable patches and one unnamed shared dependency. Payload: **75 own unique classes**, 30 Collections / 33 Sound / 12 common; no foreign/compile-only/test classes.

The user reported the previous version's sound hold opened a menu but blocked ordinary taps and omitted a known YouTube Music destination; the count slider was one dialog too deep. The new slider is directly inside the Collection popup page and saves each user change with native Favorites haptics. Sound replays short taps through the full original Activity dispatcher and uses bounded asynchronous native MusicDetail fetching for missing feed data, accepting only exact IDs or an explicit TikTok matched recording. Same-track/window title matching no longer requires identical parameter objects. Popup width uses observed Favorites sizing (otherwise native 212 dp fallback); selected-row color/haptics match enhanced Favorites, including screen-origin-aware hover hit testing.

**76 tests and all three minSDK 23 NewApi lint checks pass.** See the [0.5 verification report](verification/reports/interaction-detail-beta1-verification.md) for completed combined builds, exact hashes, payload isolation and limitations. Only `detail6-` reports match this revision. Historical reports below are retained unchanged, not fresh evidence.

Target remains original TikTok **47.1.4 / 2024701040**, HushFeed **0.71.1** (external bundle SHA-256 `2af01462eb712a20d6479fe09d33f82e0e2483e7613d5fa8c9a81309271aced0`, source commit `c86799e2820870746203f2614ce027eb4e0770c8`). APK SHA-256 `4226ed5d3031b68208c29f62d80ac421b281fc74201bc4163d98e442d0991a40`; original signer `9041803e91bcb814b4b4399fb5c85a91640b755e5e8ba76813814bf4cf2ab5ba`. Upstream sources/inputs are unchanged. No signing, installation, publishing or host-APK redistribution.

**Not phone-accepted.** Real navigation, native detail/account eligibility, provider saves/authentication, haptic strength, popup placement, scrolling, fonts/RTL/accessibility remain device-dependent. HushFeed Turn off haptics/device restrictions may suppress vibration. Hidden controls stay hidden, Pause/safe mode stays stock, Favorites hold-speed remap remains a gesture conflict. Native detail fetching does not guarantee every sound-page experiment/provider will be available. A dedicated TalkBack disc long-click action is not implemented.

## Historical 0.4.0-beta.2 — hold menu opened, short taps/destination detection failed

## Current disc routing and collection slider

Current bundle: `TikTok-Interaction-Addons-0.4.0-beta.2.mpp`, SHA-256 `0b64c325eb46c32b772f514bd8b83c9b7b1d45ca1540f3903df13f4e0c0c7470`. Exactly two selectable patches plus one unnamed settings dependency. Runtime payload: 60 unique own classes (30 Collections, 21 Sound, nine common), no foreign, stub or test classes.

The user confirmed Sound was selected/enabled on beta.1 and a disc hold still sped up, including a song known to offer YouTube Music saving on its sound page. **Beta.1's compile/simulation success was not phone acceptance.** This revision reserves a visible eligible disc touch at the native shared Main/Detail Activity dispatcher before custom routing/feed parents, forwards native disc events locally for short-click/feedback handling and manages the add-on hold once. It also binds both native disc containers and re-registers after native view creation, using the current bound item rather than a global last song. The earlier settings wrapper and supplemental edge guard remain.

The collection count now uses a themed native slider with one tick for each value 1–20, a live selected number, confirmed Save, Cancel and saved draft restoration. Its keyboard/accessibility controls keep the displayed 1–20 range; physical rendering/TalkBack acceptance is still pending.

**60 tests and all three minSDK 23 NewApi lint checks pass**. Current compiled combinations and eight-hook audits are recorded in the [beta.2 verification report](verification/reports/disc-gesture-beta2-verification.md). Only exact-hash `disc5-` reports concern this version. Historical `hub4-` reports are retained and do not prove the new window hook.

Target remains original TikTok 47.1.4 / 2024701040 with external HushFeed 0.71.1, using the same input/bundle/signature identities in the historical report below. Upstream source and external bundles are unchanged. No signing, installation, publishing or APK distribution occurred.

**Phone confirmation required:** normal For You and search/sound-detail videos, native short taps, holds inside/outside the visible disc, cancellation/scrolling, hidden controls, pause mode, actual save/linking, themes/rotation/large fonts and slider accessibility. Gesture status under **BDCrusher's Settings → Sound button actions** displays session-only counters to help locate a remaining failure. Compare the version shown on the page; if it still speeds up, share that readout after reproducing, plus the HushFeed version and selected patches/options.

External music eligibility still depends on native feed metadata and matching bound title context; this repair does not add lazy song-detail retrieval or new provider APIs. A sound-page YouTube Music button does not guarantee that the feed's native Music object already contains that destination. Capturing the hold and discovering every provider are separate acceptance checks.

## Historical 0.4.0-beta.1 — disc hold failed on the user's phone

## Current settings/gesture repair

Bundle: `TikTok-Interaction-Addons-0.4.0-beta.1.mpp`, SHA-256 `0125d045845536e1704800c97bb5cd684d012873d774c559c5641ec776907eb2`. Exactly two selectable patches, one unnamed settings dependency. Payload audit: **55 own classes** (26 Collections, 20 Sound, 9 shared navigation), no duplicate, compile-only, test or HushFeed classes injected.

External requirement checked: **HushFeed 0.71.1**, published bundle SHA-256 `2af01462eb712a20d6479fe09d33f82e0e2483e7613d5fa8c9a81309271aced0`, source commit `c86799e2820870746203f2614ce027eb4e0770c8`. Original input remains **TikTok 47.1.4 / 2024701040**, APK SHA-256 `4226ed5d3031b68208c29f62d80ac421b281fc74201bc4163d98e442d0991a40`, original signer `9041803e91bcb814b4b4399fb5c85a91640b755e5e8ba76813814bf4cf2ab5ba`.

### Crash repair

HushFeed's compiled `createMasterMenu(Context, PreferenceScreen)` has five registers. Incoming Context is v3/p1, but the host body reuses that register for booleans and Hub enum constants, ending on ABOUT. The old add-on injected `install(Fragment, Context, PreferenceScreen)` at the return using p1, which no longer had Context type: an invalid call capable of causing an ART verifier error when loading settings. Source-signature checks and DEX compilation alone did not detect that earlier bug.

The new wrapper forwards the original body to a private native copy in its own register frame, then calls install with the unchanged incoming arguments. onResume, sound cover/title binding and disc touch use the same argument-preserving pattern. The code resolves contracts against the actual external payload. No HushFeed source or distributed bundle was altered.

The personal root entry is now **HushFeed → BDCrusher's Settings**, within the main screen's **Customize** section. Stable hub keys locate insertion after Feed/layout, or before App/advanced in a settings-only selection; the row uses native compact styling. It does not inject the obsolete Your feed heading. Existing preference files are preserved.

### Sound-disc priority

The native edge-speed listener is identified structurally by its EdgeSpeedupAbility field/call, View/MotionEvent signature and 300 ms timer. A guard at entry reserves a touch sequence only when it begins in an enabled, attached, visible sound disc's clipped on-screen hit area in the same window, with a valid track/sound. It skips the native listener's whole sequence, preventing its earlier timer and stale UP coordinates. Normal disc clicks are not replaced; holds elsewhere are unchanged. Hidden/transparent, disabled, paused, invalid or different-window cases use stock behavior. Track changes, slop, cancellation and multitouch retain existing cancellation rules.

### Verification

**52 automated tests passed**: 30 Collections, 17 Sound policy/settings/gesture simulations, four hub tests, one register-wrapper regression. All three runtime modules pass minSDK 23 NewApi lint. The simulated Sound menu test covers the 300 ms race, menu opening, no save on release over the disc and exactly one fake-native dispatch after row selection; it is not a TikTok/API save.

Current exact-hash combined reports use `verification/reports/hub4-hushfeed-0.71.1-*.json`. Completed selections and supplemental audits are listed in [0.71.1 verification](verification/reports/hushfeed-0.71.1-verification.md). Full DEX/resource compilation, unique classes, resolved host references, argument-preserving wrappers and the early guard are checked, not merely patch execution. The all-patches selection is a diagnostic stress test with default options, not advice to install everything or enable every runtime switch.

**Device acceptance is pending.** No Android install/ART execution, current phone settings rendering, real native sound saving/linking, provider metadata completeness, TalkBack, or every custom option/runtime combination was verified. Ask for the crash log if a new install still fails. Other TikTok/HushFeed versions are not verified for this bundle; contract mismatches should fail patching, not be bypassed. Lazy song-detail acquisition and a TalkBack long-click action remain outside this repair.

## Historical 0.3.0-beta.1 report (not current evidence)

## BDCrusher's Settings update

Current bundle: `TikTok-Interaction-Addons-0.3.0-beta.1.mpp`, SHA-256 `e605211763e9ea56cef031290b3e27fae8fe0a560d0790f0c62db15a27170e78`. Exactly two independently selectable patches; shared settings navigation is an unnamed dependency, not a third patch. Audit: 53 unique runtime classes (26 Collections, 18 Sound, 9 shared settings), no compile-only stubs, test adapters or foreign classes.

The root entry is now **HushFeed → Your feed → BDCrusher's Settings**, containing Collection popup and Sound button actions pages only for selected modules. Sound no longer depends on the Feed screen category or an INTERFACE section hook. There is one shared install hook and lifecycle refresh, regardless of whether one or both features are selected. The existing preference files and native collection/sound engines are retained; neither HushFeed files nor YouTube Music sources were changed.

Completed current checks against the unchanged published HushFeed 0.69.0 bundle and original TikTok 47.1.4:

- Both add-ons plus all 55 default-selected HushFeed patches, HushFeed first: PASS.
- Same selection, add-on first: PASS, including cross-payload method reference resolution.
- Sound only plus HushFeed Settings only, add-on first: PASS. 27 own classes (18 Sound, 9 shared settings), no Collections classes; one root hub hook and no old section installer. Raw resources are preserved in this minimal selection.
- 42 JVM/Robolectric tests: PASS (30 Collections, 10 Sound, 2 shared hub). Individual feature tests run without the other feature's classes. Shared tests exercise both adapters, idempotence, two-level navigation, restoration without root initialization and unchanged saved preferences.
- minSDK 23 NewApi lint for all three runtime modules: PASS. Not an API 23 device run.
- Supplemental native Sound hook audit on the compiled HushFeed-first default combination: PASS for disc binding, title binding/unbinding and disc touch.

Both combined checks compile DEX/resources, reject duplicates, validate injected native/HushFeed references and count one shared root install/refresh per normal return. Machine reports: `verification/reports/hub3-hushfeed-0.69.0-*.json`. APK SHA-256 `4226ed5d3031b68208c29f62d80ac421b281fc74201bc4163d98e442d0991a40`; HushFeed SHA-256 `2d25ddeceb9abf469116d1c23688dc822079e53b755e8bd84cf2088e4b222b20`. Pinned local patcher remains 1.15.0; external HushFeed was built with 1.15.1 and requires Manager 1.34.0+.

**Build/simulated UI compatibility only, not release acceptance.** No Manager UI import, ART/device execution, theme pixel validation, actual native saving/auth, accessibility acceptance or universal patch/option compatibility is claimed. Current 0.3.0 checks do not rerun the previous all-130 or expanded selections. Other HushFeed releases need separate checks of this new shared hub. Sound still has no lazy music-detail fetch or accessible long-click path. Use SOUND_BUTTON.md's device checklist and the existing collection gesture/pagination checklist below. Update with the same signing key to retain installed-app preferences; no automatic uninstall/install occurs.

## Historical 0.2.0-beta.1 evidence

Everything in this section through the archived Collections-only reports below describes older bundle hashes, not the current 0.3.0 build.

Current bundle: TikTok Interaction Add-ons, with two independent selectable patches. Sound button actions is opt-in; selecting it alone does not inject Collections classes. HushFeed source/bundle files are unchanged. Sound originally targeted HushFeed 0.68.0 and original TikTok 47.1.4 only. Full behavior, source bindings and known limits: [SOUND_BUTTON.md](SOUND_BUTTON.md).

### HushFeed 0.69.0 verification (0.2.0 only)

The unchanged 0.2.0-beta.1 bundle has passed combined patch application, DEX/resource compilation, duplicate-class, native-reference and settings-hook checks against the published HushFeed 0.69.0 release. Exact completed selections/orders and fixture identities: [0.69.0 verification report](verification/reports/hushfeed-0.69.0-verification.md). **Build compatibility only; phone behavior and every custom option are not verified.** No production patch/runtime changes were needed. The add-on `.mpp` hash below remains unchanged.

### HushFeed 0.68.0 baseline (0.2.0 only)

Current bundle SHA-256: `bf3ec02f0bb76812a346949d8159dbc933de824add319a0b2b7b712bbaa213ae`. Audit: 46 classes across isolated payloads (27 Collections, 19 Sound), no compile-only stubs or duplicate classes. 40 policy/settings/runtime simulation tests pass (30 existing Collections, 10 Sound); Sound minSDK 23 NewApi lint passes. No Android device/emulator flow was tested.

| Current combined check | Scope |
| --- | --- |
| Both add-ons + HushFeed 0.68.0 expanded selections, both source orders | PASS: DEX/resource compilation, duplicate-class audit, native reference resolution and settings-hook counts. Expanded selection is Settings, AMOLED dark theme, Long-press controls, Keep the Favorites tab, Hide feed save button, Remember clear display, Change app name and Look like the store app. |
| Both add-ons + 41 default HushFeed 0.68.0 selections, HushFeed first | PASS: same build/reference checks; not all 106 optional patches or all option values. |
| Sound only + HushFeed Settings only, add-on first | PASS on the current bundle hash. Checks that Collections classes are absent and Feed screen/settings hooks exist. |
| Sound without HushFeed Settings | PASS: explicit patching failure, not an inaccessible installed settings entry. |
| Collections only + core HushFeed selections, HushFeed first | PASS: combined DEX/resources and native reference checks; no Sound payload classes. |
| Selecting two Sound patch copies | PASS: explicit duplicate/already-applied refusal; no silent second hook. |
| Forced complete bundle rebuild | PASS: 62 tasks rerun; generated `.mpp` retained the exact same SHA-256. |

Reports: `verification/reports/interaction-hushfeed-0.68.0-*.json`; match the bundle hash before relying on them. Stock input and HushFeed fixture hashes below are unchanged. Development beta only: saving/auth, drag interaction, theme pixels, native ViewModel lifecycle and accessibility remain device-unverified. External detection is limited to eligible metadata already supplied in the feed; lazy song-detail acquisition is not implemented. No APK was signed, installed or distributed.

## Archived Collections-only beta.2 report

Date: 2026-10-08. This update redesigns the add-on settings, not the collection-saving engine. It remains one independent patch. Device acceptance is pending.

### Settings refresh and beta.2 evidence

The entry now sits under **Your feed**, after Feed screen when present and before Watching and sharing. A settings-only selection gets a Your feed group too. The full page reuses HushFeed's external public header, palette, card, switch and dialog styling helpers. It uses the existing fragment container/back stack, not a replacement settings Activity. Defaults no longer count as changed; returning refreshes the badge. Preferences remain in `collections_addon_v1`, with no migration/reset on upgrade.

Current add-on SHA-256: `b3ff19eb546a2140dfa8b0b2dc7a44da66c9a3e3ae6635478a450b3e4cb1e697`. Payload audit: **27 classes, all `app.collectionsaddon.*`**, no upstream/stub classes. Source and bundle checksums are in `dist/SHA256SUMS.txt`.

Primary external fixture: [HushFeed v0.68.0](https://github.com/SysAdminDoc/HushFeed/releases/tag/v0.68.0), source/receipt commit `2bd8a8e24dc9b6bbb00c93922ae44dde79b27a52`. Downloaded bundle SHA-256: `06187bc54780bb3b98176e604ffe7fe1a37a5cff1ca720f95db138327616db9f`, 4,272,822 bytes, matching the publisher's release receipt. The external release requires Manager 1.34.0 or newer and was built with patcher 1.15.1; local diagnostic compilation uses the add-on's pinned patcher 1.15.0. This is not a Manager UI import test.

Original TikTok input is unchanged from beta.1: 47.1.4 / 2024701040, SHA-256 `4226ed5d3031b68208c29f62d80ac421b281fc74201bc4163d98e442d0991a40`. No installable APK was assembled, signed or distributed.

| Current check | Result and scope |
| --- | --- |
| Runtime regression | PASS: 30 simulated API 28 tests; page placement, navigation/back, immediate toggle, badge updates, count validation/cancel, isolated confirmed reset, large-font stacking and Activity recreation with an unsaved count dialog. |
| Android minSDK 23 NewApi lint | PASS: no issues. Not an API 23 device run. |
| HushFeed 0.68.0 expanded selection, HushFeed first | PASS: compiled combined DEX/resources, unique classes, resolved host references and exactly one menu hook plus lifecycle refresh. |
| HushFeed 0.68.0 expanded selection, add-on first | PASS: same checks on the exact current bundle. |
| HushFeed 0.68.0 default selection, HushFeed first | PASS: all 41 default-selected patches, compiled DEX/resources, unique classes and resolved references. Not all optional patches. |
| HushFeed 0.67.0 core selection, HushFeed first | PASS: Settings, Long-press controls, Keep the Favorites tab and Hide feed save button; compiled DEX/resources, unique classes and resolved references. |
| Missing HushFeed Settings | PASS: clear dependency refusal at patch finalization. |
| Selecting two add-on copies | PASS: explicit duplicate/already-applied refusal; no silent second hook. |
| Forced local rebuild | PASS: all 38 build tasks rerun; identical beta.2 bundle SHA-256. Same workstation/toolchain only. |

Expanded selection: Settings, AMOLED dark theme, Long-press controls, Keep the Favorites tab, Hide feed save button, Remember clear display, Change app name, Look like the store app. Upstream patch options use defaults. Runtime Favorites hold-speed remapping must remain off; selecting that patch is not the same as enabling its conflicting Favorites gesture.

The 0.67.0 core check uses the unchanged published bundle from commit `f61392d937c6e4304ea5d52f31f5146ab38776a7`, hash `fdfc85fba43fd2d9b6090c541e40c1e19ccf3c0558f71586f30affc51704560d`. It is narrower than the 0.68.0 checks; beta.1 expanded/default evidence below is historical and must not be treated as a beta.2 rerun.

Machine reports are prefixed `verification/reports/beta2-`; regression scope is in `beta2-runtime-tests.md`. Test UI helpers are compile-only stand-ins, so they test logic/lifecycle, not HushFeed's actual pixel output. Actual public helper contracts are resolved against the external binary during combined compilation.

Your screenshots establish that the previous beta.1 entry/dialog ran with HushFeed 0.68.0. They are not verification of the redesigned page. The new page still needs installed-app checks for phone visuals, actual Back, light/dark mode, TalkBack, RTL, multi-window, keyboard behavior and preference retention after a same-key update. Collection pagination, gestures and server-side saves retain the outstanding beta.1 device checklist below. Neither compilation nor simulated tests establish universal compatibility.

## Historical beta.1 evidence (not a test of beta.2)

Date: 2026-10-03. **Beta implementation, not device/release acceptance.** Exactly one selectable add-on patch; no HushFeed patches or shared extension implementations included.

## Fixture and upstream identity

Original APK: `com.zhiliaoapp.musically`, **47.1.4**, version code **2024701040**, minSDK 23; arm64-v8a / armeabi-v7a APK, 472,239,331 bytes.

APK SHA-256: `4226ed5d3031b68208c29f62d80ac421b281fc74201bc4163d98e442d0991a40`.

Original signer certificate SHA-256: `9041803e91bcb814b4b4399fb5c85a91640b755e5e8ba76813814bf4cf2ab5ba`.

The original APK was read-only input; no signed or installable patched TikTok APK was created or distributed. Local ignored scratch files contain compiled/decompiled host data and must not be included in any source delivery.

HushFeed source inspected: `0148d0e48005c8255e9325317e224b9196f1c2a4`, identifying itself as 0.67.0. **The published 0.67.0 bundle is from a different commit**, `f61392d937c6e4304ea5d52f31f5146ab38776a7`, according to its release receipt. Combined binary checks use that published bundle, not a newly built binary of the later inspected commit. Do not infer binary testing of `0148d0e...` from the shared version string.

External bundle SHA-256: `fdfc85fba43fd2d9b6090c541e40c1e19ccf3c0558f71586f30affc51704560d`, matching the publisher's checksum. Release receipt and checksums are retained under ignored `verification/inputs/`. [Published HushFeed v0.67.0](https://github.com/SysAdminDoc/HushFeed/releases/tag/v0.67.0).

HushFeed tracked source and downloaded bundle were not edited. YouTube Music sources were not changed. The final build uses its own pinned, unmodified build-tool checkouts, not the YouTube Music project's plugin checkout.

## Verification scope

The final bundle SHA-256 is in `dist/SHA256SUMS.txt`; the locally repeated build produced `848900085651c1b44248b686cebb7a731d72d49792a74321132c4c8a6a232003`. **19 runtime payload classes, all in `app.collectionsaddon.*`; no compile-only integration stubs or foreign payload classes.** Generated audit: `dist/payload-audit.json`.

| Check | Result/scope |
| --- | --- |
| Runtime regression suite | PASS: 23 Robolectric/JUnit tests on simulated API 28. See `verification/reports/runtime-tests.md`. |
| minSDK 23 API lint | PASS: Android NewApi lint found no issues. Not an API 23 device test. |
| Repeat build | PASS: normal build and forced all-task rebuild gave identical `.mpp` hash. Same workstation/toolchain only. |
| Core, HushFeed first | PASS during development; Settings, Long-press controls, Keep the Favorites tab, Hide feed save button. |
| Core, add-on first | PASS during development; same selection. |
| Core + AMOLED, HushFeed first | PASS during development; full modified-resource compilation. |
| Expanded, HushFeed first | PASS on final bundle: compiled DEX/resources, no duplicate classes/unresolved host references; one settings hook. |
| Expanded, add-on first | PASS on final bundle: same checks; one settings hook. |
| All 41 default-selected HushFeed patches | PASS on final bundle: compiled combined DEX, no duplicates/unresolved host references; one settings hook. |
| Missing HushFeed settings | PASS on final bundle: patching fails explicitly with the required-HushFeed-Settings message. |
| Duplicate add-on selection | PASS on final bundle: patching explicitly rejects a second copy rather than inserting duplicate hooks. |

The expanded selection is exactly: **Settings; AMOLED dark theme; Long-press controls; Keep the Favorites tab; Hide feed save button; Remember clear display; Change app name; Look like the store app.** Upstream patch options use their defaults. Runtime settings were not toggled in TikTok because there was no device session. Custom AMOLED colors are covered by palette logic tests, not an actual themed app run.

Successful combined checks compile DEX/resources, inspect every output class for duplicates, resolve the add-on's native/HushFeed method and field references, verify NativeCallback's host interface, and count the settings-entry hooks. They **do not** validate ART loading or runtime behavior. Final reports under `verification/reports/*.json` record exact selections, input hashes and add-on hash. Older development checks used earlier development bundles and are not equivalent to an exact final-bundle check.

The default-selection report lists all 41 names. It includes Downloads, Feed filter, Playback speed, Remember clear display, Feed tab navigation, SIM spoof, translation, sharing and privacy changes; it does not mean every optional HushFeed patch was selected. Expanded checks separately include the Favorites gesture patch. Build compatibility does not mean the runtime Favorites speed-remap and collection popup can occupy the same long press: **leave that remap off**.

## Device acceptance still outstanding

No Android emulator/physical-device TikTok run, account login, server-side save, Morphe Manager UI import, or installed combined APK test was performed. The following are **unverified**, not passing acceptance results:

- Counts 1–20 with real account data at limit−1, limit, limit+1; empty lists; cached responses; pinned, collaborative and special collections; page exhaustion/errors/offline operation.
- Favorite/unfavorite single taps; holding, short drag selection, scrolling, release outside, cancellation, dismissal, repeated opening, and duplicate/accidental save detection.
- TikTok light/dark modes independent of phone theme; AMOLED/custom palette on every action/icon/thumbnail; live theme switching.
- Font scaling, TalkBack focus/labels, RTL, rotation, cutouts/insets, screen-edge anchors, landscape and small displays in the actual native popup. Height/RTL simulation is not visual QA.
- Actual HushFeed pause/safe-mode startup, hidden save button, clear-display toggles and Favorites hold-speed remap on/off; application/package customization beyond the tested app-label and store-identity patches.
- ART bytecode verification and sustained session behavior, keyboard/settings layout, memory, and pending-popup behavior when switching host Activities/videos during loading.

Before promotion beyond beta, patch the original APK with the documented bundle pair, test a disposable/noncritical video in a collection, compare the real collection contents after each gesture, capture logs/screenshots and record device/API, app/build hashes, selected patches/options and account-specific result counts. Test disabled/paused fallback as well as enabled behavior. Do not use an all-patches selection as a shortcut to compatibility coverage.

Release acceptance remains pending those device-dependent checks. Other TikTok versions, other HushFeed binary releases, the later inspected source commit built as a binary, and every possible patch combination require separate verification. This is not a general-purpose supported-version declaration.
