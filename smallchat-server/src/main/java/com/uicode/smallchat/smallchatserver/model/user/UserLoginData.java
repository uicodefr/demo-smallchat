package com.uicode.smallchat.smallchatserver.model.user;

public class UserLoginData {

    private String jwtToken;

    private UserPayload userPayload;

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public UserPayload getUserPayload() {
        return userPayload;
    }

    public void setUserPayload(UserPayload userPayload) {
        this.userPayload = userPayload;
    }

}
