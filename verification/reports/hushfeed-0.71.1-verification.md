# HushFeed 0.71.1 integration — add-on 0.4.0-beta.1

This report concerns local compiled integration and simulated regression tests, **not phone acceptance**. No host APK was assembled for distribution, signed, installed or published. HushFeed's original source/bundle and YouTube Music work were not changed.

## Exact inputs

| Input | Identity |
| --- | --- |
| Personal bundle | `TikTok-Interaction-Addons-0.4.0-beta.1.mpp`, SHA-256 `0125d045845536e1704800c97bb5cd684d012873d774c559c5641ec776907eb2` |
| HushFeed | [v0.71.1](https://github.com/SysAdminDoc/HushFeed/releases/tag/v0.71.1), source commit `c86799e2820870746203f2614ce027eb4e0770c8`, 132 selectable patches / 115 default selections |
| External bundle | Publisher `.mpp`, 5,412,607 bytes; SHA-256 `2af01462eb712a20d6479fe09d33f82e0e2483e7613d5fa8c9a81309271aced0`, checked against GitHub asset digest |
| Original TikTok | `com.zhiliaoapp.musically` 47.1.4 / 2024701040, minSDK 23, ARM64/ARMv7, 472,239,331 bytes |
| APK SHA-256 | `4226ed5d3031b68208c29f62d80ac421b281fc74201bc4163d98e442d0991a40` |
| Original signer SHA-256 | `9041803e91bcb814b4b4399fb5c85a91640b755e5e8ba76813814bf4cf2ab5ba` |

Local tools remain pinned as documented in [BUILDING.md](../../BUILDING.md): JDK 17, Gradle 9.7.1, Android platform 36 / build-tools 36.0.0, Morphe plugin 1.3.4 and diagnostic patcher 1.15.0. The external publisher used patcher 1.15.1; Manager 1.34.0+ is its requirement. These tests do not execute Manager itself.

## Repaired boundaries

The actual external `createMasterMenu(Context, PreferenceScreen)` reuses its incoming Context register: at its return that register holds a Hub enum, not Context. The previous return-site invocation could therefore cause ART verification failure when the settings class loaded. No user crash log is available yet, so this is a demonstrated bytecode defect and likely explanation, not a log-confirmed diagnosis of that phone.

Argument-preserving wrappers now execute the original settings, resume, sound bind and disc-touch bodies in separate register frames before add-on callbacks. Compiled audits verify both the private native copy and wrapper argument registers. The shared root uses the compact grouped home and stable hub keys; it appears under **Customize → BDCrusher's Settings**, after Feed/layout or before App/advanced in the Settings-only combination. Preferences and resets remain independent of HushFeed.

The native edge timer listener is `LX/0ANF;->onTouch` in this fixture, structurally identified rather than selected by that obfuscated name alone. Its entry guard runs before the native 300 ms hold-speed timer. It reserves only a press beginning inside an enabled, attached, visible, clipped sound disc in the same window with valid song identity. The entire reserved sequence is excluded from the edge timer, including UP/CANCEL, while child native clicks/feedback continue. Holds elsewhere, hidden controls, disabled add-on and pause mode remain stock.

## Completed combination matrix

All rows below use the exact current hashes above and upstream default patch options. Named patch counts exclude unnamed dependencies. The all-patches runs are stress tests, **not a recommendation to install or enable all features**.

| Add-on selection | HushFeed selection | Source order | Result / own runtime classes |
| --- | --- | --- | --- |
| Both | All 132 | HushFeed first | PASS / 55 |
| Both | All 132 | Add-on first | PASS / 55 |
| Both | All 115 default-selected | HushFeed first | PASS / 55 |
| Sound only | Settings only | Add-on first | PASS / 29; no Collections |
| Collections only | Settings only | HushFeed first | PASS / 35; no Sound |

Machine evidence: [all / HushFeed first](hub4-hushfeed-0.71.1-both-all-hush-first.json), [all / add-on first](hub4-hushfeed-0.71.1-both-all-addon-first.json), [defaults / HushFeed first](hub4-hushfeed-0.71.1-both-defaults-hush-first.json), [Sound / Settings only](hub4-hushfeed-0.71.1-sound-minimal-addon-first.json), [Collections / Settings only](hub4-hushfeed-0.71.1-collections-minimal-hush-first.json).

Each successful combination executes patches, compiles combined DEX and resources (or preserves raw resources when none are patched), checks unique classes, validates injected host/public UI references, verifies the one shared settings hook and argument-preserving wrappers, and confirms selected payload isolation. Sound combinations also confirm the early guard precedes the timer. `androidDeviceTested` is false in every report.

Supplemental `verifyNativeHooks` passed on all four Sound-containing compiled outputs above: cover/title binding, cover/title unbinding, native disc touch and early edge guard — six intended hook sites. The cover inherits its unbind callback; the add-on's scoped override performs cleanup then invokes the original superclass callback. These are static compiled checks, not native runtime executions.

Negative integration checks also passed on the current hash: [Collections without Settings](hub4-hushfeed-0.71.1-missing-hush-first.json), [Sound without Settings](hub4-hushfeed-0.71.1-sound-missing-addon-first.json), [duplicate Collections](hub4-hushfeed-0.71.1-duplicate-hush-first.json) and [duplicate Sound](hub4-hushfeed-0.71.1-sound-duplicate-hush-first.json). Each was rejected explicitly during patching; no deliberately broken APK was compiled or installed.

## Automated regression

- **52 tests passed**: 30 Collections, 17 Sound, four settings hub and one patch wrapper regression.
- NewApi lint passed for all three runtime modules at minSDK 23. Robolectric UI/gesture simulations use SDK 28, not a physical API 23 device.
- Tests cover grouped-home placement without the obsolete heading, idempotence, independent selection/navigation, unchanged preferences, register reuse, visible/clipped hit area, disabled/pause/invalid-song cases, cancellation/multitouch/new press and ordinary short taps.
- A fake-native gesture test covers the 300 ms timer race, popup opening, no save when releasing on the disc, and exactly one dispatch after choosing a row. It does **not** save to TikTok or a music service.
- The workspace toolkit's 24 tests also pass; its review profile now includes the patch wrapper regression.
- A forced `:patches:generateMetadata --rerun-tasks` rebuild reproduced the exact delivered `.mpp` SHA-256. This is a local repeatability check, not a cross-platform/toolchain guarantee. The payload contains 55 own classes (26 Collections, 20 Sound, nine shared settings), with no compile-only stubs, tests or HushFeed classes.

## Runtime conflicts and remaining acceptance

Favorites hold-speed remapping still owns the Favorites long press when enabled: keep that remap off for Collections. The sound repair gives the disc area priority over native edge-speed and its hold-and-slide handling, not all screen holds. Hidden Favorites/discs remain hidden; clear display and pause mode are respected. Patch application with these options selected does not prove every enabled runtime combination works.

Music destinations still require eligible metadata already on the feed's Music object and a matching bound title component. No lazy song-detail fetch or new authentication flow was added. A song page can have more metadata than the feed. Native provider filtering, authentication and save dispatch are reused; unavailable destinations are not invented. TalkBack long-click support remains incomplete.

Required phone follow-up: main HushFeed settings opens; hub and both selected pages open; existing preferences survive; ordinary disc taps work; disc holds open the menu without 2×; edge holds elsewhere still work; cancellation/scrolling/dismissal do not save; each displayed native destination links/saves correctly. Check light/dark/AMOLED, hidden controls, pause mode, rotation, large fonts and accessibility. If it still crashes, collect the crash log after reproducing with the new bundle and report the selected patches/options and signing/update method. No universal or future-version compatibility is claimed.

## Storage and provenance

Small reports are retained outside generated scratch. Each combined run uses an isolated directory under `build/verification`, which can be regenerated from original inputs. These outputs contain host data and are excluded from the source ZIP. Do not distribute them, or recursively clean the app/workspace root. Inspect and authorize exact scratch paths before cleanup. Historical `hub3`/`interaction` reports and older bundle hashes are not current evidence.

The user explicitly requested **keeping this repair's generated scratch for further debugging**. Nothing was deleted. This preference supersedes any cleanup assumption; ask again before disposal. Final read-only storage inventory: **9,037,976,219 logical bytes (8.417 GiB), 213,065 files**, complete without links/read errors, under `C:\Morphe\Projects\TikTok\Add-ons\build\verification`.

Retained successful compiled outputs:

- `both-all-hush-first-5734634262248912686`
- `both-all-addon-first-3643037581757586863`
- `both-defaults-hush-first-3957397136805935231`
- `sound-minimal-addon-first-17468682868217076258`
- `collections-minimal-hush-first-1367861771559868857`

Also retained are the four negative-check directories and the failed initial development run `both-defaults-hush-first-3928202813232891172`. That failed run is obsolete and not the successful defaults output. Small final reports all identify the delivered hash; the final current defaults report replaces its development attempt.

An initial development build of 0.4.0-beta.1 (`1299…`, not the delivered hash) exposed an inherited Preference-method reference validation problem during finalization. Production references were corrected before the successful exact-hash checks above. Build success is explicitly not release acceptance or a replacement for ART/device validation.
