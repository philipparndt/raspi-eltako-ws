#!/bin/sh
mvn clean install assembly:single
if [ $? -eq 0 ]; then
    scp ./de.rnd7.ws/target/ws.jar 192.168.3.104:/home/pi/ws.jar
    echo "deploy finished"
else
    echo FAIL
fi