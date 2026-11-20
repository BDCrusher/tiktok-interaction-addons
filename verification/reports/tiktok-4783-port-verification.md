# TikTok 47.8.3 port verification

Verified on 2026-11-19 against the supplied global TikTok APK and the published HushFeed bundle. The checks cover patch application, compiled bytecode and resources, and simulated gestures and settings navigation. Phone verification is pending.

| Input | Identity | SHA-256 |
| --- | --- | --- |
| Original TikTok | `com.zhiliaoapp.musically` 47.8.3 / 2024708030, 476,347,379 bytes, ARM64 and ARMv7, minSDK 23, target SDK 36 | `30acb1301f97ae7cc51c820914ee021bc02ec80b717a126f4b01520f8c8643e0` |
| HushFeed | [0.83.0 release](https://github.com/SysAdminDoc/HushFeed/releases/tag/v0.83.0), 8,650,141 bytes, source `b1fd77880aa486613a0dbe9a5e03ab68f3a37500` | `ef0c73f97a8e9ac4e798d8c5c93c78cb9227c58a45addec0cc8b32cacd1a305c` |
| TikTok Interaction Add-ons | `TikTok-Interaction-Addons-0.9.0-beta.1.mpp`, 134,304 bytes | `18ec267e05469ff70f9939e89a484d758dc18ad3752a434dd707d46a3fe298e8` |

AAPT2 verified the APK manifest and ABI. APK signature schemes v1, v2 and v3 verified with signer SHA-256 `9041803e91bcb814b4b4399fb5c85a91640b755e5e8ba76813814bf4cf2ab5ba`. The external bundle matches GitHub's asset digest and HushFeed's checksum file. The verifier loaded the exact personal bundle using a separate classloader.

- **Patcher 2.4.0** (`0aed87c4e3264cf9233feab7ed26cf2543a08b96`). Both add-ons compiled with all 151 HushFeed patches in both source orders. Each add-on compiled independently with only HushFeed Settings. Four negative cases rejected missing Settings or duplicate add-on copies.
- **Patcher 2.5.0-dev.4** (`cc2842fdef15267d7727ab7aaefe845e02bde604`), bundled in [Manager 2.7.0-dev.6](https://github.com/MorpheApp/morphe-manager/releases/tag/v2.7.0-dev.6). Both add-ons compiled with all 151 HushFeed patches in both source orders. The verifier materialized the complete DEX output for class and reference checks.
- **73 unit and simulated tests**, **7 release helper tests**, and **3 Android API lint tasks** passed. Payloads contain 62 isolated runtime classes across two selectable patches and their shared settings dependency. Compile-only HushFeed declarations stay out of the payloads.
- The settings section uses HushFeed's native expandable group, chevron and row styling. The compiled wrappers preserve the arguments of the home-menu, list-mode, resume and saved-state callbacks. Simulated tests check child visibility while the section expands and collapses, list view, page navigation and restoration across Activity recreation. Feature pages use the active HushFeed settings theme.
- Favorites retains its native renderer, collection loader, clipped row hit test and stock fallback. Sound tracks both record containers and their lifecycle, uses the native Favorites 200/500 ms timing choice, and invokes TikTok's authenticated save after row selection. Compiled resource checks confirm opaque AMOLED black and white sheet styles plus the stock restore token in both source orders.

[Machine-readable evidence](tiktok-4783-port-evidence.json) records the artifact sizes, hashes and all selections. The all-patches runs use HushFeed's default options as a stress check. The exact patcher engines ran directly. Device checks still cover touch timing, account-specific saving, rendering, and hardware accessibility gestures.
