#!/bin/bash

access_token=`./getAdminToken.sh`
curl -s -X GET \
   http://localhost:8080/api/admin \
   -H "Authorization: Bearer "$access_token

