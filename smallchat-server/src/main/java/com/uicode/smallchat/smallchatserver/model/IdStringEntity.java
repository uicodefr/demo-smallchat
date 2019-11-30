package com.uicode.smallchat.smallchatserver.model;

import java.util.Objects;

public class IdStringEntity {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!getClass().equals(obj.getClass()))
            return false;
        IdStringEntity other = (IdStringEntity) obj;
        return Objects.equals(id, other.id);
    }

}
