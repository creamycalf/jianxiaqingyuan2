"""Verify the integrated APK without installing or executing it."""
import argparse
import hashlib
import pathlib
import zipfile

p = argparse.ArgumentParser()
p.add_argument('apk', type=pathlib.Path)
a = p.parse_args()
with zipfile.ZipFile(a.apk) as z:
    names = z.namelist()
    assert len(names) == len(set(names)), 'Duplicate ZIP members'
    roots = {n.split('/')[1] for n in names if n.startswith('assets/')}
    assert roots == {'engine', 'resources.ini', 'common', 'jxqy2'}, roots
    for required in ['assets/jxqy2/game_profile.ini', 'assets/common/version.ini', 'assets/engine/guide/guide.txt', 'lib/arm64-v8a/libmain.so', 'lib/x86_64/libmain.so']:
        assert required in names, required
    assert b'Id=JXQY2' in z.read('assets/jxqy2/game_profile.ini')
    guide = z.read('assets/engine/guide/guide.txt').decode('utf-8-sig')
    assert len(guide) > 1000
    assert '唐门' in guide and '作弊' in guide
    bad = [n for n in names if n.startswith('assets/') and (n.endswith(('.zip', '.p12', '.jks')) or '/save/system/' in n or '/save/rpg1/' in n)]
    assert not bad, bad
    assert not z.testzip(), 'ZIP CRC failure'
    print('Asset roots:', sorted(roots))
    print('JXQY2 files:', sum(n.startswith('assets/jxqy2/') for n in names))
    print('Guide characters:', len(guide))
with a.apk.open('rb') as stream:
    digest = hashlib.file_digest(stream, 'sha256').hexdigest()
print('APK bytes:', a.apk.stat().st_size)
print('SHA-256:', digest)
print('Integrated APK content verification passed')
