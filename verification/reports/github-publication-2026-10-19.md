# Published TikTok Interaction Add-ons 0.6.0-beta.1

Published with the user's explicit approval on 2026-10-19 America/Phoenix (GitHub timestamp 2026-10-20T05:16:12Z). This updates the existing beta source, not HushFeed or an APK.

- Release: https://github.com/BDCrusher/tiktok-interaction-addons/releases/tag/v0.6.0-beta.1
- Reviewed public source/tag commit: `ea801c2828a1c0280f30d4e6e2dcb3aa0cd77102` on dev.
- Clean-source CI: https://github.com/BDCrusher/tiktok-interaction-addons/actions/runs/39373781511 — success, verify job 118467234581. Automatic publication job was intentionally skipped on push; the authorized local release helper published the exact locally APK-combination-tested bundle afterwards.
- Beta feed update commit: `ac50c2cd6a045add6fec2f374b3c5daa212313f0` on dev. The public raw dev feed was fetched and confirmed as 0.6.0-beta.1 after publication. Main remains the previous channel; Pre-release patches is required for this update.
- Public local checkout: `build/publication/repository-20261015`, clean dev at the feed commit after a fast-forward. The private development source remains the authoritative project folder; it was never blindly pushed.
- Export: 147 allowlisted source/documentation files; personal Windows home paths sanitized. APKs, external bundles, caches, build tools and verification scratch excluded.

## Published immutable assets

| Asset | Bytes | SHA-256 |
| --- | ---: | --- |
| TikTok-Interaction-Addons-0.6.0-beta.1.mpp | 141201 | `c65ae77f2f1add0dcdd6ca9462e204717b0a860639b9b5c68029e59ce7891d10` |
| TikTok-Interaction-Addons-0.6.0-beta.1-source.zip | 309041 | `7be9935184012ae4a57fcc8f6d582fef03c2916e8402a5ef34a226322dd2d398` |
| patches-bundle.json | 528 | `081dea5d7c6edd06067f17bcb209b4b70dd7bdefdbc5abd20ecb8f753ba82554` |
| patches-list.json | 1830 | `a0f5c81ccd3494d5247fbd926178af9e4f363bca9df697607c95c11ca5ae13e3` |
| payload-audit.json | 5207 | `ae08706b25d41ac47724b6f422c57a95e6a454dc86ca92ea8bb9c21044bc38d6` |
| RELEASE-NOTES.md | 941 | `6d43d609eb46e2881ef9558d8bb19fa1e62d4f1a200057e12786c2d7da1ea11c` |
| SHA256SUMS.txt | 569 | `cfd2b7fae3acee35407b2c70aa7c82712e44606fa1dca0e74626ee7941db439a` |

All GitHub asset digests agree with the local prepared assets. The release helper independently downloaded the public .mpp, matched its SHA-256, then advanced the beta feed. CI's independent rebuilt artifacts are not substituted for these exact locally verified bytes. Previous 0.5.1 assets/tags are unchanged.

Verification: original TikTok 47.4.3 / 2024704030 and HushFeed 0.76.0; 83 unit/simulated tests, seven helper tests, three API lint tasks, four compiled combinations (both + all 135 HushFeed patches in both source orders; each feature with Settings alone), two negative cases and three nine-role native audits. See [compatibility record](hushfeed-076-tiktok-4743-verification.md). Device acceptance is still pending. No APK signing, installation, data clearing or vendor/prepatched APK distribution occurred.

The clean CI run emitted non-failing maintenance notices for pinned Node 20 actions, setup-java v4 and the ubuntu-latest image transition. The checks succeeded; this task did not silently upgrade unrelated build infrastructure.

Debug scratch and logs remain ignored under build/verification and build/4743-* as requested. The small private publication record was added after the immutable source/tag snapshot, so it is not inside that already-published archive. Do not replace assets to add it.
