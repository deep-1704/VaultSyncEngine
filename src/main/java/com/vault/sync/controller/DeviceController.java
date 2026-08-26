package com.vault.sync.controller;

import com.vault.sync.entity.Device;
import com.vault.sync.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/device")
public class DeviceController {
    private final SyncService syncService;

    @Autowired
    public DeviceController(SyncService _syncService){
        this.syncService = _syncService;
    }
    @GetMapping("/{username}")
    public ResponseEntity<List<Device>> getDevicesByUsername(@PathVariable String username){
        List<Device> devices = syncService.getDevicesByOwner(username);
        return ResponseEntity.ok(devices);
    }
}
