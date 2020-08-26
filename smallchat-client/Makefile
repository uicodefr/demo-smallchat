VERSION=$(shell sed "s/.*\"version\": \"\(.*\)\".*/\1/;t;d" package.json)
NEXUS_USER=deployment
DOCKER_URL=docker.uicode.dev
NEXUS_RAW_PUSH_URL=https://nexus.uicode.dev/service/rest/v1/components?repository=raw
NEXUS_RAW_GET_URL=https://nexus.uicode.dev/repository/raw/

RAW_GROUP_ID=demo/smallchat-client
RAW_ARTIFACT_ID=smallchat-client
DOCKER_IMG_NAME=smallchat-client

all:
	@echo "do nothing (version: $(VERSION))"

test:
	@echo "test (version: $(VERSION))"
	npm ci
	npm test -- --watchAll=false

buildOnly:
	@echo "buildOnly (version: $(VERSION))"
	npm ci
	npm run build
	mkdir -p dist && rm -rf ./dist/smallchat-client && cp -r ./build ./dist/smallchat-client && rm -r ./build

build:
	@echo "build (version: $(VERSION))"
	npm ci
	npm run build
	mkdir -p dist && rm -rf ./dist/smallchat-client && cp -r ./build ./dist/smallchat-client && rm -r ./build
	cd dist && zip -r ../smallchat-client-$(VERSION).zip ./smallchat-client
	curl -v --progress-bar -u $(NEXUS_USER):$$NEXUS_PASSWORD -F "raw.directory=$(RAW_GROUP_ID)" -F "raw.asset1=@./$(RAW_ARTIFACT_ID)-$(VERSION).zip" -F "raw.asset1.filename=$(RAW_ARTIFACT_ID)-$(VERSION).zip" $(NEXUS_RAW_PUSH_URL)
	rm smallchat-client-$(VERSION).zip

containerize:
	@echo "containerize (version: $(VERSION))"
	curl -v --progress-bar -u $(NEXUS_USER):$$NEXUS_PASSWORD $(NEXUS_RAW_GET_URL)$(RAW_GROUP_ID)/$(RAW_ARTIFACT_ID)-$(VERSION).zip --output client.zip
	unzip client.zip
	mv smallchat-client client
	docker build --tag $(DOCKER_IMG_NAME):$(VERSION) --tag $(DOCKER_IMG_NAME):latest ./
	echo $$NEXUS_PASSWORD | docker login --username $(NEXUS_USER) --password-stdin $(DOCKER_URL)
	docker tag $(DOCKER_IMG_NAME) $(DOCKER_URL)/$(DOCKER_IMG_NAME):$(VERSION)
	docker push $(DOCKER_URL)/$(DOCKER_IMG_NAME):$(VERSION)
	rm client.zip
	rm -R client