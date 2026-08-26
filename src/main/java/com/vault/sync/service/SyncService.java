package com.vault.sync.service;

import com.vault.sync.entity.Device;
import com.vault.sync.entity.SyncItem;
import com.vault.sync.entity.apientity.DeviceCredential;
import com.vault.sync.repository.DeviceRepository;
import com.vault.sync.repository.SyncItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SyncService {
    private final DeviceRepository deviceRepository;
    private final SyncItemRepository syncItemRepository;

    @Autowired
    public SyncService(
            DeviceRepository _deviceRepository,
            SyncItemRepository _syncItemRepository
    ){
        this.deviceRepository = _deviceRepository;
        this.syncItemRepository = _syncItemRepository;
    }

    public void syncDevices(List<DeviceCredential> deviceCredentials){
        List<SyncItem> syncItems = deviceCredentials.stream()
                .map(deviceCredential ->
                    new SyncItem(
                        deviceCredential.device().getId(),
                        deviceCredential.credential().getId(),
                        deviceCredential.content()
                    )
                )
                .toList();

        syncItemRepository.saveAll(syncItems);
    }

    public List<SyncItem> getSyncedCredentials(String deviceId){
        return syncItemRepository.findAllByDeviceId(deviceId);
    }

}
