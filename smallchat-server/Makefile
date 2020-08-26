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
	chmod u+x mvnw
	./mvnw test
	
buildOnly:
	@echo "buildOnly (version: $(VERSION))"
	chmod u+x mvnw
	./mvnw package

build:
	@echo "build (version: $(VERSION))"
	chmod u+x mvnw
	./mvnw deploy

containerize:
	@echo "containerize (version: $(VERSION))"
	chmod u+x mvnw
	./mvnw dependency:get -DrepoUrl=$(MVN_URL) -Dartifact=$(MVN_GROUP_ID):$(MVN_ARTIFACT_ID):$(VERSION)
	./mvnw dependency:copy -Dartifact=$(MVN_GROUP_ID):$(MVN_ARTIFACT_ID):$(VERSION) -DoutputDirectory=.
	mv $(JAR_NAME) server.jar
	echo $$NEXUS_PASSWORD | docker login --username $(NEXUS_USER) --password-stdin $(DOCKER_URL)
	docker build --tag $(DOCKER_IMG_NAME):$(VERSION) --tag $(DOCKER_IMG_NAME):latest ./
	docker tag $(DOCKER_IMG_NAME) $(DOCKER_URL)/$(DOCKER_IMG_NAME):$(VERSION)
	docker push $(DOCKER_URL)/$(DOCKER_IMG_NAME):$(VERSION)
	rm server.jar
