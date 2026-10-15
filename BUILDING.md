# Build and reproduce

## Toolchain

Use JDK 17, an Android SDK containing platform 36 and build-tools 36.0.0, and the included Gradle 9.7.1 wrapper. Its distribution checksum is pinned. Initial builds need network access for Maven/Google dependencies. No provider/API keys are used.

The composite builds use unmodified upstream tools:

| Tool | Commit/version |
| --- | --- |
| Morphe patch Gradle plugin | `a230c33f7eca1e18b07bf1114ca850421520f98d` / 1.3.4 |
| Morphe patcher | `524b9d8de5c2ec8f8cea0022cbce9a026d098679` / v1.15.0 |
| Scaffold/wrapper origin | template `5ec391b94cc6cb099a638576db0ba72fb0a921ae` |
| Smali | `d856bad65f` |

`build-tools/` is intentionally ignored, not distributed as patch payload. On this workstation its checkouts are already present. For a fresh source checkout, run these from the project directory:

```powershell
git clone https://github.com/MorpheApp/morphe-patches-gradle-plugin.git build-tools/morphe-patches-gradle-plugin
git -C build-tools/morphe-patches-gradle-plugin checkout --detach a230c33f7eca1e18b07bf1114ca850421520f98d
git clone https://github.com/MorpheApp/morphe-patcher.git build-tools/morphe-patcher
git -C build-tools/morphe-patcher checkout --detach 524b9d8de5c2ec8f8cea0022cbce9a026d098679
$env:ANDROID_HOME = 'USER_HOME\AppData\Local\Android\Sdk' # Replace for your machine.
.\gradlew.bat :patches:test :patches:generateMetadata :extensions:collections:testDebugUnitTest :extensions:collections:lintDebug :extensions:sound:testDebugUnitTest :extensions:sound:lintDebug :extensions:settings:testDebugUnitTest :extensions:settings:lintDebug --console=plain
```

The local composite replaces the GitHub Maven patcher dependency. `gpr.user/key=local-build` are placeholders, not real credentials; they satisfy the plugin's configuration for an unused authenticated repository. Do not put private tokens in this project. `-PmorphePluginDir=...` can override the plugin checkout path. Set `ANDROID_HOME` even when passing `-PandroidSdkDir=...` for integration-stub compilation.

`generateMetadata` builds the two-independent-patch bundle, checks both patch names, audits each isolated payload and verifies there are no duplicate payload classes, then produces the delivery files under `dist/`. Compile-only integration stubs and Kotlin dependencies are excluded from the injected runtime. Kotlin classes in the **patch bundle itself** are build/patcher code, not host payload. Each feature module is injected only when selected. Both depend on one unnamed shared settings patch; its `settings.mpe` is injected once. `integrationClassesJar` exposes the hub API to feature compilation/tests only, without copying it into their payloads.

## Combination verification

Acquire HushFeed separately from [v0.74.0](https://github.com/SysAdminDoc/HushFeed/releases/tag/v0.74.0), verify its release checksum/receipt, and place it under ignored `verification/inputs/`. It requires Manager 1.34.0+ and was published with patcher 1.15.1; our diagnostic remains pinned to 1.15.0. The user's Manager 1.36.0-dev.5 / patcher 1.15.2-dev.1 / Android 17 are not represented as local device verification. Never redistribute HushFeed or vendor APKs in this add-on. Supply the original TikTok 47.1.4 APK; the restored local input matches the historical SHA-256.

```powershell
.\gradlew.bat :patches:verifyCombination "-Papk=C:\path\to\original-47.1.4.apk" "-Phushfeed=C:\path\to\hushfeed-0.74.0.mpp" -Pselection=both-defaults -Porder=hush-first --console=plain
```

Collections-only selections: `core`, `amoled`, `extended`, `defaults`, `missing` (negative dependency), `duplicate` (negative double-selection), or `collections-minimal` (HushFeed Settings only). Sound-only: `sound-minimal`, `sound-extended`, `sound-defaults`, `sound-missing`, `sound-duplicate`. Both: `both-extended`, `both-defaults`, `both-all` (all 135 named external patches/default options; stress test, not an installation recommendation). Orders: `hush-first` / `addon-first`. Isolated scratch goes under `build/verification/`; no signing/install/publishing. Checks include payload isolation, DEX/resources, native references, argument wrappers, five-argument menu constructor, absence of the retired badge call, and native haptic/detail/width bindings. Current reports use `flat7-<external-bundle-name>-<selection>-<order>.json`; historical prefixes remain unchanged. An isolated classloader loads actual bundle code/resources. Check disk space first; retain debugging scratch as requested.

`extended` selects core plus AMOLED, Remember clear display, Change app name, and Look like the store app. Options use upstream defaults unless a report explicitly says otherwise. `defaults` selects exactly the external bundle's default patches. Do not equate this with all HushFeed patches.

After a completed combination containing Sound, run the supplemental read-only audit against that run's compiled DEX directory:

```powershell
.\gradlew.bat :patches:verifyNativeHooks "-PcompiledDex=C:\path\to\build\verification\<exact-run>\scratch\patched\dex" --console=plain
```

`verification/NativeHookAudit.java` checks nine hook sites: cover/title bind and unbind, cover view creation, native disc touch, edge-timer guard, early Activity window dispatch and Favorites width observation. Its classes remain under ignored `build/`, never in the payload. It does not execute TikTok. `probeDex` supports `-PprobeMethod=@annotations` to inspect native method/parameter annotations, useful for validating endpoint signatures without executing requests.

## Repeatability

Archive entry order and timestamps, including the manifest Timestamp, are fixed. A forced `--rerun-tasks` rebuild can be compared by hash; local repeatability is not a cross-platform guarantee. The GitHub workflow verifies source on ordinary pushes/PRs and publishes only after an explicit workflow-dispatch choice. See [PUBLISHING.md](PUBLISHING.md) for public snapshot/privacy checks, immutable releases and main/dev update feeds. Local source ZIPs may contain historical paths; publish only an audited public snapshot/archive.

After updating compatibility reports, run `gradlew.bat :patches:packageSource` to generate the source ZIP and checksums for every delivered `.mpp`, JSON and ZIP. The source archive excludes tool checkouts, build caches, input bundles/APKs, generated host scratch files, Git internals and local SDK settings. It contains the build instructions, source, notices, wrapper and verification reports.

## Remaining acceptance

Use COMPATIBILITY.md and SOUND_BUTTON.md device checklists before promoting beyond beta. Compiled checks cannot prove ART execution, coordinates, native detail/account eligibility, real vibration, accessibility or server-side saves. Bounded native detail loading is implemented but needs phone validation. A TalkBack-specific disc long-click action is still not implemented.
