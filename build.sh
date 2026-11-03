#!/bin/bash
# Build Muse Voice Mode Shortcut APK from source.
# Toolchain: meta-muse-widget tools/sdk (build-tools 35.0.0, platform
# android-35), used READ-ONLY for compilation, + Temurin JDK 21.
# Framework-only app (no AndroidX): javac/d8/aapt2 raw flow, same as
# Muse for Android Auto. No emulator is touched by this build.
set -euo pipefail
cd "$(dirname "$0")"

SDK="${ANDROID_SDK_ROOT:-$HOME/workspace/meta-muse-widget/tools/sdk}"
export JAVA_HOME="$HOME/workspace/custom-skills/local-rea/tools/jdk"
export PATH="$JAVA_HOME/bin:$PATH"
BT="$SDK/build-tools/35.0.0"
ANDROID_JAR="$SDK/platforms/android-35/android.jar"
KS="keystore/musevoice.keystore"
KS_PASS_FILE="keystore/keystore-password.txt"
VERSION_CODE=9
VERSION_NAME="1.7.0"

rm -rf build
mkdir -p build/gen build/classes build/dex keystore

if [ ! -f "$KS" ]; then
  PASS="$(openssl rand -base64 24 | tr -d '/+=' | head -c 24)"
  echo "$PASS" > "$KS_PASS_FILE"
  chmod 600 "$KS_PASS_FILE"
  keytool -genkeypair -keystore "$KS" -alias musevoice -keyalg RSA -keysize 2048 \
    -validity 10000 -storepass "$PASS" -keypass "$PASS" \
    -dname "CN=Muse Voice Mode Shortcut, O=JonathanCruzate, C=US" 2>/dev/null
  echo "keystore created"
fi
PASS="$(cat "$KS_PASS_FILE")"

echo "== aapt2 compile =="
find res -type f | sort > build/res-files.txt
"$BT/aapt2" compile --dir res -o build/res.zip

echo "== aapt2 link =="
"$BT/aapt2" link -o build/unsigned.apk \
  -I "$ANDROID_JAR" \
  --manifest AndroidManifest.xml \
  --java build/gen \
  --min-sdk-version 29 --target-sdk-version 35 \
  --version-code "$VERSION_CODE" --version-name "$VERSION_NAME" \
  build/res.zip

echo "== javac =="
find src build/gen -name "*.java" > build/sources.txt
javac -source 8 -target 8 -bootclasspath "$ANDROID_JAR" -d build/classes @build/sources.txt

echo "== d8 =="
"$BT/d8" --release --min-api 29 --lib "$ANDROID_JAR" --output build/dex $(find build/classes -name "*.class")

echo "== package dex =="
(cd build/dex && zip -q ../unsigned.apk classes.dex)

echo "== zipalign + sign =="
"$BT/zipalign" -f 4 build/unsigned.apk build/aligned.apk
"$BT/apksigner" sign --ks "$KS" --ks-pass "pass:$PASS" --key-pass "pass:$PASS" \
  --out "Muse-Voice-Mode-Shortcut-$VERSION_NAME.apk" build/aligned.apk
"$BT/apksigner" verify --print-certs "Muse-Voice-Mode-Shortcut-$VERSION_NAME.apk" | head -3
ls -la "Muse-Voice-Mode-Shortcut-$VERSION_NAME.apk"
