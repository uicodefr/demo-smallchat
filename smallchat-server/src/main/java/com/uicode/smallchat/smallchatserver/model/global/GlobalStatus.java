package com.uicode.smallchat.smallchatserver.model.global;

import java.util.Date;

public class GlobalStatus {

    private String status;

    private Integer confId;

    private String messaging;

    private Date upDate;

    private Date currentDate;

    private String version;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getConfId() {
        return confId;
    }

    public void setConfId(Integer confId) {
        this.confId = confId;
    }

    public String getMessaging() {
        return messaging;
    }

    public void setMessaging(String messaging) {
        this.messaging = messaging;
    }

    public Date getUpDate() {
        return upDate;
    }

    public void setUpDate(Date upDate) {
        this.upDate = upDate;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
