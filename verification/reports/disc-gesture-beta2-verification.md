# Disc gesture and tick slider — 0.4.0-beta.2

## Scope and exact identities

The user reported the circled music disc still triggered speed-up with beta.1 selected/enabled, even after testing a song whose sound page offers YouTube Music saving. That is runtime failure evidence; beta.1's successful compiled checks cannot override it. The exact failing on-phone path/log is not available, so no claim is made that a single cause was reproduced on-device here.

This revision repairs early disc touch routing, alternate/view-creation registration and the collection count control. It retains independently selectable patches and the safe settings wrapper. It does not change HushFeed source/bundles, unrelated TikTok features or YouTube Music work.

| Input | Identity |
| --- | --- |
| Personal bundle | `TikTok-Interaction-Addons-0.4.0-beta.2.mpp`, SHA-256 `0b64c325eb46c32b772f514bd8b83c9b7b1d45ca1540f3903df13f4e0c0c7470` |
| HushFeed | [v0.71.1](https://github.com/SysAdminDoc/HushFeed/releases/tag/v0.71.1), source `c86799e2820870746203f2614ce027eb4e0770c8`, 132 named patches |
| External bundle SHA-256 | `2af01462eb712a20d6479fe09d33f82e0e2483e7613d5fa8c9a81309271aced0` |
| TikTok | Original `com.zhiliaoapp.musically` 47.1.4 / 2024701040, minSDK 23, ARM64/ARMv7 |
| APK SHA-256 | `4226ed5d3031b68208c29f62d80ac421b281fc74201bc4163d98e442d0991a40` |
| Original signer SHA-256 | `9041803e91bcb814b4b4399fb5c85a91640b755e5e8ba76813814bf4cf2ab5ba` |

Tools remain pinned in [BUILDING.md](../../BUILDING.md). Personal payload audit: **60 classes**, 30 Collections / 21 Sound / nine common settings, no duplicate/foreign/compile-only/test classes. Exactly two selectable features, not a HushFeed fork. No host APK was signed, installed, assembled for distribution or published.

## Changes and bytecode evidence

- `MainActivity.dispatchTouchEvent` invokes `X.03uJ.dispatchTouchEvent`; `DetailActivity` inherits from `X.03uJ`. Its native method either calls Android Activity dispatch or a custom `X.04UK` delegate. The new entry guard runs before both paths, only claiming a sequence beginning inside the clipped visible area of a bound eligible disc in that window. Fingerprints validate target version, ancestry and both original calls.
- The routed sequence updates its hold binding once, converts screen coordinates to native disc-local coordinates and dispatches native disc events directly, bypassing parent feed timers. A depth guard prevents the existing patched disc listener from updating state twice. Short taps still reach the disc's native handlers; a successfully opened menu cancels the native target so release cannot also navigate. Cancelled/changed/disabled sequences are drained, never passed as orphan UP events into stock parents.
- `VideoMusicCoverAssem` contains primary `LLLLLLL` and alternate `LLLLLLLLL` FrameLayouts. Both are registered; no global approximate screen rectangle or forced-visible control is introduced. Cleanup checks binding identity before removing a recycled View's new owner.
- The native `ReusedAssem.rq()` getter reads its current item from `LLJJL` and returns null if not bound. An argument-preserving `onViewCreated` wrapper runs the original body, then reads that current item to register views after inflation. It is installed during finalization, after execute-time HushFeed fingerprints, so those still inspect the original host body. This covers bind-before-inflation/recreation without assuming the order on the user's phone.
- The beta.1 edge-listener guard is retained as supplemental protection. The new route does not depend on it being the only native long-press path.
- Gesture status exposes only session counts/state: window events, bound/eligible discs, captures and menu openings. No IDs, song titles, native provider objects, account details or credentials are logged or exported.
- CountSlider uses a native integer SeekBar and custom Canvas ticks for API 23. Twenty values, live number, native palette accent, RTL-mirrored marks, 1/20 endpoints, Save/Cancel and draft restoration. Accessibility range and set-progress arguments are mapped from zero-based native progress to 1–20. Existing preference names/count defaults/reset are unchanged.

## Automated evidence

**60 tests passed**, zero failures: 31 Collections, 24 Sound, four hub and one patch-wrapper regression. NewApi lint passed for all three runtime modules at minSDK 23.

New sound simulations verify captured disc holds bypass a parent 300 ms speed timer; outside presses still reach that timer; short taps call the native click handler exactly once; holding/releasing on the disc does not save; scrolling-before-hold/multitouch cancel clicks/saves; disabling mid-sequence drains input; alternate containers/late inflation are registered; owner recycling and native View exceptions do not corrupt the route. Fake native saving is used — not real TikTok or provider requests.

Slider simulations verify integer limits/defaults, twenty tick positions, live value, Cancel leaving preferences unchanged, Save, unsaved-draft recreation and RTL positions. Tests execute under Robolectric SDK 28; minSDK lint is not an API 23 device test or a pixel/TalkBack acceptance test.

A forced `:patches:generateMetadata --rerun-tasks` rebuild reproduced the exact current bundle SHA-256. This is local toolchain repeatability, not a cross-platform reproducibility guarantee.

## Compiled integration

Machine reports use `disc5-hushfeed-0.71.1-*.json`, not historical `hub4` results. Audits check compiled DEX/resources, isolated payload selection, duplicate classes, native/public UI references, argument wrappers, early window guard and view-creation registration. Supplemental NativeHookAudit checks eight hook sites. These are not ART/device execution.

| Selection | Source order | Compiled check |
| --- | --- | --- |
| Both add-ons + all 132 HushFeed patches | Add-on first | [PASS](disc5-hushfeed-0.71.1-both-all-addon-first.json), 60 own classes; supplemental eight-hook audit PASS |
| Both add-ons + all 132 HushFeed patches | HushFeed first | [PASS](disc5-hushfeed-0.71.1-both-all-hush-first.json), 60 own classes; supplemental eight-hook audit PASS |
| Sound only + HushFeed Settings only | Add-on first | [PASS](disc5-hushfeed-0.71.1-sound-minimal-addon-first.json), 30 own classes, no Collections; supplemental eight-hook audit PASS |
| Collections only + HushFeed Settings only | Add-on first | [PASS](disc5-hushfeed-0.71.1-collections-minimal-addon-first.json), 39 own classes, no Sound |

All-patches checks use upstream default patch options and are stress tests, not recommendations to enable every runtime feature. Only completed checks are listed here. Earlier beta.1 default/negative-selection checks are historical, not rerun/current-hash claims for this bundle.

## Phone follow-up

Repatch the original TikTok 47.1.4 with HushFeed 0.71.1 Settings and your chosen add-ons in one run; replace the old add-on source, don't select two copies. Keep the signing key/preferences. Settings stay at **Customize → BDCrusher's Settings**. Select Sound explicitly; its patch-selection default is off.

Check inside-disc hold on For You and videos opened from search/sounds; normal disc taps; outside edge hold; no saves on cancel/release over disc; actual TikTok save and each displayed provider's native linking/save flow; hidden controls/pause; rotation/light/dark/AMOLED/large fonts/RTL/TalkBack; slider draft Save/Cancel. If still failing, reproduce, then share **Sound button actions → Gesture status**, page version, HushFeed version and enabled options. Counters are session-only, so capture before restarting.

External destinations still require native feed eligibility data and a corresponding title component. A richer sound page may supply options the feed does not. No lazy detail fetch, borrowed credentials, private API bypass or installed-app enumeration was added. Real device acceptance and full music detection remain pending; no universal compatibility claim.

## Debug storage

Previously retained scratch is untouched, following the user's request to keep it for debugging. This beta adds four bounded isolated verification runs under `build/verification`; small reports are outside that directory and source packaging excludes all host scratch/inputs. Do not distribute scratch or clean it without renewed authorization. Final complete read-only inventory of all retained verification runs: **13,364,584,727 logical bytes (12.447 GiB), 319,591 files**, no skipped links or read errors. Beta.1's prior retained 8.417 GiB remains included; beta.2 adds about 4.03 GiB. Nothing was deleted.

Beta.2's four retained compiled runs:

- `both-all-addon-first-9148914210959098800`
- `both-all-hush-first-3456053013240291938`
- `sound-minimal-addon-first-15529145720997762618`
- `collections-minimal-addon-first-17010060306683992406`
