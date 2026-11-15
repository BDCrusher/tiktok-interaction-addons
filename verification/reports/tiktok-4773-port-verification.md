# TikTok 47.7.3 port verification

Verified on 2026-11-15 against the original global TikTok APK and the exact published HushFeed bundle. This report covers host compilation, compiled bytecode and resources, and simulated gestures. Phone testing is pending.

| Input | Identity | SHA-256 |
| --- | --- | --- |
| Original TikTok | `com.zhiliaoapp.musically` 47.7.3 / 2024707030, 469,814,182 bytes, ARM64 and ARMv7, minSDK 23, target SDK 36 | `9b35d4caed14ae54938eca1fa27fc89ce8307a9cf8350b80f66a9968fc3684e9` |
| HushFeed | [0.82.0 release](https://github.com/SysAdminDoc/HushFeed/releases/tag/v0.82.0), 7,994,969 bytes, source `86131614f2ac78ccb92b5e68af3e6bc27d3d6d79` | `e5bf347ebbc365f3bb3b131dc868f99592f23e2aa34122edf86eaf8443678f46` |
| TikTok Interaction Add-ons | `TikTok-Interaction-Addons-0.8.0-beta.1.mpp`, 134,492 bytes | `f534c03189dffa2268afa53b81fe3e8518cb8ca06f62859e157a73176ccdd15f` |

APK signature schemes v1, v2 and v3 verified. Original signer SHA-256 is `9041803e91bcb814b4b4399fb5c85a91640b755e5e8ba76813814bf4cf2ab5ba`. The original APK hash matches HushFeed's fixture pin. The external bundle digest matches its release. The test loader isolated patch classes and resources from the exact add-on `.mpp`. The vendors' binaries stay local.

## Results

- **Patcher 2.3.0** (`d19545e8505e0329cc640723cb8ff91eddd3cd05`). Both add-ons and all 147 HushFeed patches compiled in both source orders. Each add-on also compiled independently with HushFeed Settings alone. Four negative cases rejected a missing Settings patch or a duplicate copy, for both features.
- **Patcher 2.4.0-dev.8** (`82383c3c805d4cf545ef9e55b48f6935f24fc4e5`), the engine pinned by [Manager 2.6.0-dev.6](https://github.com/MorpheApp/morphe-manager/releases/tag/v2.6.0-dev.6). Both add-ons and all 147 HushFeed patches compiled in both source orders. The verifier materialized unchanged original DEX files for a complete duplicate-class and reference audit.
- All **72 unit and simulated tests**, **7 publication helper tests**, and **3 Android API lint tasks** passed. The payload has **65 isolated classes** and two selectable patches, with shared settings code injected once. No compile-only stubs or external implementations were included.
- Compiled native bindings cover disc setup and rebinding, window dispatch priority, live Favorites width, settings and pause integration, and TikTok's authenticated `MusicCollectViewModel` save. The original Favorites timer chooses **200 ms or 500 ms** using TikTok's experiment flag. Sound uses the same flag and timing. Tests cover the single **Save sound to TikTok** row, both timings and saving only after row selection.
- Favorites still uses native collection data, callbacks, clipped hit areas and fallback to the stock sheet. AAPT2 inspection confirms `a26` resolves through `aif` to opaque AMOLED black (`#FF000000`) and through `axq` to opaque light (`#FFFFFFFF`) in both source orders. The stock restoration resource remains available, and a simulated test covers live changes to the backing palette.

[Machine-readable evidence](tiktok-4773-port-evidence.json) records exact inputs, each selection and its corresponding detailed report. The all-patches cases used default options as a stress test. Manager 2.5.0 is the minimum supported version. The compatibility checks run the pinned patcher engines directly. No patched app was signed or installed. Phone checks still cover actual touch timing, save and sign-in, sheet rendering and Favorites interactions.
