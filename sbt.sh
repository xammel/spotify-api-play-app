#!/usr/bin/env bash

./sbt-dist/bin/sbt -jvm-debug 5005 "$@"
