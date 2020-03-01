all:
	@echo "do nothing"

test:
	make -C smallchat-server test
	make -C smallchat-client test

buildOnly:
	make -C smallchat-server buildOnly
	make -C smallchat-client buildOnly
	
build:
	make -C smallchat-server build
	make -C smallchat-client build

containerize:
	make -C smallchat-server containerize
	make -C smallchat-client containerize
