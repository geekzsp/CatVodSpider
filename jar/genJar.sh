#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
cd ..
ROOT_DIR="$(pwd)"
echo  $SCRIPT_DIR
echo  $ROOT_DIR
# 删除文件和目录
rm -f "$SCRIPT_DIR/custom_spider.jar"
rm -rf "$SCRIPT_DIR/Smali_classes"

# 反编译 dex 文件到 smali
java -jar "$SCRIPT_DIR/3rd/baksmali-2.5.2.jar" d "$ROOT_DIR/app/build/intermediates/dex/release/minifyReleaseWithR8/classes.dex" -o "$SCRIPT_DIR/Smali_classes"

# 删除旧的 smali 目录
rm -rf "$SCRIPT_DIR/spider.jar/smali/com/github/catvod/spider"
rm -rf "$SCRIPT_DIR/spider.jar/smali/com/github/catvod/parser"
rm -rf "$SCRIPT_DIR/spider.jar/smali/com/github/catvod/js"

# 创建目录
mkdir -p "$SCRIPT_DIR/spider.jar/smali/com/github/catvod/"

# 移动 smali 文件
mv "$SCRIPT_DIR/Smali_classes/com/github/catvod/spider" "$SCRIPT_DIR/spider.jar/smali/com/github/catvod/"
mv "$SCRIPT_DIR/Smali_classes/com/github/catvod/parser" "$SCRIPT_DIR/spider.jar/smali/com/github/catvod/"
mv "$SCRIPT_DIR/Smali_classes/com/github/catvod/js" "$SCRIPT_DIR/spider.jar/smali/com/github/catvod/"

# 重新编译
java -jar "$SCRIPT_DIR/3rd/apktool_2.4.1.jar" b "$SCRIPT_DIR/spider.jar" -c

# 移动输出文件并生成 MD5
mv "$SCRIPT_DIR/spider.jar/dist/dex.jar" "$SCRIPT_DIR/custom_spider.jar"

md5 -q "$SCRIPT_DIR/custom_spider.jar" > "$SCRIPT_DIR/custom_spider.jar.md5"

# 删除临时目录
rm -rf "$SCRIPT_DIR/spider.jar/build"
rm -rf "$SCRIPT_DIR/spider.jar/smali"
rm -rf "$SCRIPT_DIR/spider.jar/dist"
rm -rf "$SCRIPT_DIR/Smali_classes"