package com.uicode.smallchat.smallchatserver.exception;

import com.uicode.smallchat.smallchatserver.util.HttpStatus;

public class LimitationException extends AppRuntimeException {

    private static final long serialVersionUID = -1198440603691568695L;

    public LimitationException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }

}
