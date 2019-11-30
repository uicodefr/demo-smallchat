package com.uicode.smallchat.smallchatserver.exception;

import com.uicode.smallchat.smallchatserver.util.HttpStatus;

public class InvalidDataException extends AppException {

    private static final long serialVersionUID = -3870792412125495745L;

    public InvalidDataException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
