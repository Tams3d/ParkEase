#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

JAVAC=javac
if [ -n "${JAVA_HOME:-}" ]; then
  JAVAC="$JAVA_HOME/bin/javac"
fi

if ! command -v "$JAVAC" >/dev/null 2>&1; then
  echo "javac was not found. Install JDK 17 LTS or newer and set JAVA_HOME if needed." >&2
  exit 1
fi

mkdir -p out/classes
find src -name '*.java' > out/sources.txt
"$JAVAC" -encoding UTF-8 -d out/classes @out/sources.txt
echo "Build successful: out/classes"
