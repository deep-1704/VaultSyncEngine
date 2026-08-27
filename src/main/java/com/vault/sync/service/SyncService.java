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

    @Autowired
    public SyncService(SyncItemRepository _syncItemRepository){
        this.syncItemRepository = _syncItemRepository;
    }

    public void syncDevices(List<SyncItem> syncItems){
        syncItemRepository.saveAll(syncItems);
    }

    public List<SyncItem> getSyncedCredentials(String deviceId){
        return syncItemRepository.findAllByDeviceId(deviceId);
    }

    public void deleteSyncEntries(List<String> deviceIds, Long credId){
        syncItemRepository.deleteByCredentialIdAndDeviceIdIn(credId, deviceIds);
    }
}
