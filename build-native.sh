#!/bin/bash
./mvnw package -Pnative
#./mvnw package -Pnative -Dquarkus.native.container-build=true -DskipTests=true
