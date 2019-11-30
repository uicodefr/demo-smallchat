package com.uicode.smallchat.smallchatserver.model.chat;

import com.uicode.smallchat.smallchatserver.model.IdStringEntity;

public abstract class AbstractStateEntity extends IdStringEntity {

    private boolean delete;

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
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
