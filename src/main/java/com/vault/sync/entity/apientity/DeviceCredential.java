package com.vault.sync.entity.apientity;

import com.vault.sync.entity.Credential;
import com.vault.sync.entity.Device;

public record DeviceCredential(
        Device device,
        Credential credential,
        String content
) { }
