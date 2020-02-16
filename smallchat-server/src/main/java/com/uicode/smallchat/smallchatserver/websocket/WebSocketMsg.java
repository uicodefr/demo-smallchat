package com.uicode.smallchat.smallchatserver.websocket;

public class WebSocketMsg<T> {
    
    public static class WebSocketMsgString extends WebSocketMsg<String> {
    }

    public static final String CHANNEL_PREFIX = "#";
    public static final String USER_PREFIX = "@";
    public static final String CHAT_STATE_CHANNEL = "state";
    public static final String PING_CHANNEL = "ping";


    private String channel;

    private T data;

    private WebSocketMsg() {
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> WebSocketMsg<T> of(String channel, T data) {
        WebSocketMsg<T> webSocketMsg = new WebSocketMsg<>();
        webSocketMsg.setChannel(channel);
        webSocketMsg.setData(data);
        return webSocketMsg;
    }

}
