#!/usr/bin/env bash
set -euo pipefail

# Real AWT windows need a window manager for focus, stacking and native resizing.
openbox --sm-disable &
window_manager_pid=$!
trap 'kill "$window_manager_pid" 2>/dev/null || true' EXIT

window_manager_ready=false
for attempt in {1..50}; do
    if xprop -root _NET_SUPPORTING_WM_CHECK | grep -q 'window id #'; then
        window_manager_ready=true
        break
    fi
    sleep 0.1
done
if [[ "$window_manager_ready" != true ]]; then
    echo 'The X11 window manager did not become ready.' >&2
    exit 1
fi

./gradlew --no-daemon --max-workers=2 \
    -Dorg.gradle.jvmargs="${GRADLE_JVM_ARGS}" \
    -Pkotlin.compiler.execution.strategy=in-process \
    :ui2:koverVerify
