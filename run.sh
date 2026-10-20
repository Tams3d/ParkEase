#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"
./build.sh

JAVA=java
if [ -n "${JAVA_HOME:-}" ]; then
  JAVA="$JAVA_HOME/bin/java"
fi

if ! command -v "$JAVA" >/dev/null 2>&1; then
  echo "Java was not found. Install JDK 17 LTS or newer and set JAVA_HOME if needed." >&2
  exit 1
fi

echo "Starting ParkEase backend. Press Ctrl+C to stop."
"$JAVA" -jar target/parkease.jar
