![Master CI](https://github.com/uicodefr/demo-smallchat/workflows/Master%20CI/badge.svg)

# SmallChat

Demo App With React, Vertx and Kafka that allows you to do instant messaging.



## features

- Instant messaging through kafka and websocket
- CRUD on channels
- Possibility to join several channels
- Notification for new messages
- Instant display of chat state evolution (channels / users)

## main dependencies used

smallchat-server :

- Vertx
- Vertx web with Websockets
- Google guice
- Vertx auth jwt
- Vertx kafka client



smallchat-client :

- React
- React Bootstrap
- Axios
- Rxjs
- Formik / Yup

