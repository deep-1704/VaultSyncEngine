package com.vault.sync.service;

import com.vault.sync.entity.Device;
import com.vault.sync.entity.SyncItem;
import com.vault.sync.repository.DeviceRepository;
import com.vault.sync.repository.SyncItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class SyncService {
    private final SyncItemRepository syncItemRepository;
    private final DeviceRepository deviceRepository;

    @Autowired
    public SyncService(
            SyncItemRepository _syncItemRepository,
            DeviceRepository _deviceRepository
    ){
        this.syncItemRepository = _syncItemRepository;
        this.deviceRepository = _deviceRepository;
    }

    public void syncDevices(List<SyncItem> syncItems){
        syncItemRepository.saveAll(syncItems);
    }

    public List<SyncItem> getSyncedCredentials(String deviceId){
        return syncItemRepository.findAllByDeviceId(deviceId);
    }

    public boolean checkDeviceBelongsToUser(List<String> deviceIds, String username){
        List<Device> devices = deviceRepository.findAllByIdIn(deviceIds);

        for(Device d : devices){
            if(!Objects.equals(d.getOwner(), username)) return false;
        }
        return true;
    }
}
