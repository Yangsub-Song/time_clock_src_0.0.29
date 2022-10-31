#!/bin/bash

echo "====================================="
echo " UPDATE..."
echo "====================================="

echo "  copying time_clock.jar"
yes | cp time_clock.jar ../
echo "  copying time_clock.sha"
yes | cp time_clock.sha ../

if [ -e "./audio" ]; then
    echo "  copying audio files"
    rm -rf ../audio
    yes | cp -rf audio ../
fi

if [ -e "./run.sh" ]; then
    echo "  copying run.sh"
    yes | cp -rf run.sh ../
fi

echo "  cleaning..."
rm -rf ../update

sleep 1

echo "====================================="
echo " DONE "
echo "====================================="
