package com.uicode.smallchat.smallchatserver.model;

import java.util.Objects;

public class IdLongEntity {

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
        IdLongEntity other = (IdLongEntity) obj;
        return Objects.equals(id, other.id);
    }

}
