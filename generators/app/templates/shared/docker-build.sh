#!/usr/bin/env bash

set -e

SERVICE_NAME=<%= props.appName %>
TAG=$1

if [ -z "$TAG" ] ; then
  echo "Tag not specified - eg 0.1.0"
  echo "Note: you need to have the dist zip present"
  echo
  echo "Usage:"
  echo "./docker-build.sh <tag>"
  echo
  echo "Example:"
  echo "./docker-build.sh latest"
  exit
fi

rm -rf "$SERVICE_NAME"

unzip "target/universal/$SERVICE_NAME".zip

docker build -t "docker.movio.co/$SERVICE_NAME:$TAG" .
