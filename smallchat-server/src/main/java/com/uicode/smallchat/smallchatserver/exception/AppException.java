package com.uicode.smallchat.smallchatserver.exception;

import com.uicode.smallchat.smallchatserver.util.HttpStatus;

public abstract class AppException extends Exception implements HttpStatusReturn {

    private static final long serialVersionUID = 5339900106644426283L;

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
