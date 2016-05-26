#!/bin/bash
####### docker clean images script #######
### Tests if we hace a swarm cluster ###
### endpoint or a single server      ###
### Environment variables needed     ###
[ -z $DOCKER_HOST ] && exit 11
### If endpoint is secured also define #
# DOCKER_TLS_VERIFY=1
# DOCKER_CERT_PATH

function clean-single() {
  docker -H $1 images | grep none | awk '{print($3)}' | xargs docker -H $1 rmi
}

function clean-cluster() {
  echo '[INFO] Cleaning cluster images'
  nodes=$(docker info | grep 4243 | awk '{print $2}')

  # We need to go to each node as swarm do not print untagged images
  echo '[INFO] Clean all untagged images to recover space'
  for node in $nodes; do
    echo "Cleaning endpoint $node"
    clean-single $node
  done

  echo 'Total available space in cluster:'
  for node in $nodes; do
    echo $node:
    docker -H $node info | grep Available
  done

}

# Getting info about endpoint
version=$(curl -k $DOCKER_HOST/version | python -m json.tool | grep \"Version\": | awk '{print $2}')

if [[ $(echo $version | grep -q swarm) ]]; then
  # Is a swarm cluster
  clean-cluster
else
  # Get any other type as a single instance
  clean-single $DOCKER_HOST
fi
