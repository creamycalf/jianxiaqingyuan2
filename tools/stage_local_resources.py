"""Stage verified upstream resources for a local-only bundled Android build."""
import hashlib
import pathlib
import shutil
import stat
import zipfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
DEST = ROOT / 'build-local' / 'assets'
PACKS = {
    'common': '7ea0dc22bf7ed95eea234dbbc02eecfc524de9ce085e7d4a7bf20226f364c9a1',
    'jxqy2': 'd1f2821a2b71560c1569ef60b6712ce1b4bab3aaa5ecbd8cc16aa7bbef7abfc0',
}

def extract(archive, target):
    if target.exists():
        raise RuntimeError(f'Refusing existing extraction target: {target}')
    with zipfile.ZipFile(archive) as z:
        for entry in z.infolist():
            path = pathlib.PurePosixPath(entry.filename)
            if path.is_absolute() or '..' in path.parts or '\\' in entry.filename or ':' in entry.filename or stat.S_ISLNK(entry.external_attr >> 16):
                raise ValueError(f'Unsafe archive entry: {entry.filename}')
        target.mkdir(parents=True)
        for entry in z.infolist():
            path = pathlib.PurePosixPath(entry.filename)
            if any(part in {'.git', '.gitignore', '.gitattributes'} for part in path.parts):
                continue
            out = target.joinpath(*path.parts)
            if entry.is_dir():
                out.mkdir(parents=True, exist_ok=True)
            else:
                out.parent.mkdir(parents=True, exist_ok=True)
                with z.open(entry) as source, out.open('xb') as destination:
                    shutil.copyfileobj(source, destination)

if __name__ == '__main__':
    if DEST.exists():
        raise RuntimeError(f'Staging already exists: {DEST}')
    for name, expected in PACKS.items():
        archive = ROOT / 'delivery' / 'resources' / (name + '.zip')
        with archive.open('rb') as stream:
            actual = hashlib.file_digest(stream, 'sha256').hexdigest()
        if actual != expected:
            raise ValueError(f'Checksum mismatch: {name}')
    DEST.mkdir(parents=True)
    shutil.copytree(ROOT / 'upstream/assets/engine', DEST / 'engine')
    for name in PACKS:
        extract(ROOT / 'delivery/resources' / (name + '.zip'), DEST / name)
    patch = ROOT / 'downloads/jxqy2-update-20261106/jxqy2-cumulative-20261023.zip'
    if hashlib.sha256(patch.read_bytes()).hexdigest() != '5c485af7138941b9fd198be5460df69c6e64aa26f26ac95bcb082d8439614bf2':
        raise ValueError('Cumulative patch checksum mismatch')
    patch_root = ROOT / 'build-local/jxqy2-cumulative'
    extract(patch, patch_root)
    for file in patch_root.rglob('*'):
        if file.is_file():
            destination = DEST / 'jxqy2' / file.relative_to(patch_root)
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(file, destination)
    guide = DEST / 'engine/guide'
    guide.mkdir()
    shutil.copy2(ROOT / 'docs/guide-content.md', guide / 'guide.txt')
    import runpy
    labels = runpy.run_path(str(ROOT / 'tools/configure_mobile_skill_labels.py'))
    for filename, label in labels['LABELS'].items():
        labels['update_ini'](DEST / 'common/ini/ui/mobile/skills' / filename, label)
    # Local build must not install upstream-signed updates over its own identity.
    (DEST / 'resources.ini').write_text('[Collection]\nCommonPath=common\n', encoding='utf-8')
    print(f'Staged resources at {DEST}')
