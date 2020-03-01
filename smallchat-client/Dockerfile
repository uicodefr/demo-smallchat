FROM nginx:1.15-alpine

MAINTAINER uicode.fr

RUN mkdir /log
VOLUME /log

EXPOSE 80

COPY ./client /usr/share/nginx/html/client
COPY ./conf/default.conf /etc/nginx/conf.d/default.conf
