#!/bin/bash
# 
# Coreのapk作成、コピー
# 

# apk作成（署名なし）
./gradlew floating-window-core:assemble

# apkコピー
cp ./floating-window-core/build/outputs/apk/debug/floating-window-core-debug.apk ./download
cp ./floating-window-core/build/outputs/apk/release/floating-window-core-release-unsigned.apk ./download
