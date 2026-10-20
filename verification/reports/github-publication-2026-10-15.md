# GitHub source publication — 2026-10-15

Published at the user's explicit request. Repository: https://github.com/BDCrusher/tiktok-interaction-addons

Release: https://github.com/BDCrusher/tiktok-interaction-addons/releases/tag/v0.5.1-beta.1

Add to Morphe: https://morphe.software/add-source?github=BDCrusher%2Ftiktok-interaction-addons&name=TikTok%20Interaction%20Add-ons

## Identity and contents

- Public source commit/tag target: `72fc72f7e60c4902768f59518ba82eb87c66cc55`.
- Published `.mpp`: 139,032 bytes, SHA-256 `900c0bd68a27a6014aac3d8ed7cfdc7a41757998e4c8475c245d3523e6f6e054`. This is the unchanged locally combined-build-verified bundle, not a newly substituted cloud build.
- Reviewed Git source archive: 285,389 bytes, SHA-256 `2e137c6676009190113edfa833eb76dbb70f728dcc87f8c603c44394a1530068`.
- Seven assets: `.mpp`, public source ZIP, patch list, payload audit, remote metadata, release notes and checksums. GitHub release is public, not draft, marked prerelease.
- Exactly two selectable patches; external HushFeed remains separate and unchanged. Target remains original TikTok 47.1.4 / 2024701040 with HushFeed 0.74.0 Settings.

## Completed checks

1. Public source export allowlist/audit excluded vendor inputs, caches, Git history, local SDK settings and credentials; personal Windows user-home prefixes in historical documentation were redacted. Private originals/debug scratch were preserved.
2. Seven release-helper tests passed both privately and in the public checkout/cloud build.
3. GitHub Actions [run 38732368807](https://github.com/BDCrusher/tiktok-interaction-addons/actions/runs/38732368807) passed on the source commit: pinned build tools, Android SDK, Gradle compilation, existing unit/simulated tests, all three NewApi lint tasks, isolated payload audit, source-archive privacy audit and artifact preparation/upload. Initial SDK/PATH and obsolete default SDK-package problems were repaired in the workflow. Ordinary pushes/PRs do not publish; explicit dispatch enables the write job.
4. Anonymous HTTPS downloads of all seven public release assets matched their local SHA-256 values and the GitHub asset digests. Public downloads are retained locally under `build/publication/download-check-20261015`.
5. Anonymous root metadata requests on both `main` and `dev` returned version `0.5.1-beta.1` and the immutable, verified release `.mpp` URL. Initially both channels advertise this beta; future prereleases advance dev, stable releases advance main.

The cloud build generated SHA-256 `06a0844d03f045b6e83dc8392b86a9defb1b6fa3e5771abf4bd3f4177cefa702`; that CI artifact was **not** substituted for the published local bundle. Local repeatability is not a cross-platform hash guarantee. Exact APK/HushFeed combination evidence applies to the published `900c...` bundle as recorded in COMPATIBILITY.md, not automatically to other binaries.

## Maintenance and limitations

Private working source: `C:\Morphe\Projects\TikTok\Add-ons`. Reviewed public Git checkout: `build/publication/repository-20261015`; this has fresh history and the GitHub remote, unlike the private root's existing unborn/untracked Git state. Public release assets are isolated in `dist/public`. Do not commit/push the private umbrella directory or upload old private source ZIPs. Read PUBLISHING.md before exporting or releasing again.

Morphe installation: use the add-source link or repository URL, enable Pre-release patches and Experimental app versions, and remove/disable the old local source rather than selecting two versions. Keep HushFeed external. Updates refresh patch source files; the user must still repatch/reinstall through Morphe to apply them. New HushFeed versions do not become compatible automatically.

No Manager UI import, physical-device run, account-specific eligibility/save flow, APK signing or installation was performed. Device acceptance remains pending. No vendor APK, HushFeed bundle, private input or large debug scratch was published or deleted. No recurring automation was created.
