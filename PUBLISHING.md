# Remote Morphe source and releases

Repository: [BDCrusher/tiktok-interaction-addons](https://github.com/BDCrusher/tiktok-interaction-addons).

[Add TikTok Interaction Add-ons to Morphe](https://morphe.software/add-source?github=BDCrusher%2Ftiktok-interaction-addons&name=TikTok%20Interaction%20Add-ons).

Morphe reads root `patches-bundle.json` on main/dev. Its required fields are version, download_url, created_at and description; signature_download_url is empty because no separate bundle signature is claimed. Timestamps are UTC ISO local-date-time strings (no trailing zone) as accepted by the current Manager. Release downloads are immutable version-tag URLs, not moving latest URLs.

The **0.7.0-beta.1** port targets original TikTok 47.5.3 / 2024705030 and verified HushFeed 0.78.0/0.79.0 with Manager/patcher 2.0.0. Beta publication advances dev; main retains its older version. Use the release page and actual patches-bundle.json to confirm current publication state.

## Channels and installation

This source began with the same beta on main/dev; the channels have since advanced separately. Enable **Pre-release patches**, then **Experimental app versions** on its Morphe source card. Future beta publications advance dev; stable publications advance main. Manager's prerelease mode compares both feeds so a newer stable release is not missed.

Remove/disable the old local source rather than selecting it alongside the remote one. Keep HushFeed as its own external source. For 0.7.0-beta.1, repatch original TikTok 47.5.3 with HushFeed 0.79.0 Settings and your selected add-ons in one Expert-mode run. Source updates do not patch/reinstall TikTok automatically and do not automatically repair compatibility with future HushFeed versions.

## Maintainer workflow

1. Modify source on main/dev, update gradle.properties/version and CHANGELOG.md for a new immutable version. Keep runtime About labels in sync. Do not reuse a published version.
2. Perform app-specific compatibility verification locally against legally obtained original inputs. CI has no vendor APK, cannot reproduce phone behavior and does not claim to run all HushFeed combinations.
3. Push the reviewed source. The Build workflow runs unit tests, lint, payload audit, helper tests and source-archive privacy checks. Ordinary pushes and pull requests **never publish**.
4. In GitHub Actions, run **Build, test and optionally release** on the intended main/dev revision, explicitly enabling **publish**. The write-enabled job runs only after verification succeeds and only in the canonical repository. It uses GitHub's repository-scoped built-in token, no private credentials stored in source.
5. Publication creates a versioned release, verifies the publicly downloaded .mpp SHA-256, then advances that channel's feed through GitHub's contents API with an expected-file-SHA check. It refuses a feed downgrade and never replaces release assets. A failed feed update can leave an unadvertised release; inspect it instead of overwriting a version.

The initial publication used the already combined-build-verified 0.5.1-beta.1 bundle. For the 0.7.0-beta.1 beta update, publish the exact locally combined-build-verified bundle after public-source CI passes, using the shared Toolkit github_publish.py prepare/validate/publish commands in place. CI rebuilds have their own hashes and must not be substituted while claiming the local APK-combination evidence. The current candidate/published hash is recorded in COMPATIBILITY.md and the publication record; local cross-source evidence applies only to that hash. The immutable release source archive comes from the reviewed public Git revision, not this private workspace.

## Local private workspace versus public snapshot

Keep APKs, external HushFeed inputs, build-tool checkouts, local SDK settings, credentials and large verification scratch out of Git. `scripts/export_public.py` exports only reviewed source/documentation trees to a **fresh** destination, prunes caches/inputs, rejects credential patterns/links and redacts personal Windows user-home prefixes in historical documentation. It never alters the original workspace or copies Git history. Historical paths in a public snapshot may therefore say USER_HOME rather than a person's account name.

```powershell
python scripts/export_public.py . build/publication/repository-NEW
```

Review that snapshot and its staged file list before committing/pushing. Do not blindly add the private workspace or upload its local source ZIPs; those can contain unredacted historical paths. An initial public source archive is made from the audited public Git commit; later CI archives come from that public checkout only. The only allowed source binary is Gradle's wrapper JAR. Releases contain patch bundles/source/checksums, never vendor or prepatched APKs.

`scripts/release.py prepare` emits Manager-compatible release metadata and bounded release assets. The old Gradle generateMetadata output under dist is a local inventory, not a remote feed; always run the release helper before publishing. Export/prepare are local operations. `publish` is an explicit external write and must be authorized by the maintainer.
