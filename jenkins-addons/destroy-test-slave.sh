#!/bin/bash
### Environment variables needed  ###
[ -z $DOCKER_HOST ] && exit 11

echo "Deleting ${BUILD_USER_ID}_test slave"
docker kill ${BUILD_USER_ID}_test && \
docker rm ${BUILD_USER_ID}_test && \
echo "Instance successfully deleted"
