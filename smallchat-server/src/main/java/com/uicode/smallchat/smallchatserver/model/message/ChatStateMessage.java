package com.uicode.smallchat.smallchatserver.model.message;

import com.uicode.smallchat.smallchatserver.model.chat.internal.ChatStateInternal;

public class ChatStateMessage extends AbstractMessage {

    public static final String TOPIC = "chat_state";

    private ChatStateInternal chatState;

    public ChatStateMessage() {
        super(TOPIC);
    }

    public ChatStateInternal getChatState() {
        return chatState;
    }

    public void setChatState(ChatStateInternal chatState) {
        this.chatState = chatState;
    }

}
