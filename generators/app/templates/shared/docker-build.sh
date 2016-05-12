#!/usr/bin/env bash

set -e

DOCKER_REPO=<%= props.dockerRepoUrl %>
SERVICE_NAME=<%= props.appName %>
TAG=$1

if [ -z "$DOCKER_REPO" ] ; then
    echo "DOCKER_REPO environment variable not set"
    echo "If you push this image it will be pushed to the public docker repo"
fi

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

unzip "$SERVICE_NAME".zip

docker build -t "${DOCKER_REPO}${SERVICE_NAME}:${TAG}" .
