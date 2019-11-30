package com.uicode.smallchat.smallchatserver.model.chat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatState {

    private List<Channel> channels;

    private List<ChatUser> users;

    private Date updateDate;

    public ChatState() {
        channels = new ArrayList<>();
        users = new ArrayList<>();
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public List<ChatUser> getUsers() {
        return users;
    }

    public void setUsers(List<ChatUser> users) {
        this.users = users;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

}
