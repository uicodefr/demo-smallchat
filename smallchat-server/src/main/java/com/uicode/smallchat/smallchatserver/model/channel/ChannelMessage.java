package com.uicode.smallchat.smallchatserver.model.channel;

import java.util.Date;

public class ChannelMessage {

    public enum MessageCode {
        MSG, CONNECT, DISCONNECT, CREATED, UPDATED, DELETED
    }

    private String id;

    private String channelId;

    private String message;

    private String user;

    private Date date;

    private MessageCode code;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public MessageCode getCode() {
        return code;
    }

    public void setCode(MessageCode code) {
        this.code = code;
    }

}
