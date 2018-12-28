#!/bin/bash
cd "$(dirname "$0")"
gradle clean build
cp -rf build/libs/*.jar ../server-pack-dev/plugins/.
