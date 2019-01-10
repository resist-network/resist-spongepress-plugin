#!/bin/bash
cd "$(dirname "$0")"
gradle clean build
read -p "Press any key to exit!"

