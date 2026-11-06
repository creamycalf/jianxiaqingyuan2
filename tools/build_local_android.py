"""Build the local APK without storing signing passwords in source or command lines."""
import os
import pathlib
import secrets
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
JAVA = pathlib.Path('C:/Users/lxx/.codex/tools/rea/jdk-21.0.13+9')
SDK = pathlib.Path('F:/aiworkspace/codexworkspace/worktools_image_suite/worktools_image_suite/mobile_android/.android-sdk')
PRIVATE = ROOT / '.local-signing'
PRIVATE.mkdir(exist_ok=True)
password_path = PRIVATE / 'password.txt'
keystore = PRIVATE / 'jxqy2-local.p12'
if not password_path.exists():
    if keystore.exists():
        raise RuntimeError('Signing key exists without its password; refusing replacement')
    with password_path.open('x', encoding='ascii') as stream:
        stream.write(secrets.token_urlsafe(36))
env = os.environ.copy()
env.update(JAVA_HOME=str(JAVA), ANDROID_HOME=str(SDK), ANDROID_SDK_ROOT=str(SDK))
env['JXQY_ANDROID_KEYSTORE'] = str(keystore)
env['JXQY_ANDROID_KEY_ALIAS'] = 'jxqy2-local'
env['JXQY_ANDROID_STORE_PASSWORD'] = password_path.read_text(encoding='ascii').strip()
env['JXQY_ANDROID_KEY_PASSWORD'] = env['JXQY_ANDROID_STORE_PASSWORD']
if not keystore.exists():
    subprocess.run([str(JAVA / 'bin/keytool.exe'), '-genkeypair', '-keystore', str(keystore), '-storetype', 'PKCS12', '-storepass:env', 'JXQY_ANDROID_STORE_PASSWORD', '-keypass:env', 'JXQY_ANDROID_KEY_PASSWORD', '-alias', 'jxqy2-local', '-keyalg', 'RSA', '-keysize', '3072', '-validity', '10000', '-dname', 'CN=JXQY2 Local Build'], env=env, check=True)
command = [str(ROOT / 'upstream/android/gradlew.bat'), '-p', str(ROOT / 'upstream/android'), '-I', str(ROOT / 'tools/local-build.init.gradle'), '-PjxqyAssetsDir=' + str(ROOT / 'build-local/assets'), ':app:assembleFullyRelease', '--console=plain', '--max-workers=4']
subprocess.run(command, env=env, check=True)
