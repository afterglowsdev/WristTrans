#!/bin/sh
#
# Gradle wrapper start script for POSIX shells.
#
# This file is intentionally checked into the repo because GitHub Actions (ubuntu)
# expects `./gradlew` to exist.
#

set -eu

APP_HOME=$(cd "$(dirname "$0")" && pwd)

# Build the classpath (Gradle 8.x split wrapper into 2 jars).
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar:$APP_HOME/gradle/wrapper/gradle-wrapper-shared.jar:$APP_HOME/gradle/wrapper/gradle-cli.jar"

if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
else
  JAVA_CMD="java"
fi

exec "$JAVA_CMD" \
  -Dorg.gradle.appname=gradlew \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
