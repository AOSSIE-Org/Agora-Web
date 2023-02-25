FROM openjdk:alpine

MAINTAINER dev@aossie.org

RUN mkdir -p /app/

ADD ./agora-api.jar /app/agora-api.jar


WORKDIR /app

RUN echo '#!/bin/ash' >> /app/run.sh
RUN echo 'java -Dhttp.port=8080 -jar /app/agora-api.jar' >> /app/run.sh
RUN chmod a+x /app/run.sh

CMD [ "/app/run.sh" ]