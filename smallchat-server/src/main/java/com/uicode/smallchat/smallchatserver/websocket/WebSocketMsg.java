package com.uicode.smallchat.smallchatserver.websocket;

public class WebSocketMsg<T> {

    public static final String CHANNEL_MESSAGE_SUBJECT = "channel-message";
    public static final String CHAT_STATE_SUBJECT = "state";
    public static final String PING_SUBJECT = "ping";

    private String subject;

    private T data;

    private WebSocketMsg() {
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> WebSocketMsg<T> of(String subject, T data) {
        WebSocketMsg<T> webSocketMsg = new WebSocketMsg<>();
        webSocketMsg.setSubject(subject);
        webSocketMsg.setData(data);
        return webSocketMsg;
    }

}
