package com.uicode.smallchat.smallchatserver.websocket;

public class WebSocketMsg<T> {

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
