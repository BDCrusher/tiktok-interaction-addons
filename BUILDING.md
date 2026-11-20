# Build and reproduce

## Current toolchain

The 0.9.0-beta.1 port targets original global TikTok 47.8.3 / 2024708030 and external HushFeed 0.83.0. The published HushFeed bundle requires Morphe Manager 2.6.0 or newer, with Patcher 2.4.0 or newer. Build the add-on against 2.4.0 for that floor. See [COMPATIBILITY.md](COMPATIBILITY.md) for the exact tested versions. No device installation is performed by these diagnostics.

Use JDK 17 (local pin Microsoft 17.0.21.21), Android SDK platform 36/build-tools 36.0.0 and the included checksum-pinned Gradle 9.7.1 wrapper. Set `JAVA_HOME` explicitly if your shell defaults to a different JDK. Initial dependency resolution needs network access. No provider or API keys are used.

| Dependency | Exact pin |
| --- | --- |
| Morphe patches Gradle plugin 1.3.4 | `a230c33f7eca1e18b07bf1114ca850421520f98d` |
| Morphe Patcher v2.4.0 | `0aed87c4e3264cf9233feab7ed26cf2543a08b96` |
| Smali | `5eaff5a2c9` |
| ARSCLib | `50418a07a8` |
| Scaffold/wrapper origin | template `5ec391b94cc6cb099a638576db0ba72fb0a921ae` |

Composite checkouts are ignored `build-tools/morphe-patches-gradle-plugin` and `build-tools/morphe-patcher-2.4.0`. The historical 1.15.0 and 2.0.0 checkouts are retained unchanged. Fresh checkouts must use the pins above. `-PmorphePluginDir=...` and `-PmorphePatcherDir=...` override their locations; match the Smali dependency to the selected Patcher. Manager v2.7.0-dev.6 (`d1173b0686f9d093d3afaa66497a2e59ced5ade1`) bundles Patcher v2.5.0-dev.4 (`cc2842fdef15267d7727ab7aaefe845e02bde604`, Smali `5eaff5a2c9`), the separate beta verification engine. Use `-Paddon=<absolute path to built .mpp>` to verify the exact stable-built bundle with a different test engine and `-PverificationLabel=<label>` to keep those reports distinct. The local Maven placeholders are not credentials; do not put private tokens in this project.

## Local build and tests

Run from this repository:

```powershell
python -B scripts/local_build.py :patches:generateMetadata :extensions:collections:lintDebug :extensions:sound:lintDebug :extensions:settings:lintDebug
python -B scripts/local_tests.py
python -B scripts/local_build.py :patches:packageSource
```

`local_build.py` confines Gradle, Android and Java writable homes, caches and temporary files to ignored `build/local-build/`. It reads the existing JDK/SDK in place and disables automatic JDK downloads. Set JAVA_HOME and ANDROID_HOME for another workstation. Dependencies may be seeded from an existing cache; do not relocate or mutate a shared cache. All task output stays in the app repository.

Gradle 9.7.1's Windows test-worker argument file mishandles the apostrophe in this workspace path. `local_tests.py` exports actual Gradle test classpaths, dependencies, system properties and working directories using `local_test_runtime.init.gradle`, then runs JUnitCore directly without a Java argument file. It runs the same compiled JUnit/Robolectric tests and Python release-helper tests; results are retained in module `build/local-junit.log` files and `build/local-build/junit-results.json`. The simulated tests use API 28 and do not establish Android 17 device behavior.

`generateMetadata` builds the two independently selectable patches and checks their names and isolated payloads. Compile-only host stubs, test classes and foreign implementations are excluded. Both features depend on one unnamed common settings patch; common infrastructure is injected once. Kotlin patcher code in the bundle is separate from injected host runtime code.

## Exact combination verification

Acquire the external [HushFeed v0.83.0 release](https://github.com/SysAdminDoc/HushFeed/releases/tag/v0.83.0) separately, verify its published checksums, and keep it in ignored inputs. Supply the original TikTok 47.8.3 APK. See [COMPATIBILITY.md](COMPATIBILITY.md) for exact artifact identities and results. Never distribute vendor or prepatched APKs with this add-on.

```powershell
python -B scripts/local_build.py :patches:verifyCombination "-Papk=C:\path\to\original-47.8.3.apk" "-Phushfeed=C:\path\to\patches-0.83.0.mpp" -Pselection=both-all -Porder=hush-first
```

Collections selections: `core`, `amoled`, `extended`, `defaults`, `collections-minimal`, `missing`, `duplicate`. Sound selections: `sound-minimal`, `sound-extended`, `sound-defaults`, `sound-missing`, `sound-duplicate`. Both: `both-extended`, `both-defaults`, `both-all`. `both-all` selects every compatible public HushFeed 0.83.0 patch with default options as a stress check. `defaults` selects the external defaults. Orders are `hush-first` and `addon-first`.

The four negative selections must explicitly reject missing Settings or duplicate add-on selection. Positive checks compile DEX/resources, check duplicate classes and injected references, and audit settings arguments and native bindings. The verification harness supplies a DEX verifier so STRIP_FAST materializes the complete DEX directory, including unchanged original entries held in memory by Patcher 2.4. No signing or installation is performed.

Machine reports identify the exact inputs and selection/order. Preserve earlier evidence before reruns. Historical reports remain separate. Scratch is retained under `build/verification/`; check free space before full APK runs. Do not delete retained scratch without cleanup authorization.

After a completed selection containing Sound:

```powershell
python -B scripts/local_build.py :patches:verifyNativeHooks "-PcompiledDex=C:\path\to\exact-run\scratch\patched\dex"
```

`verification/NativeHookAudit.java` checks native lifecycle, disc touch, Activity dispatch, edge-timer guarding and Favorites width observation. Audit classes stay under ignored build output. `probeDex` supports `-PprobeMethod=@annotations` for inspecting native contracts without executing requests.

## Delivery and acceptance

`packageSource` writes a local source ZIP and checksums beside the bundle and metadata in `dist/`. It excludes tool checkouts, caches, input APKs/bundles, host scratch, Git internals and SDK settings, while retaining source, notices, wrapper and verification reports. This private source ZIP can contain historical local paths; it is not a privacy-approved public export. Publication is a separate explicitly authorized action; see PUBLISHING.md. The authorized beta publication advances dev only after exact-asset verification. Main is not the beta channel.

Archive order/timestamps are fixed. A forced rebuild can assess local repeatability; it is not a cross-platform guarantee. Source updates do not automatically repatch or reinstall TikTok.

Physical-device acceptance remains pending: ART loading, short taps/holds/cancellation, native TikTok sound saving, haptics, themes, rotation, fonts/RTL and accessibility. Favorites hold-speed remapping conflicts with the collection hold; keep it off when using that gesture. Hidden controls, HushFeed pause/safe mode and Turn off haptics remain authoritative. A dedicated TalkBack sound-disc long-click action is still not implemented.
