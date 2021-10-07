#!/bin/bash
#access_token=`./getAliceToken.sh`
access_token=`./getAdamToken.sh`
curl -s  -X  GET  --header 'Content-Type: application/json' --header 'Accept: application/json' --header "Authorization: Bearer $access_token"  --header "Cache-Control: no-cache" "http://localhost:8080/api/users/me"
