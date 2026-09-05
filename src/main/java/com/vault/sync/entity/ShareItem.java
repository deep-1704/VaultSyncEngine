package com.vault.sync.entity;

import com.vault.sync.entity.compositekey.ShareItemId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

@Entity
@IdClass(ShareItemId.class)
public class ShareItem {
    @Id
    String deviceId;
    @Id
    Long sharedCredId;

    @Column(columnDefinition = "TEXT")
    String content;

    public ShareItem() {
    }

    public ShareItem(String deviceId, Long sharedCredId, String content) {
        this.deviceId = deviceId;
        this.sharedCredId = sharedCredId;
        this.content = content;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Long getSharedCredId() {
        return sharedCredId;
    }

    public void setSharedCredId(Long sharedCredId) {
        this.sharedCredId = sharedCredId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
