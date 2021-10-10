#!/bin/bash
#export KEYCLOAK_AUTH_URL=https://keycloak.gada.io/auth/realms/internmatch
#export KEYCLOAK_CLIENT_ID=alyson
export KEYCLOAK_AUTH_URL=http://localhost:8180/auth/realms/quarkus
export KEYCLOAK_CLIENT_ID=frontend
./mvnw clean quarkus:dev -Ddebug=5555

