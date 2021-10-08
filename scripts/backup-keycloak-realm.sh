#!/bin/bash
# backup-keycloak.sh

# Copy the export bash script to the (already running) keycloak container
# to perform an export
docker cp docker-exec-cmd.sh keycloak:/tmp/docker-exec-cmd.sh
# Execute the script inside of the container
docker exec -it keycloak /tmp/docker-exec-cmd.sh
# Grab the finished export from the container
docker cp keycloak:/tmp/realms-export-single-file.json .
