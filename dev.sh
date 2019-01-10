#!/bin/bash
cd "$(dirname "$0")"
gradle clean build
read -p "Press any key to start server, or ctrl+c to exit if build fails..."
cp -rf build/libs/*.jar ../_dev/plugins/.
cd ../_dev
./start_windows.sh
cd ../resist-spongepress-plugin
