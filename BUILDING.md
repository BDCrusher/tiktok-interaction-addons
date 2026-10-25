# Build and reproduce

## Current toolchain

The 0.7.0-beta.1 port targets original global TikTok 47.5.3 / 2024705030 and external HushFeed 0.78.0 or 0.79.0. Use Morphe Manager 2.0.0 or newer with patcher 2.0.0. No device installation is performed by these diagnostics.

Use JDK 17, Android SDK platform 36/build-tools 36.0.0 and the included checksum-pinned Gradle 9.7.1 wrapper. Initial dependency resolution needs network access. No provider/API keys are used.

| Dependency | Exact pin |
| --- | --- |
| Morphe patches Gradle plugin 1.3.4 | `a230c33f7eca1e18b07bf1114ca850421520f98d` |
| Morphe patcher v2.0.0 | `8c7ff9f29d4cf6395fcbe42a4ceddd034d71fecf` |
| Smali | `4220ae75f7` |
| Scaffold/wrapper origin | template `5ec391b94cc6cb099a638576db0ba72fb0a921ae` |

Composite checkouts are ignored `build-tools/morphe-patches-gradle-plugin` and `build-tools/morphe-patcher-2.0.0`. The historical 1.15.0 checkout is retained unchanged. Fresh checkouts must use the pins above. `-PmorphePluginDir=...` and `-PmorphePatcherDir=...` override their locations. The local Maven placeholders are not credentials; do not put private tokens in this project.

## Local build and tests

Run from this repository:

```powershell
python -B scripts/local_build.py :patches:generateMetadata :extensions:collections:lintDebug :extensions:sound:lintDebug :extensions:settings:lintDebug
python -B scripts/local_tests.py
python -B scripts/local_build.py :patches:packageSource
```

`local_build.py` confines Gradle, Android and Java writable homes, caches and temporary files to ignored `build/local-build/`. It reads the existing JDK/SDK in place and disables automatic JDK downloads. Set JAVA_HOME and ANDROID_HOME for another workstation. Dependencies may be seeded from an existing cache; do not relocate or mutate a shared cache. All task output stays in the app repository.

Gradle 9.7.1's Windows test-worker argument file mishandles the apostrophe in this workspace path. `local_tests.py` exports actual Gradle test classpaths, dependencies, system properties and working directories using `local_test_runtime.init.gradle`, then runs JUnitCore directly without a Java argument file. It runs the same compiled JUnit/Robolectric tests and seven Python release-helper tests; results are retained in module `build/local-junit.log` files and `build/local-build/junit-results.json`. The current 83 JVM tests simulate API 28; they do not establish Android 17 device behavior.

`generateMetadata` builds the two independently selectable patches and checks their names and isolated payloads. Compile-only host stubs, test classes and foreign implementations are excluded. Both features depend on one unnamed common settings patch; common infrastructure is injected once. Kotlin patcher code in the bundle is separate from injected host runtime code.

## Exact combination verification

Acquire the external [HushFeed v0.79.0 release](https://github.com/SysAdminDoc/HushFeed/releases/tag/v0.79.0) separately, verify its published checksums/receipt, and keep it in ignored inputs. Supply the original TikTok 47.5.3 APK. Exact fixture identities and current results are in [the verification report](verification/reports/hushfeed-079-tiktok-4753-verification.md). Never distribute vendor or prepatched APKs with this add-on.

```powershell
python -B scripts/local_build.py :patches:verifyCombination "-Papk=C:\path\to\original-47.5.3.apk" "-Phushfeed=C:\path\to\patches-0.79.0.mpp" -Pselection=both-all -Porder=hush-first
```

Collections selections: `core`, `amoled`, `extended`, `defaults`, `collections-minimal`, `missing`, `duplicate`. Sound selections: `sound-minimal`, `sound-extended`, `sound-defaults`, `sound-missing`, `sound-duplicate`. Both: `both-extended`, `both-defaults`, `both-all`. `both-all` selects all 140 global TikTok patches in 0.79.0 with default options as a stress check, not an installation recommendation. `defaults` selects the external defaults, not all patches. `extended` adds AMOLED, Remember clear display, Change app name and Look like the store app to core. Orders are `hush-first` and `addon-first`.

The four negative selections must explicitly reject missing Settings or duplicate add-on selection. Positive checks compile DEX/resources, check duplicate classes and injected references, audit settings argument preservation and native bindings. Patcher 2 STRIP_FAST returns changed entries only; the verifier reads the complete compiled DEX directory, including unchanged originals. No signing or installation is performed.

Current machine reports use `verification/reports/port9-patches-0.79.0-<selection>-<order>.json`. A repeated identical selection overwrites that report; preserve earlier evidence before reruns. Historical reports remain separate. Scratch is retained under `build/verification/`; check free space before full APK runs. Do not delete retained scratch without cleanup authorization.

After a completed selection containing Sound:

```powershell
python -B scripts/local_build.py :patches:verifyNativeHooks "-PcompiledDex=C:\path\to\exact-run\scratch\patched\dex"
```

`verification/NativeHookAudit.java` checks nine roles: cover/title bind/unbind, cover view creation, native disc touch, edge-timer guard, early Activity dispatch and Favorites width observation. Audit classes stay under ignored build output. `probeDex` supports `-PprobeMethod=@annotations` for inspecting native contracts without executing requests.

## Delivery and acceptance

`packageSource` writes a local source ZIP and checksums beside the bundle and metadata in `dist/`. It excludes tool checkouts, caches, input APKs/bundles, host scratch, Git internals and SDK settings, while retaining source, notices, wrapper and verification reports. This private source ZIP can contain historical local paths; it is not a privacy-approved public export. Publication is a separate explicitly authorized action; see PUBLISHING.md. The authorized 0.7 beta publication advances dev only after exact-asset verification. Main is not the beta channel.

Archive order/timestamps are fixed. A forced rebuild can assess local repeatability; it is not a cross-platform guarantee. Source updates do not automatically repatch or reinstall TikTok.

Physical-device acceptance remains pending: ART loading, short taps/holds/cancellation, native account/provider eligibility and saving, haptics, themes, rotation, fonts/RTL and accessibility. Favorites hold-speed remapping conflicts with the collection hold; keep it off when using that gesture. Hidden controls, HushFeed pause/safe mode and Turn off haptics remain authoritative. A dedicated TalkBack sound-disc long-click action is still not implemented.
