"""Run the same compiled JUnit suites directly; avoids Gradle's Windows apostrophe argfile bug."""
from pathlib import Path
import json
import os
import subprocess
import sys
from local_build import ROOT, STATE

MODULES = (('patches', 'test'), ('extensions/collections', 'testDebugUnitTest'),
           ('extensions/sound', 'testDebugUnitTest'), ('extensions/settings', 'testDebugUnitTest'))

def main():
    tasks = [':' + module.replace('/', ':') + ':' + task + 'Runtime' for module, task in MODULES]
    subprocess.run([sys.executable, '-B', str(ROOT / 'scripts/local_build.py'), '-I',
                    str(ROOT / 'scripts/local_test_runtime.init.gradle'), *tasks], check=True)
    env = os.environ.copy()
    env.update(TEMP=str(STATE / 'temp'), TMP=str(STATE / 'temp'))
    env.pop('JAVA_TOOL_OPTIONS', None)
    env.pop('JDK_JAVA_OPTIONS', None)
    java = Path(env.get('JAVA_HOME', 'C:/Program Files/Microsoft/jdk-17.0.20.101-hotspot')) / 'bin/java.exe'
    results = []
    for module, task in MODULES:
        config = json.loads((ROOT / module / 'build' / (task + '-runtime.json')).read_text())
        classes = sorted({p.relative_to(folder).as_posix()[:-6].replace('/', '.')
                          for folder in map(Path, config['classes']) for p in folder.rglob('*Test.class')
                          if '$' not in p.name})
        if not classes:
            raise RuntimeError('No test classes for ' + module)
        command = [str(java), '-XX:-UsePerfData', '-Duser.home=' + str(STATE / 'home'),
                   '-Djava.io.tmpdir=' + str(STATE / 'temp'), '-Xmx1g',
                   *['-D' + k + '=' + str(v) for k, v in config['systemProperties'].items()],
                   *config['jvmArgs'], '-cp', config['classpath'], 'org.junit.runner.JUnitCore', *classes]
        output = ROOT / module / 'build' / 'local-junit.log'
        with output.open('w', encoding='utf-8') as log:
            run = subprocess.run(command, cwd=config['cwd'], env=env, stdout=log, stderr=subprocess.STDOUT)
        print(module + ': ' + output.read_text(encoding='utf-8')[-700:], flush=True)
        results.append(dict(module=module, exitCode=run.returncode, command=command, cwd=config['cwd']))
    helper_command = [sys.executable, '-B', '-m', 'unittest', 'discover', '-s', 'scripts/tests', '-v']
    helper_log = STATE / 'release-helper-tests.log'
    with helper_log.open('w', encoding='utf-8') as log:
        helper_run = subprocess.run(helper_command, cwd=ROOT, env=env, stdout=log, stderr=subprocess.STDOUT)
    print(helper_log.read_text(encoding='utf-8')[-700:], flush=True)
    results.append(dict(module='release-helpers', exitCode=helper_run.returncode, command=helper_command, cwd=str(ROOT)))
    (STATE / 'junit-results.json').write_text(json.dumps(results, indent=2), encoding='utf-8')
    return int(any(result['exitCode'] for result in results))

if __name__ == '__main__':
    raise SystemExit(main())
