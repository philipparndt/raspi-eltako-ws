#!/bin/sh
mvn clean install assembly:single
if [ $? -eq 0 ]; then
    scp ./target/mp3player.jar 192.168.3.34:/home/pi/mp3player.jar
    echo "deploy finished"
else
    echo FAIL
fi