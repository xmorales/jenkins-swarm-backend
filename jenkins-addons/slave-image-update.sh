#!/bin/bash
####### docker clean images script #######
### Tests if we hace a swarm cluster ###
### endpoint or a single server      ###
### Environment variables needed     ###
[ -z $DOCKER_HOST ] && exit 11
### If endpoint is secured also define #
# DOCKER_TLS_VERIFY=1
# DOCKER_CERT_PATH

IMAGE_LIST=""

Pull_Images( )
{
  for image in $IMAGE_LIST
  do
    echo "Downloading $image"
    docker pull $image
  done
}

if [[ "$image" == "" ]]; then
  echo '[INFO] Update all images'
  IMAGE_LIST=`docker images | grep -v none | awk '{print($1":"$2)}' | sort -u `
else
  IMAGE_LIST=`echo $image`
fi

Pull_Images

#Never fail as oftenly are problems 
exit 0
