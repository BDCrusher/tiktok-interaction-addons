# Settings refresh regression evidence — beta.2

Date: 2026-10-08. Final runtime payload tested with Robolectric 4.17/JUnit, simulated API 28. HushFeed compile-only test stand-ins provide contracts, not upstream UI implementations: these are logic/lifecycle checks, not real-app visual acceptance.

30 tests passed: PagerTest 10, PanelTest 6, RuntimeTest 7, SettingsPageTest 7. Android NewApi lint (minSDK 23) reports no issues.

New coverage: insertion after Feed screen/before Watching and sharing; minimal settings-only group creation; no duplicate entry; defaults have no changed badge; deviations count correctly; full fragment page/back-stack navigation; badge refresh on returning; immediate enable/disable; disabled count row; count range validation; cancellation; confirmed reset without clearing a host preference sentinel; large-font stacking; actual simulated Activity recreation restoring the page and unsaved count dialog, followed by a successful save.

Existing loader/gesture/fallback regression tests remain unchanged, except replacing the old settings-dialog test with the new page's count picker test. Native TikTok popup behavior is not exercised by these simulations.

Commands:

```powershell
$env:ANDROID_HOME = 'USER_HOME\AppData\Local\Android\Sdk'
.\gradlew.bat :patches:generateMetadata :extensions:collections:testDebugUnitTest :extensions:collections:lintDebug --console=plain
```

Tests and lint were also run as separate Gradle invocations while combined APK compilation ran. All final suites report zero failures/errors. Full XML/HTML reports remain under ignored `extensions/collections/build/`.

Outstanding: installed TikTok/HushFeed page visuals, actual hardware/system Back, light/dark mode switching, TalkBack, RTL, multi-window, OEM keyboard behavior and installed-update preference retention. A compilation pass does not verify ART runtime loading.
