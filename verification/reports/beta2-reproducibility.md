# Beta.2 repeat-build check

Date: 2026-10-08. Ordinary build and forced all-task build produced the same 72,041-byte bundle:

`b3ff19eb546a2140dfa8b0b2dc7a44da66c9a3e3ae6635478a450b3e4cb1e697`

Command: `gradlew.bat :patches:generateMetadata --rerun-tasks --console=plain`. All 38 build tasks executed. The shell compared the SHA-256 before and after and would fail on a mismatch. This repeats build inputs on the same Windows workstation and pinned toolchain; it does not establish cross-platform reproducibility.

All six current beta.2 combination reports identify this exact add-on hash. Both HushFeed 0.68.0 expanded source orders, its 41-patch default selection, the 0.67.0 core selection, missing-settings refusal and duplicate-selection refusal passed their respective checks. Full source and bundle checksums are generated after source packaging.

Payload audit: 27 runtime classes, entirely `app.collectionsaddon.*`; no HushFeed implementation or integration stubs injected. Exactly one selectable patch.
