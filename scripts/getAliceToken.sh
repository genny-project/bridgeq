#!/bin/bash
export access_token=$(\
    curl -s -X POST https://keycloak.gada.io/auth/realms/quarkus/protocol/openid-connect/token \
    --user backend-service:secret \
    -H 'content-type: application/x-www-form-urlencoded' \
    -d 'username=alice&password=asdf1234&grant_type=password' | jq --raw-output '.access_token' \
 )
echo $access_token

