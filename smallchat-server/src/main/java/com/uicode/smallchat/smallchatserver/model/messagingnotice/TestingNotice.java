package com.uicode.smallchat.smallchatserver.model.messagingnotice;

public class TestingNotice extends AbstractNotice {

    public static final String TOPIC = "test";

    private String value;

    public TestingNotice() {
        super(TOPIC);
    }

    public TestingNotice(String value) {
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
