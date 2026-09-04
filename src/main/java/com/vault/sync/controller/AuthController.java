package com.vault.sync.controller;

import com.vault.sync.entity.Device;
import com.vault.sync.entity.VaultUser;
import com.vault.sync.service.AuthService;
import com.vault.sync.utils.DeviceOwnershipException;
import com.vault.sync.utils.DuplicateUsernameException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;
    public record UserDevice(VaultUser user, Device device) {}

    public AuthController(AuthService authService){
        this.service = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody Device device, Authentication authentication){
        // Spring security handles authentication

        String username = authentication.getName();
        try {
            service.validateDeviceOwnership(device.getId(), username);
        } catch (DeviceOwnershipException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        device.setOwner(username);
        service.registerDevice(device);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@RequestBody UserDevice userDevice){
        try {
            service.validateDeviceOwnership(userDevice.device.getId(), userDevice.user.getUsername());
        } catch (DeviceOwnershipException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try{
            service.signupUser(userDevice.user);
        } catch (DuplicateUsernameException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        // Just to make sure
        userDevice.device.setOwner(userDevice.user.getUsername());

        service.registerDevice(userDevice.device);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
