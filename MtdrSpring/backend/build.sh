#!/bin/bash

export IMAGE_NAME=todolistapp-springboot
export IMAGE_VERSION=$(date '+%Y%m%d_%H%M%S')  # Dynamic version based on timestamp

if [ -z "$DOCKER_REGISTRY" ]; then
    echo "DOCKER_REGISTRY not set. Will get it with state_get"
    export DOCKER_REGISTRY=$(state_get DOCKER_REGISTRY)
fi

if [ -z "$DOCKER_REGISTRY" ]; then
    echo "Error: DOCKER_REGISTRY env variable needs to be set!"
    exit 1
fi

export IMAGE=${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_VERSION}

docker build -f Dockerfile -t $IMAGE .
docker push $IMAGE

if [ $? -eq 0 ]; then
    # Update deployment with new image
    kubectl set image deployment/todolistapp-springboot-deployment \
        todolistapp-springboot=$IMAGE -n mtdrworkshop
    
    # Watch the rollout status
    kubectl rollout status deployment/todolistapp-springboot-deployment -n mtdrworkshop
    
    docker rmi "$IMAGE"
fi