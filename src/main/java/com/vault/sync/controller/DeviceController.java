package com.vault.sync.controller;

import com.vault.sync.entity.Device;
import com.vault.sync.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/device")
public class DeviceController {
    private final DeviceService deviceService;

    @Autowired
    public DeviceController(DeviceService _deviceService){
        this.deviceService = _deviceService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<List<Device>> getDevicesByUsername(@PathVariable String username){
        List<Device> devices = deviceService.getDevicesByOwner(username);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("")
    public ResponseEntity<List<Device>> getDevices(
            Authentication authentication,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long credId
    ){
        List<Device> devices;
        if(username == null && credId == null){
            username = authentication.getName();
            devices = deviceService.getDevicesByOwner(username);
        } else if (username == null){
            devices = deviceService.getDevicesWithCred(credId);
        } else if(credId == null){
            devices = deviceService.getDevicesByOwner(username);
        } else {
            devices = deviceService.getDevicesWithCred(credId, username);
        }

        return ResponseEntity.ok(devices);
    }
}
