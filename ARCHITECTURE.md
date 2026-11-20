# Implementation boundaries

## Two selectable patches, one shared settings hub

`enhancedCollectionsPatch` and `soundButtonActionsPatch` are independently selectable. Their engines live in `app.collectionsaddon.runtime.*` and `app.soundaddon.runtime.*`, with separate unchanged preference files. Both depend on the unnamed `settingsHubPatch`, which injects `app.bdcrusher.runtime.*` once. Exactly two patches are exposed in metadata; shared navigation is not a third selectable feature. No HushFeed patches or implementations are embedded. Compile-only stubs and the shared API compilation JAR never enter feature payloads; the generator checks namespace isolation and duplicates across all three payloads.

After extension merge, the settings patch validates HushFeed's UI and pause contracts. It wraps `createMasterMenu`, `showAsList`, `onResume` and the two saved-state callbacks. Each original body runs in a separate register frame. The wrappers preserve arguments and read the host's list setting before inserting the section.

BDCrusher's Settings uses HushFeed's compact `SettingsMenuPreference` with its native chevron, accessibility actions and child visibility. Tapping it expands the selected add-on rows on the home screen. HushFeed's list mode displays those rows under a fixed heading. The expanded state survives Activity recreation and the summaries update on return from a feature page. Each child opens its controls using the same fragment container and back stack. The feature pages use HushFeed's public palette and honor its independent settings theme.

The section discovers the two optional adapter classes by their fixed names. An unselected feature has no row, while an adapter that fails to load has a disabled recovery row. Each adapter provides a page and reports the number of changed settings.

The flat home entry goes immediately after section_interface. If absent, it follows the last available Your feed route (feed_filter/feed_navigation/playback/screen_time), using untranslated keys. With no feed routes, a compact Personal add-ons heading and entry precede Hushfeed's own heading, or append safely if that heading is absent. Existing row identities/order and negative header/search/quick-setup orders are preserved. New headings are inserted only after assigning row orders so PreferenceGroup's binary insertion never sees an unsorted list. The old Customize/hub_* placement is not used. Other HushFeed versions are not claimed compatible; preferences/defaults are unchanged.

`CollectionSettingsFragment` uses HushFeed's existing fragment container/back stack and public header/card/palette helpers. CountSlider lives directly inside its Popup card, saves each user change immediately and pulses native Favorites feedback. Silent refresh/reset does not pulse. Reset/About remain restorable DialogFragments; a legacy count-dialog branch can restore an already-open old dialog but no current setting opens it. View references are released on destruction; preference names and defaults are unchanged.

## Native hooks for 47.8.3

| Host boundary | Change |
| --- | --- |
| Native Favorites response consumer | Observe the original collection response and optionally await more pages. |
| Original inlined Favorites renderer | Replace the list cap and More threshold and install bounded themed content. A wrapper routes pagination and a single stock rendering retry. |
| Native Favorites popup touch and rows | Intercept actual scrolling and style the stock pressed overlay. Keep TikTok's existing clipped hit test in screen coordinates. |
| Native Favorites dismiss | Release per-dialog scrolling state. |
| HushFeed `createMasterMenu` | Add the independent settings entry after merge. |

Exact descriptors, fields and structural anchors are checked in the patch source and current verification. APK version name and code must match. Compatibility metadata also specifies the original signer SHA-256. No upstream source files are edited.

If the native response/render methods already contain the add-on hooks, patching refuses an already-applied or second-selected copy. Finalization also avoids reinserting an existing settings hook. Double-selection rejection is covered by a combined negative test.

`HostBridge` has placeholder Java bodies which the patch replaces with validated native DEX references. It retains TikTok's response model, pinned-section/cursor/hasMore/total fields and Rx schedulers. `NativeCallback` receives the host consumer interface at patch time. Native-class reflection, downloaded executable code and a separate saving API are not introduced. The shared settings hub reflects only its own two fixed optional adapter classes.

## Loading and failure behavior

The first native response is used directly if it already fills the requested count or is exhausted. Otherwise a per-consumer session requests native collection pages until count/exhaustion. Deduplication preserves first-seen IDs/order; special null/empty-ID entries remain intact. Whole responses preserve the host's pinned-section metadata; they do not invent a separate pinned/collaborative order.

Loading has a five-second deadline and at most five additional distinct cursors. Repeated/non-positive cursors, disabled/paused state or an error return the original response through the original consumer with enhancement temporarily suppressed. Done sessions reject late callbacks; subscriptions are disposed; closing/destroying the Activity suppresses a late popup and releases the native pending flag. Additional page callbacks are marshalled to the main thread. This is not an account-level cache implementation: TikTok's own loader/cache remains authoritative.

More uses both list size and native `hasMore`; a bounded weak identity record avoids confusing equal cached lists with different exhaustion states. If UI installation fails before show(), an internal signal aborts that inlined frame. The response wrapper retries the original native construction exactly once with enhancement suppressed, preventing the outer frame from showing a second popup. Unmatched patch fingerprints stop patching, rather than producing a silently disabled installation.

## Theme, space and gestures

The live host Activity or Dialog theme supplies `UISheetFlat1`, the 47.8.3 APK's `attr/a26` token (`0x7f0603e7`). HushFeed's AMOLED patch changes dark `aif` and restores stock values when its switch is off. The shared resolver verifies the entry name and accepts only opaque colors. It deliberately does not reapply a build-time AMOLED color from `hushfeed-build-v1.txt`, since that would override the live switch. A missing or unsuitable token falls back using the host context's night configuration. Text, native icons, separators and pressed overlays follow the same palette; cover images keep their original colors. A fixed-count popup may contain More or Create as an additional action row.

The content is measured against usable window space and capped at 60% height. Long content is wrapped in a ScrollView; anchors are clamped inside the screen. Moving beyond touch slop sends native ACTION_CANCEL before consuming scroll/release, so the native pressed row cannot be saved on that release. Offscreen clipped rows cannot be selected by screen-coordinate hit testing. Native callbacks, thumbnail construction and short-list selection remain in TikTok.

HushFeed owns rail visibility and the Favorites long-press speed remapping. The add-on does not hook their gesture entry or force a hidden button visible. Pause/safe mode is read through `HushfeedPause.isPaused()`, leaving saved add-on settings intact. Runtime preferences and token lookup avoid hardcoded app package names; metadata still identifies the required original input package.

## Sound native forwarding and touch ownership

Actual bound-disc sequences are reserved at the shared Main/Detail Activity dispatch boundary, before parent timers. Other touches reach the original body. During the hold decision, events are translated into disc-local coordinates for native feedback. A valid short tap cancels that direct target, then replays the copied original DOWN and UP through Activity dispatch with a depth guard. It reaches the native window delegates and normal child click path. Native edge handling is not suppressed during that replay; its UP cancels timers. Successful holds and cancelled, scrolled, multitouch, disabled or changed-track sequences never replay. Copied events are recycled; weak window ownership is retained.

Both native cover containers are registered. A finalization-time argument-preserving onViewCreated wrapper calls the native body, then registers the current `ReusedAssem.getItem()` data after inflation. This covers bind-before-view-creation and recreated views without guessing a last song. Finalization keeps execute-time HushFeed fingerprints able to inspect their original method. Owner cleanup only removes its own bindings, not a newer owner's recycled view. Session-only diagnostics count window events, captured disc presses, menu openings and visible eligible bindings, with no song IDs or credentials.

Cover binding forwards native execution in a separate register frame before recording original `VideoItemParams`. The scoped unbind removes the binding and invokes the original lifecycle. Disc `onTouch` forwards native press feedback, then combines native and add-on consumption without losing the original View and MotionEvent arguments. Sound does not use a title hook in this port.

The native edge listener is fingerprinted by its ability, MotionEvent handler and timer call. `SoundRuntime.skipEdgeTouch` checks the same-window visible clipped disc hit area before that timer is queued. Reserved downTime sequences swallow the listener's tail too, so a native UP never uses old coordinates from a previous DOWN. The child disc still gets its own stock touch and click processing. Reservations never extend across a new DOWN; weak keys prevent persistent view ownership. Disabled, paused, hidden, transparent, invalid-track and unrelated-window presses are not reserved. An already reserved sequence is drained until UP or CANCEL even if settings change mid-press, avoiding synthetic stock UP without stock DOWN.

Production identity delegates to native track and music getters; the injectable package-private identity is used for deterministic tests. The simulated menu regression checks UI dispatch sequencing. TikTok's native authenticated add-to-favorites action owns the server response and actual save result.

## Shared popup style and TikTok sound saving

`PopupStyle` centralizes the live `a26` sheet background, a 15% contrasting row highlight, text contrast and native Favorites feedback. A weak Resources-keyed width sample records actual Favorites sizing and is invalidated by screen, density, font and RTL changes. Sound observes native Favorites rendering with an argument-preserving wrapper; Collections records its enhanced measured width. Neither feature requires the other's payload.

The 47.8.3 Sound menu offers only **Save sound to TikTok** for the currently bound eligible track. It does not perform the external provider lookup or native detail enrichment used by older releases. The menu validates its current binding before dispatching the native save action and closes on invalidation.

Sound rows use native-equivalent width/highlight/haptics. Hover offsets clipped root-local rectangles by the PopupWindow root screen origin before comparing raw touch coordinates. Programmatic state refresh and repeated hovering within one row do not emit number/row-change haptics. Real vibration remains subject to native HushFeed/device settings.

## Privacy and scope

The inline collection-count SeekBar has 20 Canvas ticks on API 23, native tinting, a live 1–20 value and immediate saving on user changes. Tick locations mirror in RTL; accessibility range/actions map native zero-based progress to 1–20. Haptics follow native Favorites preferences/system restrictions. API lint/simulations are not physical accessibility/pixel acceptance.

No new manifest permission, telemetry, third-party endpoint, borrowed credential, or private authentication extraction. Collection pagination sends only what TikTok's native loader already requires, through the existing app session. The add-on owns no playback/download/filter/privacy/navigation logic. HushFeed preferences are neither reset nor migrated. Its backup/export does not automatically include this separate preference file.

Device acceptance is still needed: native API resolution at compilation is not proof of ART loading, visual correctness, or duplicate-save absence on an actual app session.
