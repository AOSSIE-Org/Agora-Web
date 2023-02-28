FROM openjdk:alpine

MAINTAINER dev@aossie.org

# Update Alpine Linux Package Manager and Install the bash
RUN apk update && apk add bash

RUN mkdir -p /app/

ADD ./target/universal/stage /app

CMD ["/bin/bash", "/app/bin/agora-rest-api", "-Dhttp.port=8080"]