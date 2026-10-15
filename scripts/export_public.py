"""Export an allowlisted public source snapshot; never copy APKs, caches or Git history."""
import argparse
import os
from pathlib import Path
import re
import shutil

FILES = {".gitignore", ".gitattributes", "LICENSE", "NOTICE", "README.md", "BUILDING.md",
         "ARCHITECTURE.md", "COMPATIBILITY.md", "SOUND_BUTTON.md", "PUBLISHING.md",
         "CHANGELOG.md", "patches-bundle.json", "gradle.properties", "settings.gradle.kts",
         "gradlew", "gradlew.bat"}
TREES = {"patches", "extensions", "integration-stubs", "gradle", "verification", "scripts", ".github"}
SKIP = {"build", "build-tools", "dist", "inputs", ".git", ".gradle", ".kotlin", "__pycache__"}
TEXT = {".md", ".java", ".kt", ".kts", ".json", ".yml", ".yaml", ".properties", ".toml", ".xml", ".py", ".sh", ".bat"}
PRIVATE_HOME = re.compile(r"(?i)[a-z]:[\\/]+Users[\\/]+[^\\/\s\"'<>]+")
SECRET = re.compile(r"gh[pousr]_[A-Za-z0-9]{20,}|github_pat_[A-Za-z0-9_]{20,}|-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY")

def sanitize(text):
    if SECRET.search(text):
        raise ValueError("Possible private credential: refusing public export")
    return PRIVATE_HOME.sub("USER_HOME", text)

def export(source, destination):
    source, destination = Path(source).resolve(), Path(destination).resolve()
    if destination.exists():
        raise ValueError("Use a fresh destination; existing files are never overwritten")
    selected = []
    paths = []
    for directory, folders, files in os.walk(source, followlinks=False):
        parent = Path(directory)
        folders[:] = [name for name in folders if name not in SKIP and (parent != source or name in TREES)]
        for name in folders:
            folder = parent / name
            if folder.is_symlink() or getattr(folder.lstat(), "st_file_attributes", 0) & 0x400:
                raise ValueError("Links/junctions are not allowed in public source")
        paths.extend(parent / name for name in files)
    for path in paths:
        relative = path.relative_to(source)
        if any(part in SKIP for part in relative.parts) or path.is_dir():
            continue
        if path.is_symlink() or not path.resolve().is_relative_to(source):
            raise ValueError("Links are not allowed in public source")
        if relative.as_posix() not in FILES and relative.parts[0] not in TREES:
            continue
        binary = relative.as_posix() == "gradle/wrapper/gradle-wrapper.jar"
        if not binary and path.suffix not in TEXT and path.name not in FILES:
            raise ValueError(f"Unreviewed source type: {relative}")
        content = path.read_bytes() if binary else sanitize(path.read_text(encoding="utf-8-sig")).replace("\r\n", "\n").encode()
        selected.append((relative, content))
    destination.mkdir(parents=True)
    for relative, content in selected:
        output = destination / relative
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_bytes(content)
    return len(selected)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path)
    parser.add_argument("destination", type=Path)
    arguments = parser.parse_args()
    print(f"Exported {export(arguments.source, arguments.destination)} allowlisted files; no Git history/vendor inputs")
