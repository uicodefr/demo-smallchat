package com.uicode.smallchat.smallchatserver.exception;

import com.uicode.smallchat.smallchatserver.util.HttpStatus;

public abstract class AppRuntimeException extends RuntimeException implements HttpStatusReturn {

    private static final long serialVersionUID = -8402740476936824492L;

    public AppRuntimeException(String message) {
        super(message);
    }

    public AppRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
