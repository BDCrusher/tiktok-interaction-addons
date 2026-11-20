# TikTok Interaction Add-ons

An independent, personal beta source for Morphe with **two separately selectable patches: Enhanced collection popup and Sound button actions**. Selecting either does not inject the other's runtime. HushFeed remains a separate requirement and is neither included nor modified on disk. Source now lives at `C:\Morphe\App's\TikTok\Add-ons`; implementation namespaces and the Gradle project ID remain unchanged.

**[Add TikTok Interaction Add-ons to Morphe](https://morphe.software/add-source?github=BDCrusher%2Ftiktok-interaction-addons&name=TikTok%20Interaction%20Add-ons)** · [GitHub releases](https://github.com/BDCrusher/tiktok-interaction-addons/releases) · [Publishing and update guide](PUBLISHING.md).

For remote updates, add this repository once and enable **Pre-release patches**, then **Experimental app versions** on its source card. Replace/disable the old local add-on source; do not select both copies. HushFeed stays separate. Morphe can fetch new patch bundles automatically; applying one still requires repatching TikTok. Future HushFeed changes still need compatibility work.

Expand **BDCrusher's Settings** on the HushFeed home screen, after Feed screen when available. In HushFeed's list view, **Collection popup** and **Sound button actions** appear under the heading automatically. Tap a feature to open its controls. With no feed pages selected, look under **Personal add-ons**. The feature pages use HushFeed's selected settings theme.

**Sound button actions** is an opt-in beta. Hold the spinning music disc for **Save sound to TikTok**. This port does not offer external music-app destinations. Its page contains a full-page enable switch and gesture diagnostics. Ordinary disc taps retain their native route. See [SOUND_BUTTON.md](SOUND_BUTTON.md) for the device test checklist.

Long-press Favorites to open TikTok's native vertical collection popup. By default it shows **five collections before More**, instead of three. Choose 1–20 using an **inline slider on the Collection popup page**, with 20 tick marks and a live value. User changes save immediately and request the same native haptic feedback as Favorites. No extra count dialog or Save button. Ordinary favorite/unfavorite taps and TikTok's save, create-collection, and More actions are retained.

The popup follows the active TikTok sheet palette, including HushFeed's runtime AMOLED switch, keeps light mode light, limits its height to 60% of usable screen space, and scrolls longer lists. Scrolling cancels release-to-select. Extra pages use TikTok's existing authenticated collection loader; the add-on does not introduce a saving API, credential store, or external network service.

**0.9.0-beta.1** ports Favorites and Sound to TikTok 47.8.3 and HushFeed 0.83.0. Turning AMOLED off restores TikTok's dark palette. Enable **Pre-release patches** and refresh the remote source before repatching. For the previous TikTok 47.7.3 and HushFeed 0.82.0 pairing, use the [0.8.0 beta release](https://github.com/BDCrusher/tiktok-interaction-addons/releases/tag/v0.8.0-beta.1).

## Status and limits

Target: original global TikTok **47.8.3 / 2024708030**, package `com.zhiliaoapp.musically`, with **HushFeed 0.83.0 Settings**. The port updates the collection renderer, pagination, disc bindings and native TikTok save path. The five-argument HushFeed settings menu and section keys are retained. See [COMPATIBILITY.md](COMPATIBILITY.md) for exact checks, limitations and historical evidence.

**Physical-device validation is pending.** JVM/Robolectric tests and compiled DEX/resource checks do not prove correct rendering, account-specific data, or save behavior on a phone. Other TikTok and HushFeed versions require verification.

## Install or update

1. For local import, copy `dist/TikTok-Interaction-Addons-0.9.0-beta.1.mpp` to your Android device. Check its SHA-256 against `dist/SHA256SUMS.txt`. Replace the older add-on source. After publication, you can fetch the exact bundle by refreshing the remote source with Pre-release patches enabled.
2. Recommended: use the add-source link above, or **Sources → + → Remote** and paste `https://github.com/BDCrusher/tiktok-interaction-addons`. Enable **Pre-release patches** and **Experimental app versions**. For offline/local use, pick the .mpp through **Sources → + → Local** instead; local imports still require manual replacement. See Morphe's [source instructions](https://github.com/MorpheApp/morphe-manager/blob/main/docs/patch-sources.md).
3. Enable **Settings → Advanced → Expert settings → Expert mode**. Select the **original, unmodified 47.8.3 APK**, then your chosen **HushFeed 0.83.0** patches and either/both add-ons in **one run**. **HushFeed Settings is required.** Sound button actions defaults off in patch selection. Accept experimental-target and multi-source confirmations. Do not patch an already-patched APK or select two add-on versions. The HushFeed bundle requires Manager **2.6.0+** and Patcher **2.4.0+**. Desktop verification and Android Manager/device acceptance are separate.
4. Build/install through Morphe's normal workflow. The add-on does not distribute TikTok or a prepatched APK. Android may require uninstalling an app signed with a different key; preserve anything you need before doing that.
5. Open **HushFeed → BDCrusher's Settings** (after Feed screen; Personal add-ons fallback if no feed pages), then **Collection popup** or **Sound button actions**. Collections defaults remain enabled/five. Switch and inline slider changes save immediately and apply to the next popup. Reset affects only its feature. Keep your signing key when updating to retain preferences.

Morphe documents multi-source selection in [Expert mode](https://github.com/MorpheApp/morphe-manager/blob/main/docs/patching-expert-mode.md). The Manager workflow itself has not been exercised here.

### HushFeed choices

- Keep **Favorites hold-speed remapping off** under Long-press controls. Selecting that patch is allowed; enabling its Favorites remap consumes the same long press and intentionally prevents this menu. The add-on explains the conflict and does not override it.
- Hidden feed save/Favorites buttons stay hidden. Show the button in HushFeed if you want to trigger the popup there. The add-on does not defeat clear-display behavior.
- HushFeed pause/safe mode returns this feature to stock behavior for that process while retaining its preferences.
- Select AMOLED if desired; add-on menus follow the live sheet color, including HushFeed's runtime switch.
- Haptics use TikTok's native Favorites routine. HushFeed's **Turn off haptics** or device haptic restrictions can suppress vibration; this update does not bypass them.
- See the compatibility report for the exact tested selections. You do not need to disable unrelated HushFeed features solely because this is a separate source, but untested combinations are not guaranteed.

## Settings and fallback

BDCrusher's Settings uses HushFeed's expandable menu and follows its list view toggle. Section placement uses the HushFeed 0.83 keys. Each feature page has its own controls, summaries and reset. The collection slider saves immediately, with API 23 ticks and native keyboard and accessibility controls. Narrow layouts stack the enable switch. Disabling Collections restores the stock layout and count.

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
