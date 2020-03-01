FROM adoptopenjdk/openjdk8:alpine-slim

MAINTAINER uicode.fr

RUN mkdir /log
VOLUME /log

EXPOSE 8080

COPY ./server.jar /server.jar

ENTRYPOINT ["java"]
CMD ["-Djava.security.egd=file:/dev/./urandom", "-jar", "/server.jar"]
