#!/bin/bash
docker rm -f keycloak
docker run --name keycloak -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin -p 8180:8080  -e KEYCLOAK_IMPORT=/tmp/quarkus-realm.json -v $PWD/config/quarkus-realm.json:/tmp/quarkus-realm.json   jboss/keycloak

