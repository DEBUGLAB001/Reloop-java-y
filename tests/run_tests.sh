#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT"

# Recompile candidate modified source and tests before testing; redirect build logs to stderr
mvn test-compile -q -B >&2

# Determine classpath separator (: for Linux/macOS containers, ; for Windows)
SEP=":"
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" || "$OSTYPE" == "win32" ]]; then
  SEP=";"
fi

java -cp "target/classes${SEP}target/test-classes" com.debuglab.reloop.TestRunner
