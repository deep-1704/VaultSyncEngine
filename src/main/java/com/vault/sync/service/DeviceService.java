package com.vault.sync.service;

import com.vault.sync.entity.Device;
import com.vault.sync.repository.DeviceRepository;
import com.vault.sync.repository.SyncItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final SyncItemRepository syncItemRepository;

    @Autowired
    public DeviceService(DeviceRepository _deviceRepository, SyncItemRepository _syncItemRepository){
        this.deviceRepository = _deviceRepository;
        this.syncItemRepository = _syncItemRepository;
    }

    public List<Device> getDevicesByOwner(String username){
        return deviceRepository.findAllByOwner(username);
    }
    public List<Device> getDevicesWithCred(Long credId){
        List<String> deviceIds = syncItemRepository.findDeviceIdsByCredentialId(credId);
        return deviceRepository.findAllByIdIn(deviceIds);
    }
    public List<Device> getDevicesWithCred(Long credId, String username){
        return getDevicesWithCred(credId)
                .stream()
                .filter(device -> (Objects.equals(device.getOwner(), username)))
                .toList();
    }
}
