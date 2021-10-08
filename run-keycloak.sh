#!/bin/bash
docker rm -f keycloak
docker run --name keycloak -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin -p 8180:8080  -e KEYCLOAK_IMPORT=/tmp/quarkus-realm.json -v $PWD/src/test/resources/genny-realm.json:/tmp/quarkus-realm.json -v $PWD/scripts/backup-keycloak-users.sh:/opt/jboss/keycloak/backup-keycloak-users.sh -v $PWD/scripts/docker-exec-cmd.sh:/tmp/docker-exec-cmd.sh  jboss/keycloak

