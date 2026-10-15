import importlib.util
import json
from pathlib import Path
import sys
import tempfile
import unittest
import zipfile

SCRIPTS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(SCRIPTS))
import export_public
import release

class ReleaseTests(unittest.TestCase):
    def test_semver_channels_and_numeric_prereleases(self):
        self.assertLess(release.version_key("0.5.1-beta.2"), release.version_key("0.5.1-beta.10"))
        self.assertLess(release.version_key("0.5.1-beta.10"), release.version_key("0.5.1"))
        self.assertLess(release.version_key("0.5.1"), release.version_key("0.5.2-beta.1"))

    def test_rejects_invalid_versions_repository_and_timestamp(self):
        for version in ("../evil", "0.5", "0.5.1-beta.01", "01.5.1"):
            with self.assertRaises(ValueError):
                release.version_key(version)
        with self.assertRaises(ValueError):
            release.manifest("owner/repo/evil", "0.5.1")
        with self.assertRaises(ValueError):
            release.manifest("owner/repo", "0.5.1", "not a date")

    def test_manifest_matches_morphe_required_schema_and_immutable_url(self):
        data = release.manifest("BDCrusher/tiktok-interaction-addons", "0.5.1-beta.1", "2026-10-15T12:00:00")
        self.assertEqual(set(data), {"version", "download_url", "created_at", "description", "signature_download_url"})
        self.assertIn("/releases/download/v0.5.1-beta.1/TikTok-Interaction-Addons-0.5.1-beta.1.mpp", data["download_url"])
        self.assertEqual("", data["signature_download_url"])

    def test_export_redacts_personal_home_and_excludes_private_inputs(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary) / "private"
            root.mkdir()
            personal = "C:" + chr(92) + "Users" + chr(92) + "sample-user"
            (root / "README.md").write_text("SDK at " + personal + chr(92) + "Sdk", encoding="utf-8")
            (root / "verification" / "inputs").mkdir(parents=True)
            (root / "verification" / "inputs" / "original.apk").write_bytes(b"private vendor input")
            (root / "build").mkdir()
            (root / "build" / "debug.txt").write_text("private debug")
            output = Path(temporary) / "public"
            self.assertEqual(1, export_public.export(root, output))
            self.assertNotIn("sample-user", (output / "README.md").read_text())
            self.assertFalse((output / "verification").exists())
            self.assertFalse((output / "build").exists())
            with self.assertRaises(ValueError):
                export_public.export(root, output)

    def test_export_refuses_credentials(self):
        with self.assertRaises(ValueError):
            export_public.sanitize("ghp_" + "a" * 36)
        with self.assertRaises(ValueError):
            export_public.sanitize("-----BEGIN " + "PRIVATE KEY-----")

    def test_source_audit_rejects_vendor_and_cache_entries(self):
        with tempfile.TemporaryDirectory() as temporary:
            for name in ("original.apk", "build/debug.txt", "../escape.txt", ".git/config"):
                archive = Path(temporary) / "source.zip"
                with zipfile.ZipFile(archive, "w") as zip_file:
                    zip_file.writestr(name, "not public source")
                with self.assertRaises(ValueError):
                    release.audit_source(archive)

    def test_prepare_checksums_and_remote_metadata(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            (root / "gradle.properties").write_text("version=0.5.1-beta.1\n")
            dist = root / "dist"
            dist.mkdir()
            bundle = dist / "TikTok-Interaction-Addons-0.5.1-beta.1.mpp"
            with zipfile.ZipFile(bundle, "w") as archive:
                for name in ("classes.dex", "extensions/collections.mpe", "extensions/settings.mpe", "extensions/sound.mpe"):
                    archive.writestr(name, b"fixture")
            source = dist / "TikTok-Interaction-Addons-0.5.1-beta.1-source.zip"
            with zipfile.ZipFile(source, "w") as archive:
                archive.writestr("README.md", "Public source")
            (dist / "patches-list.json").write_text("{}")
            (dist / "payload-audit.json").write_text("{}")
            self.assertEqual("0.5.1-beta.1", release.prepare(root, dist, "BDCrusher/tiktok-interaction-addons"))
            feed = json.loads((dist / "patches-bundle.json").read_text())
            self.assertIn("download_url", feed)
            for line in (dist / "SHA256SUMS.txt").read_text().splitlines():
                digest, name = line.split("  ", 1)
                self.assertEqual(digest, release.sha256(dist / name))

if __name__ == "__main__":
    unittest.main()
