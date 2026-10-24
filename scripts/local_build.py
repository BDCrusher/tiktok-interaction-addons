"""Run the project wrapper with writable homes/caches confined to the TikTok checkout."""
from pathlib import Path
import os
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
STATE = ROOT / "build" / "local-build"

def main():
    for folder in (ROOT, ROOT / "build", STATE):
        if folder.exists() and (folder.is_symlink() or getattr(folder.lstat(), "st_file_attributes", 0) & 0x400):
            raise SystemExit(f"Refusing linked build root: {folder}")
    for name in ("home", "temp", "gradle-home", "android-home"):
        folder = STATE / name
        if not folder.resolve().is_relative_to(ROOT) or (folder.exists() and
                (folder.is_symlink() or getattr(folder.lstat(), "st_file_attributes", 0) & 0x400)):
            raise SystemExit(f"Build state escapes project: {folder}")
        folder.mkdir(parents=True, exist_ok=True)
    env = os.environ.copy()
    env.update(GRADLE_USER_HOME=str(STATE / "gradle-home"),
               ANDROID_USER_HOME=str(STATE / "android-home"),
               TEMP=str(STATE / "temp"), TMP=str(STATE / "temp"))
    env.pop("JDK_JAVA_OPTIONS", None)
    env.pop("ANDROID_PREFS_ROOT", None)
    env.pop("ANDROID_SDK_HOME", None)
    env["JAVA_TOOL_OPTIONS"] = f'-Duser.home="{STATE / "home"}" -Djava.io.tmpdir="{STATE / "temp"}" -XX:-UsePerfData'
    java_home = env.get("JAVA_HOME", "C:/Program Files/Microsoft/jdk-17.0.20.101-hotspot")
    env["JAVA_HOME"] = java_home
    env.setdefault("ANDROID_HOME", "USER_HOME/AppData/Local/Android/Sdk")
    command = [str(Path(java_home) / "bin/java.exe"), "-cp", str(ROOT / "gradle/wrapper/gradle-wrapper.jar"),
               "org.gradle.wrapper.GradleWrapperMain", "--no-daemon", "--no-watch-fs", "--console=plain",
               "-Dorg.gradle.java.installations.auto-download=false", *sys.argv[1:]]
    return subprocess.call(command, cwd=ROOT, env=env)

if __name__ == "__main__":
    raise SystemExit(main())
