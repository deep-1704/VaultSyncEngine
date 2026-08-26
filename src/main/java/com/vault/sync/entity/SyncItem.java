package com.vault.sync.entity;

import com.vault.sync.entity.compositekey.SyncItemId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

@Entity
@IdClass(SyncItemId.class)
public class SyncItem {
    @Id
    String deviceId;
    @Id
    Long credentialId;
    String content;

    public SyncItem(String deviceId, Long credentialId, String content) {
        this.deviceId = deviceId;
        this.credentialId = credentialId;
        this.content = content;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Long getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(Long credentialId) {
        this.credentialId = credentialId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
