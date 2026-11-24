# Sound button actions — development beta

**0.10.0-beta.1** is a separately selectable patch for original global TikTok **47.9.3 / 2024709030** with external **HushFeed 0.84.0 Settings**. Select it explicitly in Morphe. Its runtime switch defaults on once selected.

## Behavior and settings

Settings: **HushFeed → BDCrusher's Settings → Sound button actions**, after Feed screen when available. With no feed pages selected, the hub is under **Personal add-ons**. The feature keeps its preferences in `sound_actions_addon_v1`. Reset affects only that file.

An ordinary music-disc tap opens TikTok's native sound page. Hold a visible eligible disc to open **Save sound to TikTok**. Drag onto the visible row and release, or release over the disc and tap the row. A hold and release on the disc alone never saves the sound. Moving beyond touch slop before the hold, multitouch, cancellation, detach, rebind or a track change cancel selection.

The record uses the same native delay choice as the feed Favorites button: normally 200 ms, or 500 ms when TikTok enables its longer-hold experiment. The menu is ready without a song-detail lookup. If you are signed out, TikTok prompts you to sign in; open the menu again afterward.

The save action initializes TikTok's music collection ViewModel, then runs its authenticated add-to-favorites job. A success message follows confirmation from the native music object. An unconfirmed or interrupted job shows a failure message. If TikTok already marks the sound as collected, the add-on reports that state.

The popup uses Favorites' measured width when available and a bounded fallback otherwise. It follows the live host sheet palette, including HushFeed's AMOLED switch and standard dark/light colors. Selection compares clipped visible row bounds in screen coordinates. Haptics use TikTok's native Favorites feedback and remain subject to HushFeed and device settings.

**Gesture status** shows session-only window events, bound and eligible discs, captured presses and menu openings. It refreshes when returning or toggling the feature. If a hold fails, share this readout, the build version and the feed/search/sound route. Counters reset after process restart and contain no track IDs, user data or credentials.

## Native hooks and testing

The patch validates the Activity's original touch-dispatch contract, disc-binding/view lifecycle, native feedback and edge-timer boundaries. A hold takes priority only inside a bound eligible disc. During the hold decision the native disc receives local feedback events. A valid short tap cancels that direct target, then replays the original DOWN and UP through full Activity dispatch under a depth guard. Successful holds and cancelled sequences never replay. Hidden, disabled, paused, invalid and unrelated-window touches stay stock.

Source: `patches/src/main/kotlin/app/soundaddon/patches/SoundPatch.kt` binds the exact verified APK. `extensions/sound/src/main/java/app/soundaddon/runtime/` contains the isolated native bridge, gesture policy, runtime and settings. Compile-only HushFeed declarations are excluded from the payload. The shared popup style and settings hub are injected once.

See [COMPATIBILITY.md](COMPATIBILITY.md) for automated and compiled checks against the exact candidate. [0.7.1 color-repair evidence](verification/reports/theme-tint-071-verification.md) and the [47.5.3 / HushFeed 0.79.0 report](verification/reports/hushfeed-079-tiktok-4753-verification.md) are historical. They do not establish compatibility or behavior of the new version.

**Phone validation is pending.** Verify original taps, hold without movement, slide and release, release outside, scrolling before hold, multitouch, detach and rapid track changes. Confirm native save results after relaunch, account errors, and that duplicate saves or unfavorites do not occur. Check light/dark/AMOLED/custom colors, fonts, rotation, RTL, Back, TalkBack, native Favorites width and device haptics. Check pause, hidden disc, clear display, Favorites remapping and both source orders. A dedicated accessible disc long-click action is not implemented.

## Build and install

Use the pinned toolchain and confined launchers in [BUILDING.md](BUILDING.md). In Morphe Expert mode select **HushFeed 0.84.0 Settings** and **Sound button actions** in the same run against original 47.9.3. Collections is optional. Enable **Pre-release patches** and **Experimental app versions** for the beta source. Keep your existing signing key. The HushFeed bundle requires Manager **2.7.0+** and Patcher **2.5.0+**. Source updates require repatching; they do not change an already installed app. No APK is distributed or installed automatically.
