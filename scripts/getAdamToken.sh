#!/bin/bash
export access_token=$(\
    curl  -X POST https://keycloak.gada.io/auth/realms/quarkus/protocol/openid-connect/token \
    --user backend-service:secret \
    -H 'content-type: application/x-www-form-urlencoded' \
     --data-urlencode   "username=adamcrow63+bob@gmail.com" -d "password=asdf1234&grant_type=password" | jq --raw-output '.access_token' \
 )
echo $access_token

