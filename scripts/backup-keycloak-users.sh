#!/bin/bash
bin/standalone.sh -Djboss.socket.binding.port-offset=100   -Dkeycloak.migration.action=export -Dkeycloak.migration.provider=dir -Dkeycloak.migration.dir=/tmp/

