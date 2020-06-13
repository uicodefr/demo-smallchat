package com.uicode.smallchat.smallchatserver.exception.runtime;

import com.uicode.smallchat.smallchatserver.util.HttpStatus;

public class NotFoundException extends AppRuntimeException {

    private static final long serialVersionUID = -4859387746748305585L;

    public NotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

}
