# Local repeat-build evidence — 2026-10-03

The normal final build and a forced `:patches:generateMetadata --rerun-tasks` rebuild produced the same `.mpp` SHA-256:

`848900085651c1b44248b686cebb7a731d72d49792a74321132c4c8a6a232003`

The forced build executed all 64 applicable tasks (including the runtime tests) rather than reusing the existing archive. Both builds used the pinned tool commits and same JDK/SDK on this Windows workstation. This demonstrates local repeatability, not byte-for-byte reproducibility across untested operating systems/toolchains. See BUILDING.md for dependency/tool pins and invocation.
