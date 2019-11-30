package com.uicode.smallchat.smallchatserver.exception;

import com.uicode.smallchat.smallchatserver.util.HttpStatus;

public class InvalidJWTException extends AppException {

    private static final long serialVersionUID = 911495334751387896L;
    
    public InvalidJWTException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNAUTHORIZED;
    }

}
