# TikTok 47.9.3 port verification

Verified against the supplied original APK and published HushFeed artifact on 2026-11-24. [Machine-readable evidence](tiktok-4793-port-evidence.json).

| Input | Identity | SHA-256 |
| --- | --- | --- |
| TikTok APK | Global `com.zhiliaoapp.musically` 47.9.3 / 2024709030, ARM64 and ARMv7, minSDK 23, target SDK 36 | `7321ae3537572004956e2308c7eb5b1d82e6eb536c2c2fac432f3d8a1801ff70` |
| HushFeed | 0.84.0, source `d0543d2cbb1d7216cf7d01a6e3a94a1561d2aacc` | `ad41e505daf4c47277461598b9e11ae3e47031f0f92040c18330fc73a9988b41` |
| Personal add-ons | 0.10.0-beta.1 | `5af8cfeeb736f57d88e8eb6ec2ad3cd7b1dcaedf00d6d9ee5dfd1083cc7dd94c` |

APK signature schemes v1, v2 and v3 verified with SHA-256 signer `9041803e91bcb814b4b4399fb5c85a91640b755e5e8ba76813814bf4cf2ab5ba`. The HushFeed SHA-256 matches the published release asset digest. The compatibility verifier loads the delivered bundle in a separate classloader and checks references against the complete compiled DEX output.

- **Patcher 2.5.0** at `5003d688333c40317703d45a091a496d831dd27e`. This is HushFeed's minimum engine and the engine pinned by [Manager 2.8.0-dev.2](https://github.com/MorpheApp/morphe-manager/releases/tag/v2.8.0-dev.2). Morphe Manager requires version 2.7.0 or newer for this HushFeed release.
- Both add-ons compile alongside all 155 global HushFeed patches in both source orders. Each compiles with HushFeed Settings alone. Four negative cases cover missing Settings and repeat selections. Global TikTok was the verification target.
- 74 local unit and simulated tests, seven release helper tests, and three Android lint tasks passed. The combined personal payload has 61 isolated runtime classes and two selectable patches.
- The opened settings pages use the host's pinned header, actual settings list adapter, section headings, cards and switches. The simulator covers page navigation, recreation and the 1–20 collection slider. The compiled DEX check verifies both features call into the shared native renderer.
- Sound now initializes the native music collection ViewModel before launching TikTok's authenticated add action. The compiled checks verify its lifecycle initializer, native task and completion callback linkage. The simulator covers a successful, unconfirmed or interrupted completion and checks ViewModel cleanup.
- Compiled resources retain the opaque AMOLED and light popup styles and the restoration reference for TikTok's stock dark style. The Sound delay still follows the native Favorites experiment.

Phone rendering and signed-in server persistence remain pending. Verification used static APK inspection, compiled patches and simulated controls and callbacks.
