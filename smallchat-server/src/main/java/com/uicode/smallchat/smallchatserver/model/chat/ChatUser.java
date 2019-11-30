package com.uicode.smallchat.smallchatserver.model.chat;

public class ChatUser extends AbstractStateEntity {

    private String pseudo;

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
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
