package com.uicode.smallchat.smallchatserver.exception;

import com.uicode.smallchat.smallchatserver.util.HttpStatus;

public class FunctionnalException extends AppException {

    private static final long serialVersionUID = 7343070813228688370L;

    public FunctionnalException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.I_AM_A_TEAPOT;
    }

}
