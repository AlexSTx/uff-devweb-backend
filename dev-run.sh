#!/bin/sh
set -e

# Marker used to detect source changes since the last recompile.
touch /tmp/last_compile

# Start the Spring Boot app (spring-boot:run stays in the foreground);
# devtools is on the classpath and will hot-restart on classpath changes.
mvn -B spring-boot:run &
APP_PID=$!

trap 'kill $APP_PID 2>/dev/null; exit 0' INT TERM

# Give the app a moment to boot before we start polling src.
sleep 3

# Poll src for host-side edits (inotify on bind mounts can't see host writes),
# then recompile. The compile writes to target/classes inside the container,
# which devtools DOES see -> it triggers a fast app context restart.
while kill -0 $APP_PID 2>/dev/null; do
    if [ -n "$(find src -newer /tmp/last_compile -type f 2>/dev/null | head -1)" ]; then
        touch /tmp/last_compile
        echo "==> Source changed, recompiling..."
        mvn -B -q compile || echo "==> compile failed (will retry)"
    fi
    sleep 2
done

wait $APP_PID