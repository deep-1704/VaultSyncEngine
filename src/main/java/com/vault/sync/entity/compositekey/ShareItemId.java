package com.vault.sync.entity.compositekey;

import java.io.Serializable;
import java.util.Objects;

public class ShareItemId implements Serializable {
    private String deviceId;
    private Long sharedCredId;

    public ShareItemId() {}

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

    public ShareItemId(String deviceId, Long sharedCredId) {
        this.deviceId = deviceId;
        this.sharedCredId = sharedCredId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ShareItemId that = (ShareItemId) o;
        return Objects.equals(getDeviceId(), that.getDeviceId()) && Objects.equals(getSharedCredId(), that.getSharedCredId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeviceId(), getSharedCredId());
    }
}
