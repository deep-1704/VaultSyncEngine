package com.vault.sync.controller;

import com.vault.sync.entity.Device;
import com.vault.sync.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> deleteDevice(
            @PathVariable String deviceId,
            Authentication authentication
    ){
        Optional<Device> deviceOpt = deviceService.getDeviceById(deviceId);
        if(deviceOpt.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        Device device = deviceOpt.get();
        if(!Objects.equals(device.getOwner(), authentication.getName())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        deviceService.deleteDevice(device);
        return ResponseEntity.noContent().build();
    }
}
