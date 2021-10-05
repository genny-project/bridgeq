#!/bin/bash
project=`echo "${PWD##*/}" | tr '[:upper:]' '[:lower:]'`
file="src/main/resources/${project}-git.properties"
org=gennyproject
function prop() {
  grep "${1}=" ${file} | cut -d'=' -f2
}
#version=$(prop 'git.build.version')

if [ -z "${1}" ]; then
  version=$(cat src/main/resources/${project}-git.properties | grep 'git.build.version' | cut -d'=' -f2)
else
  version="${1}"
fi

version=$version-native

USER=`whoami`
#./mvnw package -Pnative -Dquarkus.native.container-build=true -DskipTests=false
#./mvnw package -Pnative -Dquarkus.native.container-build=true
# MUST HAVE GRAALVM installed!!! (so run under linux with lots of memory)
#./mvnw package -Pnative  -Dquarkus.native.container-build=true  -Dquarkus.container-image.build=true

#th lots of memory)Build using Linux on an osx
./mvnw package -Pnative  -Dquarkus.native.container-build=true  -Dquarkus.container-image.build=true

#docker build -f src/main/docker/Dockerfile.native -t ${USER}/${project}:${version} .
docker tag ${USER}/${project}:${version} ${org}/${project}:${version}
docker tag ${USER}/${project}:${version} ${org}/${project}:native
