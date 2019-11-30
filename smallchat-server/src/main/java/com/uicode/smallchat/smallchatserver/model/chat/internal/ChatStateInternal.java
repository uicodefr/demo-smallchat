package com.uicode.smallchat.smallchatserver.model.chat.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.uicode.smallchat.smallchatserver.model.chat.Channel;
import com.uicode.smallchat.smallchatserver.model.chat.ChatState;
import com.uicode.smallchat.smallchatserver.model.chat.ChatUser;

public class ChatStateInternal {

    private Map<String, Channel> channels;

    private Map<String, ChatUser> users;

    private Date updateDate;

    public ChatStateInternal() {
        channels = new HashMap<>();
        users = new HashMap<>();
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, Channel> channels) {
        this.channels = channels;
    }

    public Map<String, ChatUser> getUsers() {
        return users;
    }

    public void setUsers(Map<String, ChatUser> users) {
        this.users = users;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public ChatState toChatState() {
        ChatState chatState = new ChatState();
        chatState.setChannels(new ArrayList<>(getChannels().values()));
        chatState.setUsers(new ArrayList<>(getUsers().values()));
        chatState.setUpdateDate(getUpdateDate());

        // Sort User and Channel by Ids
        chatState.getChannels().sort((chat1, chat2) -> chat1.getId().compareTo(chat2.getId()));
        chatState.getUsers().sort((user1, user2) -> user1.getId().compareTo(user2.getId()));

        return chatState;
    }

}
