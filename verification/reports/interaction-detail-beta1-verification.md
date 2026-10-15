# Inline slider and sound-detail repair — 0.5.0-beta.1

## Scope and input identities

The user reported that the count slider should be on the Collection popup page, not in another dialog; requested feedback for number changes; and reported that Sound opened on hold but ordinary taps did not work and a known YouTube Music destination was missing. They also requested Favorites-equivalent popup width, hover highlight and vibration. The previous on-phone report is not overwritten by simulated successes.

| Input | Identity |
| --- | --- |
| Add-on | `TikTok-Interaction-Addons-0.5.0-beta.1.mpp` |
| Add-on SHA-256 | `9e933d46410e130db944e10495fc4d0750d15c0ae5cc6be4664acce89599c186` |
| HushFeed | [v0.71.1](https://github.com/SysAdminDoc/HushFeed/releases/tag/v0.71.1); source `c86799e2820870746203f2614ce027eb4e0770c8`; 132 named patches |
| External bundle SHA-256 | `2af01462eb712a20d6479fe09d33f82e0e2483e7613d5fa8c9a81309271aced0` |
| TikTok | Original `com.zhiliaoapp.musically`, 47.1.4 / 2024701040; minSDK 23; ARM64/ARMv7 |
| APK SHA-256 | `4226ed5d3031b68208c29f62d80ac421b281fc74201bc4163d98e442d0991a40` |
| Original signer SHA-256 | `9041803e91bcb814b4b4399fb5c85a91640b755e5e8ba76813814bf4cf2ab5ba` |

Tools remain pinned in [BUILDING.md](../../BUILDING.md). Exactly two separately selectable patches and one unnamed common dependency. Payload audit: **75 unique own classes**, 30 Collections / 33 Sound / 12 common; no duplicate, foreign, compile-only or test payload classes. HushFeed upstream/source/bundle and original APK are unchanged. No APK was signed, installed, published or redistributed.

## Implementation and native evidence

- Inline CountSlider is part of the Collection popup card, with ticks 1–20, live count and immediate user saving. Keyboard changes and supported accessibility changes use the same path; programmatic refresh/reset is silent. Reset/About dialogs remain separate. The old count-dialog implementation only supports restoring an already-open legacy dialog.
- Shared NativeUiBridge calls `X.0d6x(Context).LIZ(0)`, the exact native routine invoked by Favorites open/selection. HushFeed/device suppression is respected. No new vibration permission or forced vibration API.
- Valid short disc taps cancel the direct feedback target and replay copied original DOWN/UP through original Activity dispatch under a re-entry guard. Original custom delegate and child routing remain authoritative. Successful holds/cancelled/scroll/multitouch/changed-track sequences never replay; copied events are recycled.
- Native title context matching uses current track and same Activity window instead of strict VideoItemParams object identity. Native `ReusedAssem.rq()` is preferred over recorded weak data; unbind still removes associations.
- Native `MusicDetailApi$DetailApi.queryMusic(String,int)` annotations identify `/aweme/v1/music/detail/`, `music_id`, `click_reason`. Its native ListenableFuture is fetched off the main thread, reason zero, through TikTok's own client/authentication. The four-argument variant has a music-compliance argument; it is not guessed/forced. Inspection of `MusicDetailApi.LIZ(Map)` found Christmas/template/live-scene extras, not a reason to invent DSP query flags.
- Bounded lookup: four-second total deadline, at most two IDs, two daemon worker slots/two queued requests. Original ID or an explicitly native-declared matched recording only. MusicDetail status and identity are checked; recommendations rejected. No title searches, installed-app scan, provider credentials, new HTTP client or auth bypass. Native destinations are filtered again before dispatch.
- Closing/rebinding/detaching/changing track cancels obsolete work. UI generation/track checks reject late results. A temporary Checking music apps footer disappears on completion; failure retains TikTok saving. Inserting rows clears hover/release-selection until fresh movement, preventing a new destination from being selected silently.
- Width observes native `X.0yck.Qk(List)` after its body and enhanced Collections after measurement. Fallback is the inspected `ik3` collection-row layout's 212 dp maximum, bounded to the visible window. Weak width samples invalidate on screen/density/font/RTL changes. Sound works without selecting Collections.
- Sound uses the same host `a24` sheet/AMOLED background, 15% contrast overlay and native feedback. Hover offsets clipped popup-root rectangles into screen coordinates before comparing raw events. Same-row movement does not repeat selection-change vibration.

## Automated evidence

**76 tests passed**, zero failures/errors: 31 Collections, 37 Sound (eight lookup tests), seven common UI/style, one patch-wrapper regression. NewApi lint passes for all three runtime modules at minSDK 23. Tests run under Robolectric SDK 28/plain JUnit, not Android ART.

New tests cover inline placement/immediate saving/native feedback/silent refresh; original-window short-tap replay and parent timer cancellation; same-track/window context with distinct parameter objects; native-equivalent highlight/width/haptic changes; offset/clipped hover rectangles; asynchronous rows without accidental save and stale responses; exact/matched IDs, unrelated rejection, deadline, cancellation and sanitized failure. Native bridge/service calls are fixtures: no actual account, network, device vibration or server saves are exercised.

A forced `:patches:generateMetadata --rerun-tasks` rebuild reproduced the exact bundle hash above. This proves repeatability with this local pinned toolchain, not across all operating systems/toolchains.

## Compiled integration

Current machine reports use `detail6-hushfeed-0.71.1-*.json`, preserving historical reports. They record exact hashes and `androidDeviceTested: false`, resolve native references, verify isolated payloads/no duplicates, settings/gesture wrappers and native haptic/detail/width bindings. Supplemental NativeHookAudit checks nine sites: cover/title bind/unbind, cover inflation, disc touch, edge timer, Activity dispatch and Favorites width observer.

| Selection | Source order | Compiled evidence |
| --- | --- | --- |
| Both add-ons + all 132 HushFeed patches | Add-on first | [PASS](detail6-hushfeed-0.71.1-both-all-addon-first.json), 75 own classes; nine-hook audit PASS |
| Both add-ons + all 132 HushFeed patches | HushFeed first | [PASS](detail6-hushfeed-0.71.1-both-all-hush-first.json), 75 own classes; nine-hook audit PASS |
| Sound + HushFeed Settings only | Add-on first | [PASS](detail6-hushfeed-0.71.1-sound-minimal-addon-first.json), 45 own classes, no Collections; nine-hook audit PASS |
| Collections + HushFeed Settings only | HushFeed first | [PASS](detail6-hushfeed-0.71.1-collections-minimal-hush-first.json), 42 own classes, no Sound |

An all-patches selection uses upstream default patch-time options and is a stress test, not a recommendation to enable all runtime features. No current-hash missing/duplicate-selection negative runs were performed; their historical evidence is not presented as a rerun. No Android ART or Manager UI workflow was exercised.

## Phone acceptance checklist

Repatch the **original** TikTok 47.1.4 with HushFeed 0.71.1 Settings and your desired add-ons in one run; replace the old add-on source, never select two versions. Keep your signing key/preferences. Settings stay at **Customize → BDCrusher's Settings**; explicitly select Sound button actions in Morphe.

- Count slider is visible immediately; drag/keyboard/accessible change saves current value and pulses when haptics are permitted. Returning/restarting preserves count; programmatic reset does not pulse.
- Ordinary disc tap opens stock sound page once. Inside-disc hold opens menu without 2x; outside-disc hold stays native. Test For You and search/sound-detail entry points, cancellation, multi-touch and rapid song changes.
- Compare a known sound-page YouTube Music track with hold options, wait for native lookup, then confirm real native linking/save outcome. Check exact/matched original sounds/remixes, account/region/provider differences, offline/errors and dismissal while loading. No eligibility guarantee for every native experiment.
- Compare width/highlight and actual vibration against Favorites, before/after opening Favorites. Test light/dark/AMOLED/custom colors, narrow screens, large fonts, RTL, rotation, scrolling, Back, hidden controls and pause/safe mode. Native HushFeed Turn off haptics can suppress feedback.
- Confirm no saves on stationary release, scroll/cancel/outside release or asynchronous row insertion; no duplicate saves or unintended unfavorite. Dedicated TalkBack disc long-click action remains unimplemented; accessibility/device acceptance remains pending.

If still failing, share **Sound button actions → Gesture status**, page version, HushFeed version and where it was reproduced. Status is session-only and contains no song IDs, account details, provider objects or credentials.

## Debug storage

Existing verification scratch remains untouched as explicitly requested. New runs are isolated under `build/verification`; small reports stay here and source packaging excludes all scratch/vendor inputs. Do not distribute or delete debug scratch without renewed authorization.

Current four retained runs:

- `both-all-addon-first-9916614452224073049`
- `both-all-hush-first-6940506447935807365`
- `sound-minimal-addon-first-17654806283719661503`
- `collections-minimal-hush-first-7771023164965367569`

Complete read-only inventory after these runs: **17,691,255,399 logical bytes (16.476 GiB), 426,117 files**, no skipped links/read errors. Prior retained scratch (12.447 GiB) is included; this revision adds about 4.03 GiB. Nothing was deleted. Scratch can be regenerated from exact inputs/toolchain; deletion still requires renewed authorization.
