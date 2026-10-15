"""Prepare audited Morphe release assets; publish only when explicitly invoked with publish."""
import argparse
import base64
from datetime import datetime, timezone
import hashlib
import json
from pathlib import Path
import re
import shutil
import subprocess
import tempfile
import urllib.request
import zipfile

VERSION = re.compile(r"(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)(?:-[0-9A-Za-z]+(?:\.[0-9A-Za-z]+)*)?")
REPO = re.compile(r"[A-Za-z0-9][A-Za-z0-9-]*/[A-Za-z0-9_.-]+")

def version_key(value):
    if not VERSION.fullmatch(value):
        raise ValueError("Invalid release version")
    core, separator, pre = value.partition("-")
    parts = []
    if separator:
        for item in pre.split("."):
            if item.isdigit() and len(item) > 1 and item.startswith("0"):
                raise ValueError("Numeric prerelease identifiers cannot have leading zeroes")
            parts.append((0, int(item)) if item.isdigit() else (1, item))
    return tuple(map(int, core.split("."))), not bool(separator), tuple(parts)

def sha256(path):
    digest = hashlib.sha256()
    with Path(path).open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()

def source_version(root):
    match = re.search(r"^version=(.+)$", (root / "gradle.properties").read_text(), re.M)
    if not match:
        raise ValueError("Missing source version")
    value = match[1].strip()
    version_key(value)
    return value

def manifest(repository, version, timestamp=None):
    if not REPO.fullmatch(repository):
        raise ValueError("Expected owner/repository")
    version_key(version)
    timestamp = timestamp or datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S")
    datetime.strptime(timestamp, "%Y-%m-%dT%H:%M:%S")
    return {
        "version": version,
        "download_url": f"https://github.com/{repository}/releases/download/v{version}/TikTok-Interaction-Addons-{version}.mpp",
        "created_at": timestamp,
        "description": "Independent beta collection-popup and sound-button add-ons. Requires original TikTok 47.1.4 and HushFeed 0.74.0 Settings. Two separately selectable patches. Device acceptance pending; see the compatibility report. Repatch TikTok to apply updates.",
        "signature_download_url": "",
    }

def audit_source(path):
    with zipfile.ZipFile(path) as archive:
        names = archive.namelist()
        if len(names) != len(set(names)):
            raise ValueError("Duplicate source archive paths")
        for info in archive.infolist():
            name = info.filename.replace("\\", "/")
            parts = Path(name).parts
            if name.startswith("/") or ".." in parts or any(p in {".git", "build", "build-tools", "inputs", ".gradle", "dist", "__pycache__"} for p in parts):
                raise ValueError(f"Private/generated source archive entry: {name}")
            if Path(name).suffix.lower() in {".apk", ".apkm", ".aab", ".keystore", ".jks", ".mpp", ".mpe", ".dex"}:
                raise ValueError(f"Vendor/payload input in source archive: {name}")
            if info.is_dir() or name.endswith("gradle-wrapper.jar"):
                continue
            content = archive.read(info).decode("utf-8-sig")
            from export_public import PRIVATE_HOME, SECRET
            if PRIVATE_HOME.search(content) or SECRET.search(content):
                raise ValueError(f"Private data in source archive: {name}")

def prepare(root, output, repository, bundle=None, source_archive=None):
    root, output = Path(root).resolve(), Path(output).resolve()
    version = source_version(root)
    output.mkdir(parents=True, exist_ok=True)
    filename = f"TikTok-Interaction-Addons-{version}"
    source_bundle = Path(bundle) if bundle else root / "dist" / f"{filename}.mpp"
    with zipfile.ZipFile(source_bundle) as archive:
        for entry in ("classes.dex", "extensions/collections.mpe", "extensions/settings.mpe", "extensions/sound.mpe"):
            if entry not in archive.namelist():
                raise ValueError(f"Missing patch bundle entry: {entry}")
    target = output / f"{filename}.mpp"
    if source_bundle.resolve() != target:
        shutil.copyfile(source_bundle, target)
    assets = [target]
    archive = Path(source_archive) if source_archive else root / "dist" / f"{filename}-source.zip"
    audit_source(archive)
    source_target = output / f"{filename}-source.zip"
    if archive.resolve() != source_target:
        shutil.copyfile(archive, source_target)
    assets.append(source_target)
    for name in ("patches-list.json", "payload-audit.json"):
        source = root / "dist" / name
        if source.exists():
            target = output / name
            if source.resolve() != target:
                shutil.copyfile(source, target)
            assets.append(target)
    feed = manifest(repository, version)
    metadata = output / "patches-bundle.json"
    metadata.write_text(json.dumps(feed, indent=2) + "\n", encoding="utf-8")
    assets.append(metadata)
    notes = output / "RELEASE-NOTES.md"
    notes.write_text(f"# TikTok Interaction Add-ons {version}\n\n"
        "Independent beta source; HushFeed is external and unchanged.\n\n"
        "Target: original TikTok 47.1.4 / 2024701040 with HushFeed 0.74.0 Settings. "
        "Enhanced collection popup and Sound button actions remain independently selectable. "
        "This release updates the five-argument settings contract and flat home placement. "
        "Existing feature preferences are retained.\n\n"
        "Enable Pre-release patches and Experimental app versions on this source in Morphe. "
        "Replace any older local source rather than selecting two copies. Repatch the original APK "
        "alongside HushFeed Settings; keep your signing key.\n\n"
        "CI verifies compilation, unit tests, API lint and payload isolation, not a phone or all HushFeed combinations. "
        "Consult COMPATIBILITY.md for exact local combined-build evidence. Device acceptance remains pending. "
        "No TikTok/HushFeed APKs, credentials or debug scratch are distributed.\n", encoding="utf-8")
    assets.append(notes)
    (output / "SHA256SUMS.txt").write_text("".join(f"{sha256(asset)}  {asset.name}\n" for asset in sorted(assets)), encoding="utf-8")
    return version

def gh(*arguments, data=None):
    result = subprocess.run(["gh", *arguments], input=json.dumps(data) if data is not None else None,
        text=True, capture_output=True, check=True)
    return json.loads(result.stdout) if result.stdout.strip() else None

def publish(root, output, repository, commit):
    if not REPO.fullmatch(repository) or not re.fullmatch(r"[0-9a-f]{40}", commit):
        raise ValueError("Expected repository and exact source commit")
    output = Path(output).resolve()
    version = source_version(Path(root))
    feed = json.loads((output / "patches-bundle.json").read_text())
    if feed["version"] != version or feed["download_url"] != manifest(repository, version)["download_url"]:
        raise ValueError("Metadata does not match repository/source version")
    names = []
    for line in (output / "SHA256SUMS.txt").read_text().splitlines():
        digest, name = line.split("  ", 1)
        if Path(name).name != name or sha256(output / name) != digest:
            raise ValueError("Invalid release checksum/path")
        names.append(name)
    source = output / f"TikTok-Interaction-Addons-{version}-source.zip"
    audit_source(source)
    channel = "dev" if "-" in version else "main"
    current = gh("api", f"repos/{repository}/contents/patches-bundle.json?ref={channel}")
    old = json.loads(base64.b64decode(current["content"]))
    if version_key(old["version"]) > version_key(version):
        raise ValueError("Refusing a source feed downgrade")
    arguments = ["release", "create", f"v{version}", "--repo", repository, "--target", commit,
                 "--title", f"TikTok Interaction Add-ons {version}", "--notes-file", str(output / "RELEASE-NOTES.md")]
    if "-" in version:
        arguments.append("--prerelease")
    subprocess.run(["gh", *arguments, *(str(output / name) for name in names), str(output / "SHA256SUMS.txt")], check=True)
    # Advertise only after the binary is actually public and verified.
    bundle = output / f"TikTok-Interaction-Addons-{version}.mpp"
    with urllib.request.urlopen(feed["download_url"], timeout=60) as response:
        remote = hashlib.sha256()
        for chunk in iter(lambda: response.read(65536), b""):
            remote.update(chunk)
    if remote.hexdigest() != sha256(bundle):
        raise ValueError("Published bundle hash mismatch; source feed was not advanced")
    if old["version"] == version and old["download_url"] == feed["download_url"]:
        print("Initial feed already points at this verified release")
        return
    gh("api", "--method", "PUT", f"repos/{repository}/contents/patches-bundle.json", "--input", "-", data={
        "message": f"Publish source metadata for {version}", "branch": channel, "sha": current["sha"],
        "content": base64.b64encode((json.dumps(feed, indent=2) + "\n").encode()).decode(),
    })
    print(f"Published and verified {version}; updated {channel} source metadata")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command", choices=("prepare", "publish"))
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--dist", type=Path, required=True)
    parser.add_argument("--repository", required=True)
    parser.add_argument("--bundle", type=Path)
    parser.add_argument("--source-archive", type=Path)
    parser.add_argument("--commit")
    args = parser.parse_args()
    if args.command == "prepare":
        print(f"Prepared release {prepare(args.root, args.dist, args.repository, args.bundle, args.source_archive)}")
    else:
        publish(args.root, args.dist, args.repository, args.commit or "")
