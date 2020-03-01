package com.uicode.smallchat.smallchatserver.model.messagingnotice;

import com.uicode.smallchat.smallchatserver.model.chat.internal.ChatStateInternal;

public class ChatStateNotice extends AbstractNotice {

    public static final String TOPIC = "state";

    private ChatStateInternal chatState;

    public ChatStateNotice() {
        super(TOPIC);
    }

    public ChatStateInternal getChatState() {
        return chatState;
    }

    public void setChatState(ChatStateInternal chatState) {
        this.chatState = chatState;
    }

}
