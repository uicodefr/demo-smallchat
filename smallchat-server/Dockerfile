FROM adoptopenjdk/openjdk11:alpine-slim

LABEL maintainer="uicode.fr"

RUN mkdir /log
VOLUME /log

RUN mkdir /db
VOLUME /db

EXPOSE 8080

COPY ./server.jar /server.jar

ENTRYPOINT ["java"]
CMD ["-Djava.security.egd=file:/dev/./urandom", "-jar", "/server.jar"]
