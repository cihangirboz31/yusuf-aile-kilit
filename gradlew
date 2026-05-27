#!/usr/bin/env sh

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
DIRNAME="$0"
CDPATH=""
case "$DIRNAME" in
  /*) APP_HOME="${DIRNAME%/*}" ;;
  *)  APP_HOME="./${DIRNAME%/*}" ;;
esac
APP_HOME=$( (cd "$APP_HOME" && pwd) )

APP_NAME="Gradle"
APP_BASE_NAME=${0##*/}

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

if [ "$APP_HOME" = "" ] ; then
    APP_HOME="$(pwd)"
fi

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

JAVACMD="java"
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
fi

exec "$JAVACMD" \
  $DEFAULT_JVM_OPTS \
  $JAVA_OPTS \
  $GRADLE_OPTS \
  "-Dorg.gradle.appname=$APP_BASE_NAME" \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
