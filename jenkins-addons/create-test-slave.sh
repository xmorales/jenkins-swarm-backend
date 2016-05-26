#!/bin/bash
### Environment variables needed  ###
[ -z $DOCKER_HOST ] && exit 11
[ -z $image ] && exit 11
[ -z $ram ] && exit 11

#Tag it to not disturb jenkins launch policies
#Swarm bug, do not propagate tag instruction

docker pull ${image}
docker info | grep epg | awk '{print($2)}' | xargs -I1 docker -H 1 tag -f $image ${image}_test

# Start a new container on the cluster
echo "Starting container..."
id=$(docker run -d -P --name ${BUILD_USER_ID}_test -m ${ram}M ${image}_test)
[[ $? -ne 0 ]] && exit 1
# Set the password
echo "Defining a password..."
echo "contint:${contint_password}" | docker exec -i ${id} chpasswd
[[ $? -ne 0 ]] && exit 1
# Get connection info
access=($(docker inspect -f '{{ json .NetworkSettings.Ports }}' ${id} | python -mjson.tool | grep Host | awk 'BEGIN { FS = "\"" } ; { print $4 }'
))
echo "[ SUCCESS ] Ready to access using: ssh contint@${access[0]} -p ${access[1]}"
echo "Creating properties file to include on email"
echo "access=ssh contint@${access[0]} -p ${access[1]}" access.string

#Delete tag to avoid having duplicates
docker rmi ${image}_test
echo "Work done"
