#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"

"./gradlew" assembleRelease --no-daemon

"$SCRIPT_DIR/jar/genJar.sh" "$1"

read -p "Press any key to continue..."


