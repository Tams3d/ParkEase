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

if ! compgen -G "lib/mysql-connector-j-*.jar" >/dev/null; then
  echo
  echo "MySQL Connector/J was not found in lib/." >&2
  echo "Download mysql-connector-j-26.7.0.jar from:" >&2
  echo "  https://dev.mysql.com/downloads/connector/j/" >&2
  exit 1
fi

"$JAVA" -cp "out/classes:lib/*" parkease.Main
