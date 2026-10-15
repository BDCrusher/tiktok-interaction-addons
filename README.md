# TikTok Interaction Add-ons

An independent, personal beta source for Morphe with **two separately selectable patches: Enhanced collection popup and Sound button actions**. Selecting either does not inject the other's runtime. HushFeed remains a separate requirement and is neither included nor modified on disk. Source now lives at `C:\Morphe\Projects\TikTok\Add-ons`; implementation namespaces and the Gradle project ID remain unchanged.

**[Add TikTok Interaction Add-ons to Morphe](https://morphe.software/add-source?github=BDCrusher%2Ftiktok-interaction-addons&name=TikTok%20Interaction%20Add-ons)** · [GitHub releases](https://github.com/BDCrusher/tiktok-interaction-addons/releases) · [Publishing and update guide](PUBLISHING.md).

For remote updates, add this repository once and enable **Pre-release patches**, then **Experimental app versions** on its source card. Replace/disable the old local add-on source; do not select both copies. HushFeed stays separate. Morphe can fetch new patch bundles automatically; applying one still requires repatching TikTok. Future HushFeed changes still need compatibility work.

Both features live under **HushFeed → Your feed → BDCrusher's Settings**, immediately after Feed screen when that route is available, with separate **Collection popup** and **Sound button actions** pages. With no feed pages selected, a **Personal add-ons** group is inserted before Hushfeed's own group. Only selected add-on patches appear; disabling a feature does not hide its settings. Native headers/palette/cards and existing saved preferences are retained.

**Sound button actions** is an opt-in development beta. Hold the spinning music disc for native TikTok sound saving and TikTok-eligible music-app destinations. Its page contains a full-page enable switch and behavior/privacy notes. Ordinary disc taps retain their native route. See [SOUND_BUTTON.md](SOUND_BUTTON.md), especially its known limits and device test checklist.

Long-press Favorites to open TikTok's native vertical collection popup. By default it shows **five collections before More**, instead of three. Choose 1–20 using an **inline slider on the Collection popup page**, with 20 tick marks and a live value. User changes save immediately and request the same native haptic feedback as Favorites. No extra count dialog or Save button. Ordinary favorite/unfavorite taps and TikTok's save, create-collection, and More actions are retained.

The popup follows the host's active sheet palette and HushFeed's applied AMOLED/custom dark color, keeps light mode light, limits its height to 60% of usable screen space, and scrolls longer lists. Scrolling cancels release-to-select. Extra pages use TikTok's existing authenticated collection loader; the add-on does not introduce a saving API, credential store, or external network service.

## Status and limits

Version **0.5.1-beta.1**, targeting original TikTok **47.1.4 / 2024701040**, package `com.zhiliaoapp.musically`, with **HushFeed 0.74.0**. This repair adopts its five-argument settings constructor and flat section routes. The removed changed-count badge API is replaced by our own refreshing summary text. Inline slider/native feedback, original-window short taps, bounded song-detail lookup and shared Favorites popup styling are retained. Older/future HushFeed versions are not verified for this bundle. See [COMPATIBILITY.md](COMPATIBILITY.md) for exact completed checks and historical evidence.

**Not release-accepted: the repaired settings/gesture update still needs a physical-device test.** JVM/Robolectric tests and compiled DEX/resource checks are not proof of correct rendering, account-specific data, or save behavior on a phone. Other TikTok/HushFeed releases require verification. No universal compatibility claim.

## Install locally

1. Copy `dist/TikTok-Interaction-Addons-0.5.1-beta.1.mpp` to your Android device. Check its SHA-256 against `dist/SHA256SUMS.txt`. Replace the older add-on source rather than selecting both versions. Earlier bundles remain archived locally.
2. Recommended: use the add-source link above, or **Sources → + → Remote** and paste `https://github.com/BDCrusher/tiktok-interaction-addons`. Enable **Pre-release patches** and **Experimental app versions**. For offline/local use, pick the .mpp through **Sources → + → Local** instead; local imports still require manual replacement. See Morphe's [source instructions](https://github.com/MorpheApp/morphe-manager/blob/main/docs/patch-sources.md).
3. Enable **Settings → Advanced → Expert settings → Expert mode**. Select the **original, unmodified 47.1.4 APK**, then your usual **HushFeed 0.74.0** patches and either/both add-ons in **one run**. **HushFeed Settings is required.** Explicitly select Sound button actions if wanted; its patch-selection default is off. Accept experimental-target/multi-source confirmations. Do not patch an already-patched APK or combine two add-on versions. HushFeed 0.74.0 requires Manager 1.34.0+; our local diagnostic toolchain and the user's newer development Manager are distinguished in the compatibility report. PineDrama support in upstream HushFeed does not extend this add-on's target.
4. Build/install through Morphe's normal workflow. The add-on does not distribute TikTok or a prepatched APK. Android may require uninstalling an app signed with a different key; preserve anything you need before doing that.
5. Open **HushFeed → Your feed → BDCrusher's Settings** (after Feed screen; Personal add-ons fallback if no feed pages), then **Collection popup** or **Sound button actions**. Collections defaults remain enabled/five. Switch and inline slider changes save immediately and apply to the next popup. Reset affects only its feature. Keep your signing key when updating to retain preferences.

Morphe documents multi-source selection in [Expert mode](https://github.com/MorpheApp/morphe-manager/blob/main/docs/patching-expert-mode.md). The Manager workflow itself has not been exercised here.

### HushFeed choices

- Keep **Favorites hold-speed remapping off** under Long-press controls. Selecting that patch is allowed; enabling its Favorites remap consumes the same long press and intentionally prevents this menu. The add-on explains the conflict and does not override it.
- Hidden feed save/Favorites buttons stay hidden. Show the button in HushFeed if you want to trigger the popup there. The add-on does not defeat clear-display behavior.
- HushFeed pause/safe mode returns this feature to stock behavior for that process while retaining its preferences.
- Select AMOLED if desired; this add-on reads its applied build color for dark surfaces, not a separate AMOLED preference.
- Haptics use TikTok's native Favorites routine. HushFeed's **Turn off haptics** or device haptic restrictions can suppress vibration; this update does not bypass them.
- See the compatibility report for the exact tested selections. You do not need to disable unrelated HushFeed features solely because this is a separate source, but untested combinations are not guaranteed.

## Settings and fallback

One BDCrusher's Settings entry opens a full-page hub and independent feature pages, using HushFeed 0.74.0's flat home layout. Placement uses section keys, not translated headings; no obsolete Customize route or duplicate Your feed heading is injected. The settings wrapper preserves original argument registers; shared infrastructure is injected once. The inline 1–20 slider saves immediately, with API 23 ticks and native keyboard/accessibility controls. Reset/About stay separate. Programmatic refresh/reset is silent; narrow layouts stack the switch. Changed counts appear in our root/page summary text, not the removed upstream badge API. Disabling Collections restores stock layout/cap. Preference files/defaults are unchanged and remain separate from HushFeed backup/reset.

Additional loading stops once enough rows are available or data is exhausted. Repeated cursors, excessive pages, errors, or a five-second deadline return to the original response/stock route. More is retained when the native response says more items exist. Native order and duplicate-ID handling are documented in [ARCHITECTURE.md](ARCHITECTURE.md).

## Source and delivery

- `patches/`: two independent selectable patches, exact-version fingerprints, host bridge bindings, metadata generator.
- `extensions/collections/`: isolated runtime and simulated tests.
- `extensions/sound/`: isolated sound runtime and simulated tests; no Collections or HushFeed classes bundled.
- `extensions/settings/`: shared navigation payload, an unnamed dependency rather than a third selectable patch. Its compile-only integration JAR is not duplicated in feature payloads.
- `integration-stubs/`: compile-only HushFeed signatures; never injected.
- `dist/`: `.mpp`, source ZIP, generated metadata, delivery checksums and payload audit.
- `verification/reports/`: machine-readable local combination evidence.
- `build-tools/`: ignored, pinned build-tool checkouts; no HushFeed implementation.

Build/test instructions: [BUILDING.md](BUILDING.md). License: [GPL-3.0](LICENSE), with retained template notice in [NOTICE](NOTICE). Credits: [Morphe template](https://github.com/MorpheApp/morphe-patches-template), [HushFeed](https://github.com/SysAdminDoc/HushFeed). This is not an official project of either upstream.
