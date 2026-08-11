#!/bin/sh
set -eu
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION=8.13
CACHE_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/circle-day-planner-dists/gradle-$GRADLE_VERSION"
ZIP="$CACHE_DIR/gradle-$GRADLE_VERSION-bin.zip"
BIN="$CACHE_DIR/gradle-$GRADLE_VERSION/bin/gradle"
if [ ! -x "$BIN" ]; then
  mkdir -p "$CACHE_DIR"
  if command -v curl >/dev/null 2>&1; then
    curl -L --fail --retry 3 "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ZIP"
  else
    wget -O "$ZIP" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  fi
  unzip -q -o "$ZIP" -d "$CACHE_DIR"
  rm -f "$ZIP"
fi
exec "$BIN" "$@"
