#!/bin/bash
cd "$(dirname "$0")"
gradle clean build
read -p "Press any key to start server, or ctrl+c to exit if build fails..."
cp -rf build/libs/*.jar ../resist-server-pack-dev/plugins/.
cd ../resist-server-pack-dev
./start_windows.bat
cd ../resist-wordpress-sponge
