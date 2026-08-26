package com.vault.sync.service;

import com.vault.sync.entity.Device;
import com.vault.sync.repository.DeviceRepository;
import com.vault.sync.repository.ShareItemRepository;
import com.vault.sync.repository.SyncItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final ShareItemRepository shareItemRepository;

    @Autowired
    public DeviceService(DeviceRepository _deviceRepository, ShareItemRepository _shareItemRepository){
        this.deviceRepository = _deviceRepository;
        this.shareItemRepository = _shareItemRepository;
    }

    public List<Device> getDevicesByOwner(String username){
        return deviceRepository.findAllByOwner(username);
    }

    public List<Device> getDevicesByShareId(Long sharedCredId){
        List<String> deviceIds = shareItemRepository.findDeviceIdsBySharedCredId(sharedCredId);
        return deviceRepository.findAllByIdIn(deviceIds);
    }
}
