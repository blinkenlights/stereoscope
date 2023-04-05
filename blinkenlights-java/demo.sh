#!/usr/bin/env bash

set -e

./mvnw package || (echo "Build failed. No demo."; exit 1)

java -jar bmix/target/bmix-1.5-SNAPSHOT-jar-with-dependencies.jar bmix/src/main/config/bmix-example.xml &
java -jar bmix-sender/target/bmix-sender-1.5-SNAPSHOT-jar-with-dependencies.jar &
java -jar bmix-statistics/target/bmix-statistics-1.5-SNAPSHOT-jar-with-dependencies.jar &
