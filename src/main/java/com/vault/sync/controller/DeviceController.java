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

    @GetMapping("")
    public ResponseEntity<List<Device>> getDevices(
            Authentication authentication,
            @RequestParam(required = false) String username
    ){
        if(username == null) username = authentication.getName();

        List<Device> devices = deviceService.getDevicesByOwner(username);

        return ResponseEntity.ok(devices);
    }

    @GetMapping("/shared/{sharedCredId}")
    public ResponseEntity<List<Device>> getDevicesWithSharedCred(@PathVariable Long sharedCredId){
        List<Device> devices = deviceService.getDevicesByShareId(sharedCredId);

        return ResponseEntity.ok(devices);
    }
}
