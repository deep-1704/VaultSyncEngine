package com.vault.sync.entity.compositekey;

import java.io.Serializable;
import java.util.Objects;

public class SyncItemId implements Serializable {
    private String deviceId;
    private Long credentialId;

    public SyncItemId(String deviceId, Long credentialId) {
        this.deviceId = deviceId;
        this.credentialId = credentialId;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SyncItemId that = (SyncItemId) o;
        return Objects.equals(getDeviceId(), that.getDeviceId()) && Objects.equals(getCredentialId(), that.getCredentialId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeviceId(), getCredentialId());
    }
}