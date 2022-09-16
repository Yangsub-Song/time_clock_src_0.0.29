#!/usr/bin/env bash
cd /home/linaro
#/usr/bin/pulseaudio --system -D
#/usr/bin/xinit 

if [ -f "./update/update.sh" ]; then
    cd update
    bash update.sh
    cd ..
fi

if [ ! -e "./debug" ]; then
    export LOG4J_CONFIGURATION_FILE=./x
    rm -rf log
fi

exec startx /usr/bin/java -Xms1536M -Xmx1536M -Djdk.gtk.version=2 -Dprism.order=sw --module-path /usr/share/openjfx/lib --add-modules=javafx.base,javafx.controls,javafx.graphics,javafx.media --add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED -jar time_clock.jar 
