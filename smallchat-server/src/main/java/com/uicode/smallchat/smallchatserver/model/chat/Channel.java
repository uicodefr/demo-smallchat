package com.uicode.smallchat.smallchatserver.model.chat;

public class Channel extends AbstractStateEntity {

    private String name;
    private String description;

    public Channel() {
    }

    public Channel(Channel channel) {
        super(channel);
        this.name = channel.name;
        this.description = channel.description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

}
