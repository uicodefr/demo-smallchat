package com.uicode.smallchat.smallchatserver.model.user;

import java.util.List;

public class UserPayload {
    
    public static final String USERNAME_FIELD = "username";

    private String username;

    private List<String> roles;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

}
