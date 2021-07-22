VERSION=$(shell sed "s/^\t<version>\(.*\)<\/version>/\1/;t;d" pom.xml)
NEXUS_USER=deployment
DOCKER_URL=docker.uicode.dev

MVN_URL=https://nexus.uicode.dev/repository/maven-public/
MVN_GROUP_ID=com.uicode.demo.smallchat
MVN_ARTIFACT_ID=smallchat-server
JAR_NAME=smallchat-server-*.jar
DOCKER_IMG_NAME=smallchat-server

all:
	@echo "do nothing (version: $(VERSION))"

test:
	@echo "test (version: $(VERSION))"
	./mvnw test
	
buildOnly:
	@echo "buildOnly (version: $(VERSION))"
	./mvnw package

build:
	@echo "build (version: $(VERSION))"
	./mvnw deploy

containerize:
	@echo "containerize (version: $(VERSION))"
	./mvnw jib:build -Djib.to.image=$(DOCKER_URL)/$(DOCKER_IMG_NAME):$(VERSION) \
		-Djib.to.auth.username=$(NEXUS_USER) \
		-Djib.to.auth.password=$$NEXUS_PASSWORD