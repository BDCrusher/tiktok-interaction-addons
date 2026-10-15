# HushFeed 0.69.0 compatibility verification

Date: 2026-10-08. Original TikTok 47.1.4 / 2024701040 only. This is local patch application, DEX/resource compilation and static integration verification, **not Android-device acceptance**.

## Exact inputs

- Unchanged add-on: TikTok Interaction Add-ons 0.2.0-beta.1, SHA-256 `bf3ec02f0bb76812a346949d8159dbc933de824add319a0b2b7b712bbaa213ae`.
- External [HushFeed v0.69.0](https://github.com/SysAdminDoc/HushFeed/releases/tag/v0.69.0), release commit `528feb5a307e5ba8eda85f913d2532ee6e61128c`. Published bundle: 5,126,429 bytes, SHA-256 `2d25ddeceb9abf469116d1c23688dc822079e53b755e8bd84cf2088e4b222b20`. Bundle and release-receipt checksums match the publisher's SHA256SUMS and receipt. HushFeed remains external and unchanged; it is excluded from the source archive.
- Original APK SHA-256 `4226ed5d3031b68208c29f62d80ac421b281fc74201bc4163d98e442d0991a40`; signer identity is recorded in COMPATIBILITY.md. The supplied APK is read-only input.
- Local diagnostic toolchain remains the pinned Morphe patcher 1.15.0 and JDK 17. HushFeed's release was built with patcher 1.15.1 and requires Morphe Manager 1.34.0+. This report does not claim a Manager UI import or patching run.

## Completed checks

Unless a row explicitly says otherwise, both independent add-on patches are selected. HushFeed patch options use upstream defaults. Changing the source order changes which source's selectable patches enter the local patcher first; internal dependency execution remains controlled by the patcher.

| Selection | Source order | Result |
| --- | --- | --- |
| All 130 selectable HushFeed patches | HushFeed first | PASS |
| All 130 selectable HushFeed patches | Add-on first | PASS |
| All 55 default-selected HushFeed patches | HushFeed first | PASS |
| All 55 default-selected HushFeed patches | Add-on first | PASS |
| Expanded 8-patch compatibility selection | HushFeed first | PASS |
| Expanded 8-patch compatibility selection | Add-on first | PASS |
| Sound only + HushFeed Settings only | Add-on first | PASS; 19 Sound classes, no Collections classes |
| Collections only + core HushFeed selection | HushFeed first | PASS; 27 Collections classes, no Sound classes |

Expanded selection: Settings, AMOLED dark theme, Long-press controls, Keep the Favorites tab, Hide feed save button, Remember clear display, Change app name and Look like the store app.

Core selection: Settings, Long-press controls, Keep the Favorites tab and Hide feed save button. Eight complete combination checks passed; the published bundle still contains 130 selectable patches, with 55 default-selected.

Each positive check applies patches, compiles combined DEX, compiles modified resources when full resource patching is selected (or checks raw resource output for minimal selections), audits output classes for duplicates, resolves the add-ons' native/HushFeed method and field references, and checks settings hooks. Both-add-on selections retain 46 isolated add-on classes; independent selections exclude the unselected payload. Reports record the exact input hashes and `androidDeviceTested: false`: `interaction-hushfeed-0.69.0-*.json` in this directory.

The diagnostic was extended with an explicit `both-all` mode, strict expanded-selection presence checks and failure-report recording. No production patch/runtime code was changed for this verification.

The supplemental `NativeHookAudit.java` check passed on all six compiled both-add-on combinations above: disc binding, title binding, title unbinding and native disc-touch hooks all remain present, with no duplicated or overwritten hook sites. Run it through `:patches:verifyNativeHooks` as documented in BUILDING.md. This is static instruction evidence, not proof of actual gesture routing on Android.

## Interpretation and remaining limits

All-130 selection is a build stress test, **not a recommendation to enable every patch or runtime switch**. It does not test every subset, custom patch option, localized pixel layout, settings value or server/account condition. Your screenshot's 71 selected patches are not individually identifiable from category counts, so this report does not claim an exact reproduction of that selection.

The release's source still uses the Feed screen `INTERFACE` section and Your feed heading expected by the add-on settings integration. Public UI helper contracts are checked against the released bundle during patching. Real rendered placement and navigation remain device-unverified.

Keep Favorites hold-speed remapping off to use the Collections popup. Hidden Favorites/music controls and clear-display state are intentionally not defeated. New playback, hiding, privacy and network controls may change visible controls or native service availability; passing compilation does not prove every runtime combination behaves correctly.

No APK was assembled, signed, installed or distributed. No settings, accounts or credentials were changed. Native saving/linking, disc gestures, collection pagination, themes, Back, rotation and accessibility still require phone checks. Sound's missing lazy-detail acquisition and TalkBack long-click path are unchanged beta limitations; see SOUND_BUTTON.md. A successful build is not a claim of complete external-song detection or release readiness.
