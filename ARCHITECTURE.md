# Implementation boundaries

## Two selectable patches, one shared settings hub

`enhancedCollectionsPatch` and `soundButtonActionsPatch` are independently selectable. Their engines live in `app.collectionsaddon.runtime.*` and `app.soundaddon.runtime.*`, with separate unchanged preference files. Both depend on the unnamed `settingsHubPatch`, which injects `app.bdcrusher.runtime.*` once. Exactly two patches are exposed in metadata; shared navigation is not a third selectable feature. No HushFeed patches or implementations are embedded. Compile-only stubs and the shared API compilation JAR never enter feature payloads; the generator checks namespace isolation and duplicates across all three payloads.

In patch finalization, after every source's extension merge, the patches require HushFeed's main settings fragment and public UI/pause contracts. Unsupported signatures or lifecycle hooks fail patching clearly. The shared dependency wraps createMasterMenu and onResume: each original body runs in a private native method's register frame, then the wrapper calls install/refresh with its unchanged incoming arguments. This fixes the 0.71.1 Context-register reuse that invalidated an old return-site injection. The runtime checks its key before adding the preference. Source order does not determine wiring. No Sound installer remains on createSectionMenu, and Feed screen availability is not overridden.

The BDCrusher's Settings page discovers only the two fixed optional adapter class names in its own classloader. Absent classes mean unselected modules and produce no row; a present broken adapter produces a disabled recovery row. No global static registry or last-opened Activity is needed, so restored fragments work even before the root has been reopened. Each adapter supplies its independent page and preference-change count. Full pages use the same fragment container/back stack and public HushFeed header/card/palette helpers. The hub retains scroll position and refreshes counts on return. Preferences are not reset or migrated.

HushFeed 0.74.0's SettingsMenuPreference constructor has five arguments; its integer count argument and setActiveCount method are gone. Compile-only stubs and patch-time validation match the new signature; runtime root counts use ordinary summary text and refresh on return. No upstream badge implementation is copied or reintroduced.

The flat home entry goes immediately after section_interface. If absent, it follows the last available Your feed route (feed_filter/feed_navigation/playback/screen_time), using untranslated keys. With no feed routes, a compact Personal add-ons heading and entry precede Hushfeed's own heading, or append safely if that heading is absent. Existing row identities/order and negative header/search/quick-setup orders are preserved. New headings are inserted only after assigning row orders so PreferenceGroup's binary insertion never sees an unsorted list. The old Customize/hub_* placement is not used. Other HushFeed versions are not claimed compatible; preferences/defaults are unchanged.

`CollectionSettingsFragment` uses HushFeed's existing fragment container/back stack and public header/card/palette helpers. CountSlider lives directly inside its Popup card, saves each user change immediately and pulses native Favorites feedback. Silent refresh/reset does not pulse. Reset/About remain restorable DialogFragments; a legacy count-dialog branch can restore an already-open old dialog but no current setting opens it. View references are released on destruction; preference names and defaults are unchanged.

## Native hooks for 47.1.4

| Host boundary | Change |
| --- | --- |
| `X.0ycl.accept(Object)` | Observe native collection response; optionally await more pages; override only popup-layout selection to vertical while enabled. |
| `X.0yck.Qk(List)` | Replace take(3) and size>=4 threshold; install bounded themed content. |
| `X.0yck.LJJZ(MotionEvent)` | Intercept only actual scrolling; native tap/short drag selection otherwise continues. |
| `X.0yck.LJJLL(View, boolean)` | Use a contrasting pressed overlay on the native row's highlight view. |
| `X.0yck.LJJZZIII(int, int, View)` | Clip enhanced hit testing to the visible portion of a row. |
| `X.0yck.dismiss()` | Release per-dialog scrolling state. |
| HushFeed `createMasterMenu` | Add the independent settings entry after merge. |

Exact descriptors, fields and structural anchors are checked. The initial target is deliberately not an obfuscation-agnostic patch. APK version name/code must match. The compatibility metadata also specifies the original signer SHA-256. No upstream source files are edited.

If the native response/render methods already contain the add-on hooks, patching refuses an already-applied or second-selected copy. Finalization also avoids reinserting an existing settings hook. Double-selection rejection is covered by a combined negative test.

`HostBridge` has placeholder Java bodies which the patch replaces with validated native DEX references. It retains TikTok's response model, pinned-section/cursor/hasMore/total fields and Rx schedulers. `NativeCallback` receives the host consumer interface at patch time. Native-class reflection, downloaded executable code and a separate saving API are not introduced. The shared settings hub reflects only its own two fixed optional adapter classes.

## Loading and failure behavior

The first native response is used directly if it already fills the requested count or is exhausted. Otherwise a per-consumer session requests native collection pages until count/exhaustion. Deduplication preserves first-seen IDs/order; special null/empty-ID entries remain intact. Whole responses preserve the host's pinned-section metadata; they do not invent a separate pinned/collaborative order.

Loading has a five-second deadline and at most five additional distinct cursors. Repeated/non-positive cursors, disabled/paused state or an error return the original response through the original consumer with enhancement temporarily suppressed. Done sessions reject late callbacks; subscriptions are disposed; closing/destroying the Activity suppresses a late popup and releases the native pending flag. Additional page callbacks are marshalled to the main thread. This is not an account-level cache implementation: TikTok's own loader/cache remains authoritative.

More uses both list size and native `hasMore`; a bounded weak identity record avoids confusing equal cached lists with different exhaustion states. If UI installation fails, that dialog is marked failed and the native rendering method is rerun at the stock cap. Unmatched patch fingerprints stop patching, rather than producing a silently disabled installation.

## Theme, space and gestures

The host Activity/Dialog theme supplies `UISheetFlat1`, the APK's `attr/a24` token. Its resource ID (`0x7f0603e5`) was inspected in the original fixture, and its entry name is checked at runtime; lookup is not dependent on the app's package name. The popup's explicitly white inflation theme is not used as the palette authority. If that host sheet token cannot be resolved, the host context's night configuration supplies a conservative fallback.

For dark host colors only, `assets/hushfeed-build-v1.txt` can supply the applied AMOLED/custom opaque background. Invalid/translucent colors are ignored. Root backgrounds, labels, native icons, separators, and pressed overlays get a coordinated palette; cover images are not recolored. No global TikTok white token is changed. A fixed-count popup may contain More or Create as an additional action row.

The content is measured against usable window space and capped at 60% height. Long content is wrapped in a ScrollView; anchors are clamped inside the screen. Moving beyond touch slop sends native ACTION_CANCEL before consuming scroll/release, so the native pressed row cannot be saved on that release. Offscreen clipped rows cannot be selected by screen-coordinate hit testing. Native callbacks, thumbnail construction and short-list selection remain in TikTok.

HushFeed owns rail visibility and the Favorites long-press speed remapping. The add-on does not hook their gesture entry or force a hidden button visible. Pause/safe mode is read through `HushfeedPause.isPaused()`, leaving saved add-on settings intact. Runtime preferences and token lookup avoid hardcoded app package names; metadata still identifies the required original input package.

## Sound native forwarding and touch ownership

Actual bound-disc sequences are reserved at the shared Main/Detail Activity dispatch boundary (`X.03uJ`), before parent timers. Other touches reach the original body. During the hold decision, events are translated into disc-local coordinates for native feedback. In 0.5 a valid short tap cancels that direct target, then replays the copied original DOWN and UP through Activity dispatch with a depth guard. This reaches both native custom/window delegates and the normal child click path exactly once instead of assuming the placeholder disc is the clickable target. Native edge handling is not suppressed during that synchronous replay; its UP cancels timers. Successful holds and cancelled/scroll/multitouch/disabled/changed-track sequences never replay. Copied events are recycled; weak window ownership is retained.

Both native cover containers are registered. A finalization-time argument-preserving onViewCreated wrapper calls the native body, then registers the current `ReusedAssem.rq()` data after inflation. This covers bind-before-view-creation and recreated views without guessing a last song. Finalization keeps execute-time HushFeed fingerprints able to inspect their original method. Owner cleanup only removes its own bindings, not a newer owner's recycled view. Session-only diagnostics count window events, captured disc presses, menu openings and visible eligible bindings, with no song IDs or credentials.

Cover/title z4 bindings forward native execution in separate register frames before recording original VideoItemParams. Cover's inherited unBind gets a scoped override that removes its binding and invokes the original superclass lifecycle; title unbind is hooked at entry. Disc onTouch forwards native press feedback, then combines native/add-on consumption without losing original View/MotionEvent arguments.

The native edge listener is fingerprinted by its ability, MotionEvent handler and 300 ms postDelayed call. SoundRuntime.skipEdgeTouch checks the same-window visible clipped disc hit area before that timer is queued. Reserved downTime sequences swallow the listener's tail too, so a native UP never uses old coordinates from a previous DOWN. The child disc still gets its own stock touch/click processing. Reservations never extend across a new DOWN; weak keys prevent persistent view ownership. Disabled, paused, hidden/transparent, invalid-track/sound and unrelated-window presses are not reserved. An already reserved sequence is drained until UP/CANCEL even if settings change mid-press, avoiding synthetic stock UP without stock DOWN.

Production Identity delegates to native track/music getters; the injectable package-private identity exists for deterministic tests, not external provider lookup. The simulated menu regression uses a fake native bridge and proves UI dispatch sequencing, not server-side saving. Real music eligibility and authentication remain TikTok's responsibility.

## Shared popup style and native sound-detail loading

`PopupStyle` centralizes host `a24`/AMOLED backgrounds, a 15% contrasting row highlight, text contrast and native Favorites feedback. A weak Resources-keyed width sample records actual Favorites sizing and is invalidated by screen/density/font/RTL changes; fallback is the inspected `ik3` layout's 212 dp maximum. Sound observes native Favorites rendering with an argument-preserving post-body wrapper; Collections records its enhanced measured width. Neither feature requires the other's payload.

Sound's title context is accepted only for the same current track in the same Activity window; independently allocated VideoItemParams no longer cause false rejection. If native feed destinations are empty, `SongLookup` queries the existing native detail Future off the UI thread. The total deadline is four seconds and there are at most two native IDs (original plus explicit matched recording), two worker slots and two queued jobs. Successful MusicDetail status and exact/native-matched IDs are validated; unrelated recommendations are rejected. Native eligibility filtering remains authoritative; objects are opaque. Closing/rebinding/changing track cancels requests and stale callbacks cannot update the menu. Reflow clears selection until a fresh movement; failures leave TikTok saving available.

Sound rows use native-equivalent width/highlight/haptics. Hover offsets clipped root-local rectangles by the PopupWindow root screen origin before comparing raw touch coordinates. Programmatic state refresh and repeated hovering within one row do not emit number/row-change haptics. Real vibration remains subject to native HushFeed/device settings.

## Privacy and scope

The inline collection-count SeekBar has 20 Canvas ticks on API 23, native tinting, a live 1–20 value and immediate saving on user changes. Tick locations mirror in RTL; accessibility range/actions map native zero-based progress to 1–20. Haptics follow native Favorites preferences/system restrictions. API lint/simulations are not physical accessibility/pixel acceptance.

No new manifest permission, telemetry, third-party endpoint, borrowed credential, or private authentication extraction. Collection pagination sends only what TikTok's native loader already requires, through the existing app session. The add-on owns no playback/download/filter/privacy/navigation logic. HushFeed preferences are neither reset nor migrated. Its backup/export does not automatically include this separate preference file.

Device acceptance is still needed: native API resolution at compilation is not proof of ART loading, visual correctness, or duplicate-save absence on an actual app session.
