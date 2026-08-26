package com.vault.sync.controller;

import com.vault.sync.entity.apientity.DeviceCredential;
import com.vault.sync.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sync")
public class SyncController {
    private final SyncService syncService;

    @Autowired
    public SyncController(SyncService _syncService){
        this.syncService = _syncService;
    }

    @PostMapping("/")
    public ResponseEntity<Void> syncCredToDevices(@RequestBody List<DeviceCredential> deviceCredentials){
        syncService.syncDevices(deviceCredentials);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
