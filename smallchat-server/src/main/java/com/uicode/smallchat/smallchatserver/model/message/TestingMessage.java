package com.uicode.smallchat.smallchatserver.model.message;

public class TestingMessage extends AbstractMessage {

    public static final String TOPIC = "test";
    
    private String value;

    public TestingMessage() {
        super(TOPIC);
    }

    public TestingMessage(String value) {
        this();
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
