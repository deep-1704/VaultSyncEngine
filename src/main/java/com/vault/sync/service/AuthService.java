package com.vault.sync.service;

import com.vault.sync.entity.Device;
import com.vault.sync.entity.VaultUser;
import com.vault.sync.repository.DeviceRepository;
import com.vault.sync.repository.UserRepository;
import com.vault.sync.utils.DeviceOwnershipException;
import com.vault.sync.utils.DuplicateUsernameException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public AuthService(UserRepository _userRepository, DeviceRepository _devicerepository){
        this.userRepository = _userRepository;
        this.deviceRepository = _devicerepository;
    }

    public void validateDeviceOwnership(String deviceId, String ownerUsername) throws DeviceOwnershipException {
        if (deviceId == null) {
            return;
        }
        Optional<Device> existingDevice = deviceRepository.findById(deviceId);
        if (existingDevice.isPresent() && !Objects.equals(existingDevice.get().getOwner(), ownerUsername)) {
            throw new DeviceOwnershipException("Device " + deviceId + " is already registered to another user");
        }
    }

    public void signupUser(VaultUser vaultUser) throws DuplicateUsernameException {
        if(userRepository.existsById(vaultUser.getUsername()))
            throw new DuplicateUsernameException("VaultUser with username: " + vaultUser.getUsername() + " already exist");

        vaultUser.setPassword(passwordEncoder.encode(vaultUser.getPassword()));
        userRepository.save(vaultUser);
    }

    public void registerDevice(Device device){
        deviceRepository.save(device);
    }
}
