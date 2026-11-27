#!/bin/bash
# Builds the two QA fixture APKs (crash and ok) without Gradle. Output: out/qa-<variant>view.apk
set -euo pipefail
cd "$(dirname "$0")"
SDK=${ANDROID_HOME:-$HOME/Library/Android/sdk}
BT=$SDK/build-tools/36.0.0
JAR=$SDK/platforms/android-36/android.jar
rm -rf out && mkdir -p out
for v in crash ok; do
  w=out/$v; mkdir -p $w/classes $w/dex
  javac -nowarn -Xlint:-options -source 8 -target 8 -bootclasspath "$JAR" -d $w/classes \
    src/com/migrely/qa/fixture/ViewActivity.java gen-$v/com/migrely/qa/fixture/Variant.java
  "$BT/d8" --min-api 26 --lib "$JAR" --output $w/dex $(find $w/classes -name '*.class')
  "$BT/aapt2" link -I "$JAR" --manifest AndroidManifest-$v.xml -o $w/base.apk
  (cd $w/dex && zip -q ../base.apk classes.dex)
  "$BT/zipalign" -f 4 $w/base.apk $w/aligned.apk
  "$BT/apksigner" sign --ks "$HOME/.android/debug.keystore" --ks-pass pass:android --key-pass pass:android \
    --out out/qa-${v}view.apk $w/aligned.apk
done
ls -l out/*.apk
