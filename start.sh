#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "[ReLoop Watcher] Starting ReLoop operations platform on 0.0.0.0:3000..."

APP_PID=""

cleanup() {
    echo "[ReLoop Watcher] Shutting down..."
    if [ -n "$APP_PID" ] && kill -0 "$APP_PID" 2>/dev/null; then
        kill "$APP_PID" 2>/dev/null || true
        wait "$APP_PID" 2>/dev/null || true
    fi
    exit 0
}

trap cleanup SIGINT SIGTERM EXIT

# Initial compilation
echo "[ReLoop Watcher] Performing build verification..."
mvn compile -q -B

start_app() {
    mvn spring-boot:run &
    APP_PID=$!
    echo "[ReLoop Watcher] Server process launched (PID: $APP_PID)"
}

start_app

get_src_hash() {
    find src/main/java src/main/resources -type f -exec ls -l {} + 2>/dev/null | md5sum | awk '{print $1}'
}

LAST_HASH=$(get_src_hash)

while true; do
    sleep 3
    CURRENT_HASH=$(get_src_hash)
    if [ -n "$CURRENT_HASH" ] && [ "$CURRENT_HASH" != "$LAST_HASH" ]; then
        echo "[ReLoop Watcher] Detected source modifications. Recompiling..."
        if mvn compile -q -B; then
            echo "[ReLoop Watcher] Build successful. Restarting application server..."
            if [ -n "$APP_PID" ] && kill -0 "$APP_PID" 2>/dev/null; then
                kill "$APP_PID" 2>/dev/null || true
                wait "$APP_PID" 2>/dev/null || true
            fi
            LAST_HASH="$CURRENT_HASH"
            start_app
        else
            echo "[ReLoop Watcher] Compilation error encountered. Holding existing server instance."
        fi
    fi
done
